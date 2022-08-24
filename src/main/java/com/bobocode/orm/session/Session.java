package com.bobocode.orm.session;

public interface Session {

  <T> T find(Class<T> entityClass, Object primaryKey);

  void close();
}
