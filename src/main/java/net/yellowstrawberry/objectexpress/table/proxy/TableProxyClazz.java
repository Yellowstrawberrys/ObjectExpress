package net.yellowstrawberry.objectexpress.table.proxy;

import net.yellowstrawberry.objectexpress.ObjectExpress;
import net.yellowstrawberry.objectexpress.param.entity.Id;
import net.yellowstrawberry.objectexpress.param.entity.Transit;
import net.yellowstrawberry.objectexpress.table.Table;
import net.yellowstrawberry.objectexpress.table.proxy.obj.ObjectCache;
import net.yellowstrawberry.objectexpress.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TableProxyClazz<ID, T> implements Table<ID, T> {

    SortedMap<ID, ObjectCache<T>> data = new TreeMap<>();

    private final String tableName;
    private final ObjectExpress express;
    private Class<T> tClazz;
    private String idField;
    public TableProxyClazz(ObjectExpress express, Class<?> interfaze) {
        String tbn = interfaze.getName().endsWith("Table")?interfaze.getName().substring(0, interfaze.getName().length()-5):interfaze.getName();
        this.tableName = express.isSnake()?StringUtils.camelToSnake(tbn):tbn;
        this.express = express;
        tClazz = (Class<T>) ((ParameterizedType) interfaze.getGenericInterfaces()[0]).getActualTypeArguments()[1];
        idField = Arrays.stream(tClazz.getDeclaredFields()).filter(f->f.isAnnotationPresent(Id.class)).findFirst().orElseThrow().getName();
    }

    @Override
    public List<T> findAll() {
        return loadFromSQL("SELECT * FROM `%s`".formatted(tableName));
    }

    @Override
    public Optional<T> findById(ID id) {
        if(data.containsKey(id)) {
            ObjectCache<T> c = data.get(id);
            c.logAccess();
            return Optional.of(c.get());
        }

        return loadFromSQL("SELECT * FROM `%s` WHERE `%s`=? LIMIT 1;".formatted(tableName, idField), id).stream().findAny();
    }

    @Override
    public T save(T t) {
        return null;
    }

    @Override
    public void delete(T t) {

    }

    @Override
    public T removeById(ID id) {
        return null;
    }

    public void cleanCache() {

    }

    public List<T> loadFromSQL(String query, Object... args) {
        try {
            ResultSet set = express.getCommunicator().executeQueryN(query, args);

            List<T> l = new ArrayList<>();
            while (set.next()) {
                T t = tClazz.getDeclaredConstructor().newInstance();
                ID id = null;
                for (Field f : t.getClass().getDeclaredFields()) {
                    if(f.isAnnotationPresent(Transit.class)) continue;
                    Object o = set.getObject(express.isSnake()? StringUtils.camelToSnake(f.getName()):f.getName());
                    if(f.isAnnotationPresent(Id.class)) id = (ID) o;
                    f.setAccessible(true);
                    f.set(t, o);
                }

                if(id == null) throw new RuntimeException("Database did not returned ID.");
                data.put(id, ObjectCache.of(t, System.currentTimeMillis()));
                l.add(t);
            }

            return l;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
