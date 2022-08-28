package com.bobocode.orm.action;

import static org.assertj.core.api.Assertions.assertThat;

import com.bobocode.orm.jdbc.JdbcRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class ActionQueueTest {

  @DisplayName("Action queue should sort added actions according to defined action priorities")
  @Test
  void shouldSortActionsAccordingToDefinedPriority() {
    var entity = new Object();

    List<EntityAction> actions = new ArrayList<>();
    actions.add(new DeleteAction(entity, null));
    actions.add(new UpdateAction(entity, null));
    actions.add(new InsertAction(entity, null));

    ActionQueue actionQueue = new ActionQueue();
    actionQueue.addAll(actions);

    actions.sort(Comparator.comparing(EntityAction::priority));

    assertThat(actionQueue).hasSameSizeAs(actions);
    for (var action : actions) {
      assertThat(action).isSameAs(actionQueue.poll());
    }

    assertThat(actionQueue).isEmpty();
  }

  @DisplayName("Action queue should be empty after executing all available actions in the queue")
  @Test
  void shouldBeEmptyAfterExecutingAllActions() {
    var entity = new Object();
    JdbcRepository repositoryMock = Mockito.mock(JdbcRepository.class);

    List<EntityAction> actions = new ArrayList<>();
    actions.add(new DeleteAction(entity, repositoryMock));
    actions.add(new UpdateAction(entity, repositoryMock));
    actions.add(new InsertAction(entity, repositoryMock));

    ActionQueue actionQueue = new ActionQueue();
    actionQueue.addAll(actions);

    assertThat(actionQueue).hasSameSizeAs(actions);

    actionQueue.executeAllActions();
    assertThat(actionQueue).isEmpty();

    InOrder inOrder = Mockito.inOrder(repositoryMock);
    inOrder.verify(repositoryMock).insert(entity);
    inOrder.verify(repositoryMock).update(entity);
    inOrder.verify(repositoryMock).remove(entity);
  }
}
