package com.bobocode.orm.session.impl;

import com.bobocode.orm.session.SessionFactory;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleSessionFactory implements SessionFactory {

  private final DataSource dataSource;

  public SimpleSession createSession() {
    return new SimpleSession(dataSource);
  }
}
