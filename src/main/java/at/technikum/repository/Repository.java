package at.technikum.repository;

import java.util.List;

public interface Repository<T, ID> {
  T create(T entity);
  T readById(ID id);
  List<T> readAll();
  boolean deleteById(ID id);
  T updateById(ID id, T entity);
}
