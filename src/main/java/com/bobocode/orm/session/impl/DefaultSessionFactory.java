package com.bobocode.orm.session.impl;

import com.bobocode.orm.session.SessionFactory;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSessionFactory implements SessionFactory {

  private final DataSource dataSource;

  public StatefulSession createSession() {
    return new StatefulSession(dataSource);
  }
}
