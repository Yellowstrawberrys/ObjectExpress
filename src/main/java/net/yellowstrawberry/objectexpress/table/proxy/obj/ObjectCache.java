package net.yellowstrawberry.objectexpress.table.proxy.obj;

public class ObjectCache<T> {

    private final T t;
    private long lastAccess;
    public ObjectCache(T t, long lastAccess) {
        this.t = t;
        this.lastAccess = lastAccess;
    }

    public static <T> ObjectCache<T> of(T t, long lastAccess) {
        return new ObjectCache<>(t, lastAccess);
    }

    public T get() {
        return t;
    }

    public void logAccess() {
        lastAccess = System.currentTimeMillis();
    }

    public long getLastAccess() {
        return lastAccess;
    }
}
