package net.yellowstrawberry.objectexpress.table;

import java.util.List;
import java.util.Optional;

public interface Table<ID, T> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T save(T t);
    void delete(T t);
    void removeById(ID id);
}
