package net.yellowstrawberry.objectexpress;

import net.yellowstrawberry.objectexpress.table.Table;
import net.yellowstrawberry.objectexpress.table.proxy.TableProxy;
import net.yellowstrawberry.objectexpress.table.proxy.TableProxyClazz;
import net.yellowstrawberry.objectexpress.util.SQLCommunicator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class ObjectExpress {
    private final SQLCommunicator sql;
    private final String targetPackage;
    private final boolean isSnake;

    private final HashMap<Class<?>, Table<?,?>> proxies = new HashMap<>();

    public ObjectExpress(String url, String db, String user, String password, String targetPackage) {
        this(url, db, user, password, targetPackage, true);
    }

    public ObjectExpress(
            String url,
            String db,
            String user,
            String password,

            String targetPackage,
            boolean isSnake
    ) {
        sql = new SQLCommunicator(url, user, password, db);
        this.targetPackage = targetPackage;
        this.isSnake = isSnake;

        findTables();
    }

    public void registerTables(Object o) {
        Arrays.stream(o.getClass().getDeclaredFields())
                .filter(f -> Arrays.stream(f.getType().getInterfaces()).anyMatch(p->p.isAssignableFrom(Table.class)))
                .forEach(e -> {
                    if(proxies.containsKey(e.getType())) {
                        e.setAccessible(true);
                        try {
                            e.set(o, proxies.get(e.getType()));
                        } catch (IllegalAccessException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
    }

    private void findTables() {
        findAllClasses().forEach(e -> {
            if(Arrays.asList(e.getInterfaces()).contains(Table.class) && !e.equals(Table.class)) {
                proxies.put(e, (Table<?, ?>) TableProxy.as(e, new TableProxyClazz<>(this, e)));
            }
        });
    }

    private Set<Class<?>> findAllClasses() {
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(targetPackage.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> {
                    try {
                        return Class.forName((targetPackage.isBlank()?"":targetPackage + ".")
                                + line.substring(0, line.lastIndexOf('.')));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Class::isInterface)
                .collect(Collectors.toSet());
    }

    public boolean isSnake() {
        return isSnake;
    }

    public SQLCommunicator getCommunicator() {
        return sql;
    }
}
