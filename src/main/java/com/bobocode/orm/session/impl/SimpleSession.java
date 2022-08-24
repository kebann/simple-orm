package com.bobocode.orm.session.impl;

import com.bobocode.orm.context.PersistenceContext;
import com.bobocode.orm.jdbc.JDBCRepository;
import com.bobocode.orm.session.Session;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleSession implements Session {

  private final PersistenceContext persistenceContext;
  private final JDBCRepository jdbcRepository;
  private boolean open;

  public SimpleSession(DataSource dataSource) {
    this.persistenceContext = new PersistenceContext();
    this.jdbcRepository = new JDBCRepository(dataSource, persistenceContext);
    this.open = true;
  }

  @Override
  public <T> T find(Class<T> entityType, Object id) {
    checkIsOpen();
    log.info("Searching for {} entity with id = {}", entityType.getSimpleName(), id);
    return jdbcRepository.findOneById(entityType, id);
  }

  @Override
  public void close() {
    persistenceContext.getDirtyEntities().forEach(jdbcRepository::update);
    persistenceContext.clear();
    open = false;
    log.info("The current session has been closed");
  }

  private void checkIsOpen() {
    if (!open) {
      throw new IllegalStateException("Cannot access already closed session.");
    }
  }
}
