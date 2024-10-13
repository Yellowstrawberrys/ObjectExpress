package net.yellowstrawberry.objectexpress.table.proxy;

import net.yellowstrawberry.objectexpress.ObjectExpress;
import net.yellowstrawberry.objectexpress.param.entity.AutoGenerate;
import net.yellowstrawberry.objectexpress.param.entity.Id;
import net.yellowstrawberry.objectexpress.param.entity.Transit;
import net.yellowstrawberry.objectexpress.table.Table;
import net.yellowstrawberry.objectexpress.table.proxy.obj.ObjectCache;
import net.yellowstrawberry.objectexpress.util.ArrayUtils;
import net.yellowstrawberry.objectexpress.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class TableProxyClazz<ID, T> implements Table<ID, T> {

    SortedMap<ID, ObjectCache<T>> data = new TreeMap<>();

    private final String tableName;
    private final ObjectExpress express;
    private final Class<ID> idClazz;
    private final Class<T> tClazz;
    private final Field idField;

    private final String[] inserts;
    private final Field[] fields;
    private final Field[] autoFields;

    @SuppressWarnings("unchecked")
    public TableProxyClazz(ObjectExpress express, Class<?> interfaze) {
        String tbn = interfaze.getName().endsWith("Table") ? interfaze.getName().substring(0, interfaze.getName().length() - 5) : interfaze.getName();
        this.tableName = express.isSnake() ? StringUtils.camelToSnake(tbn) : tbn;
        this.express = express;
        idClazz = (Class<ID>) ((ParameterizedType) interfaze.getGenericInterfaces()[0]).getActualTypeArguments()[0];
        tClazz = (Class<T>) ((ParameterizedType) interfaze.getGenericInterfaces()[0]).getActualTypeArguments()[1];
        idField = Arrays.stream(tClazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Id.class)).findFirst().orElseThrow();
        idField.setAccessible(true);
        inserts = new String[]{"","","",""};

        List<Field> f = new ArrayList<>();
        List<Field> af = new ArrayList<>();
        Arrays.stream(tClazz.getDeclaredFields()).forEach(e -> {
            if(e.isAnnotationPresent(Transit.class)) return;
            if(!e.isAnnotationPresent(AutoGenerate.class) && !e.isAnnotationPresent(Id.class)) {
                String k = express.isSnake()?StringUtils.camelToSnake(e.getName()):e.getName();
                inserts[0] += k+",";
                inserts[1] += "?,";
                inserts[2] += k+"=?,";
                f.add(e);
            }else {
                inserts[3] += (express.isSnake() ? StringUtils.camelToSnake(e.getName()) : e.getName()) + ",";
                af.add(e);
            }
        });

        inserts[0] = inserts[0].substring(0, inserts[0].length()-1);
        inserts[1] = inserts[1].substring(0, inserts[1].length()-1);
        inserts[2] = inserts[2].substring(0, inserts[2].length()-1);
        inserts[3] = inserts[3].substring(0, inserts[3].length()-1);
        fields = f.toArray(new Field[]{});
        autoFields = af.toArray(new Field[]{});
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

        return loadFromSQL("SELECT * FROM `%s` WHERE `%s`=? LIMIT 1;".formatted(tableName, express.isSnake()?StringUtils.camelToSnake(idField.getName()):idField.getName()), id).stream().findAny();
    }

    @Override
    public T save(T t) {
        Object[] a = Arrays.stream(fields).map(e-> {
            try {
                e.setAccessible(true);
                return e.getType().cast(e.get(t));
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }).toArray();

        Object id;
        try {
            id = idField.get(t);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if(id==null) {
            Object[] b;
            b = new Object[a.length*2];
            for(int i =0;i<a.length; i++) b[i] = b[a.length+i] = a[i];
            a = b;
        }else {
            a = ArrayUtils.concat(a, id);
        }



        try (Statement stmt = express.getCommunicator().executeQueryN(
                id==null?"INSERT INTO `%s` (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s;".formatted(tableName, inserts[0], inserts[1], inserts[2])
                        :"UPDATE `%s` SET %s WHERE `%s`=?".formatted(tableName, inserts[2], express.isSnake()?StringUtils.camelToSnake(idField.getName()):idField.getName()),
                a
        )){

            ResultSet set = stmt.getGeneratedKeys();
            if(set.first()) {
                for (Field f : autoFields) {
                    f.setAccessible(true);
                    f.set(t, cast(f.getType(), set.getObject(".insert_"+(express.isSnake() ? StringUtils.camelToSnake(f.getName()) : f.getName()))));
                }

                data.put(cast(idClazz, set.getObject("insert_"+(express.isSnake()?StringUtils.camelToSnake(idField.getName()):idField.getName()))), ObjectCache.of(t, System.currentTimeMillis()));
                return t;
            }
        }catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }catch (ClassCastException e) {
            throw new RuntimeException("Error while casting class! (Does key of table `%s` has same type as type of field that has @Id annotation?)".formatted(tableName), e);
        }
        return null;
    }

    private <A> A cast(Class<? extends A> c, Object o) {
        if(o.getClass().isAssignableFrom(c)) return c.cast(o);
        if(o instanceof Number n) {
            if(c.equals(Integer.class)) return c.cast(n.intValue());
            else if(c.equals(Long.class)) return c.cast(n.longValue());
            else if(c.equals(Double.class)) return c.cast(n.doubleValue());
            else if(c.equals(Float.class)) return c.cast(n.floatValue());
            else if(c.equals(Short.class)) return c.cast(n.shortValue());
            else if(c.equals(Byte.class)) return c.cast(n.byteValue());
        }
        throw new UnsupportedOperationException("Unsupported key type `%s`.".formatted(o.getClass().getName()));
    }

    @Override
    public void delete(T t) {
        try {
            express.getCommunicator().executeUpdate(
                    "DELETE FROM `%s` WHERE `id`=?;".formatted(tableName),
                    idField.get(t)
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeById(ID id) {
        express.getCommunicator().executeUpdate(
                "DELETE FROM `%s` WHERE `id`=?;".formatted(tableName),
                id
        );
    }

    @SuppressWarnings("unchecked")
    public void cleanCache() {
        Arrays.asList(data.entrySet().toArray()).forEach(e -> {
            if(System.currentTimeMillis()-((Map.Entry<ID, ObjectCache<T>>) e).getValue().getLastAccess() > 20_000) data.remove(((Map.Entry<ID, ObjectCache<T>>) e).getKey());
        });
    }

    @SuppressWarnings("unchecked")
    public List<T> loadFromSQL(String query, Object... args) {
        try (ResultSet set = express.getCommunicator().executeQuery(query, args)) {
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
        } catch (SQLException | NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
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
