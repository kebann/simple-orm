package com.bobocode.orm.action;

import static com.bobocode.orm.action.ActionPriority.*;

import com.bobocode.orm.jdbc.JdbcRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InsertAction implements EntityAction {

  private final Object entity;
  private final JdbcRepository jdbcRepository;

  @Override
  public void execute() {
    jdbcRepository.insert(entity);
  }

  @Override
  public int priority() {
    return INSERT_ACTION.getPriority();
  }
}
