package com.bobocode.orm.session;

import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseTest {

  @Container
  static PostgreSQLContainer<?> postgresContainer =
      new PostgreSQLContainer<>("postgres:14-alpine").withInitScript("init.sql");

  public static DataSource postgresDataSource() {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setURL(postgresContainer.getJdbcUrl());
    dataSource.setUser(postgresContainer.getUsername());
    dataSource.setPassword(postgresContainer.getPassword());

    return dataSource;
  }
}
