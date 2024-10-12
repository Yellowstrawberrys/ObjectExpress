package net.yellowstrawberry.objectexpress.table.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TableProxy implements InvocationHandler {

    private final Object proxiee;

    public TableProxy(Object proxiee){
        this.proxiee = proxiee;
    }

    public static <T> T as(Class<T> table, Object target){
        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class[]{table},
                new TableProxy(target)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {
            return proxiee.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes()).invoke(proxiee, args);
        } catch (NoSuchMethodException e) {
            System.out.println(method.getName());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
