package com.bobocode.orm.session.impl;

import com.bobocode.orm.action.ActionQueue;
import com.bobocode.orm.action.DeleteAction;
import com.bobocode.orm.action.InsertAction;
import com.bobocode.orm.action.UpdateAction;
import com.bobocode.orm.jdbc.JdbcRepository;
import com.bobocode.orm.session.Session;
import com.bobocode.orm.session.context.PersistenceContext;
import javax.sql.DataSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatefulSession implements Session {

  private final PersistenceContext persistenceContext;
  private final JdbcRepository jdbcRepository;
  private final ActionQueue actionQueue;
  private boolean open;

  public StatefulSession(DataSource dataSource) {
    log.info("Creating a new session");
    this.persistenceContext = new PersistenceContext();
    this.jdbcRepository = new JdbcRepository(dataSource, persistenceContext);
    this.open = true;
    this.actionQueue = new ActionQueue();
  }

  @Override
  public <T> T find(Class<T> entityType, Object id) {
    checkIsOpen();
    log.info("Searching for {} entity with id = {}", entityType.getSimpleName(), id);
    return jdbcRepository.findOneById(entityType, id);
  }

  @Override
  public void persist(@NonNull Object entity) {
    checkIsOpen();

    log.info("Persisting entity {}", entity);
    if (!persistenceContext.containsKey(entity)) {
      persistenceContext.addEntity(entity);
      actionQueue.add(new InsertAction(entity, jdbcRepository));
    }
  }

  @Override
  public void remove(Object entity) {
    checkIsOpen();

    persistenceContext.remove(entity);
    actionQueue.add(new DeleteAction(entity, jdbcRepository));
  }

  @Override
  public void flush() {
    log.debug("Flushing session ...");
    checkIsOpen();

    processDirtyEntities();
    actionQueue.executeAllActions();
  }

  @Override
  public void close() {
    log.debug("Closing the current session ...");
    checkIsOpen();

    flush();
    persistenceContext.clear();
    open = false;
    log.debug("The current session has been closed");
  }

  private void checkIsOpen() {
    if (!open) {
      throw new IllegalStateException("Cannot access already closed session.");
    }
  }

  private void processDirtyEntities() {
    persistenceContext
        .getDirtyEntities()
        .forEach(
            e -> {
              log.trace("Creating update action for dirty entity: {}", e);
              actionQueue.add(new UpdateAction(e, jdbcRepository));
            });
  }
}
