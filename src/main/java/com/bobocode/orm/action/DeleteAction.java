package com.bobocode.orm.action;

import static com.bobocode.orm.action.ActionPriority.DELETE_ACTION;

import com.bobocode.orm.jdbc.JdbcRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAction implements EntityAction {

  private final Object entity;
  private final JdbcRepository jdbcRepository;

  @Override
  public void execute() {
    jdbcRepository.remove(entity);
  }

  @Override
  public int priority() {
    return DELETE_ACTION.getPriority();
  }
}
