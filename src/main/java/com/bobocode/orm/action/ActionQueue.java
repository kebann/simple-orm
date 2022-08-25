package com.bobocode.orm.action;

import static java.util.Comparator.comparing;

import java.util.PriorityQueue;
import java.util.Queue;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActionQueue implements Queue<EntityAction> {

  @Delegate private final Queue<EntityAction> delegate;

  public ActionQueue() {
    this.delegate = new PriorityQueue<>(comparing(EntityAction::priority));
  }

  public void executeAllActions() {
    log.debug("Processing actions in the queue ...");
    while (!delegate.isEmpty()) {
      var action = delegate.poll();
      action.execute();
    }
  }
}
