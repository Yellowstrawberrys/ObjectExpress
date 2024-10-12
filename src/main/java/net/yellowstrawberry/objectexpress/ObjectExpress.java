package net.yellowstrawberry.objectexpress;

import net.yellowstrawberry.objectexpress.table.Table;
import net.yellowstrawberry.objectexpress.table.proxy.TableProxy;
import net.yellowstrawberry.objectexpress.table.proxy.TableProxyClazz;
import net.yellowstrawberry.objectexpress.util.SQLCommunicator;
import net.yellowstrawberry.objectexpress.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectExpress {
    private final SQLCommunicator sql;
    private final String targetPackage;
    private final boolean isSnake;

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
        sql = null;
//        sql = new SQLCommunicator(url, db, user, password);
        this.targetPackage = targetPackage;
        this.isSnake = isSnake;

        registerTables();
    }

    private void registerTables() {
        a();
    }

    private void a() {
        findAllClasses().forEach(e -> {
            if(e.isInterface() && Arrays.asList(e.getInterfaces()).contains(Table.class) && !e.equals(Table.class)) {
                ((Table<?,?>) TableProxy.as(e, new TableProxyClazz<>(this, e))).findAll();
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
                .collect(Collectors.toSet());
    }

    public boolean isSnake() {
        return isSnake;
    }

    public SQLCommunicator getCommunicator() {
        return sql;
    }
}
