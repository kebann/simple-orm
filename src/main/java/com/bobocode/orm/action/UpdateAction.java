package com.bobocode.orm.action;

import static com.bobocode.orm.action.ActionPriority.UPDATE_ACTION;

import com.bobocode.orm.jdbc.JdbcRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateAction implements EntityAction {

  private final Object entity;
  private final JdbcRepository jdbcRepository;

  @Override
  public void execute() {
    jdbcRepository.update(entity);
  }

  @Override
  public int priority() {
    return UPDATE_ACTION.getPriority();
  }
}
