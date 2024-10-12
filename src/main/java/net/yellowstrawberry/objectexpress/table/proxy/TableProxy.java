package net.yellowstrawberry.objectexpress.table.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TableProxy implements InvocationHandler {

    private Object proxiee;

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
        System.out.println(method.getName());
        return null;
    }
}
