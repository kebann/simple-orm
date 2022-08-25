package com.bobocode.orm.session.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bobocode.orm.entity.User;
import com.bobocode.orm.session.BaseTest;
import com.bobocode.orm.session.Session;
import com.bobocode.orm.session.SessionFactory;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SessionIntegrationTest extends BaseTest {

  Session session;
  SessionFactory sessionFactory;

  @BeforeEach
  void setUp() {
    sessionFactory = new DefaultSessionFactory(postgresDataSource());
    session = sessionFactory.createSession();
  }

  @DisplayName("Consecutive retrievals of entity by id should return cached entity")
  @Test
  void shouldReturnCachedEntityForMultipleRetrievalsById() {
    User user = session.find(User.class, 2L);
    User sameUser = session.find(User.class, 2L);

    assertThat(user).isSameAs(sameUser);
    session.close();
  }

  @DisplayName("Persisting a new record should inserted into DB upon the context closure")
  @Test
  void shouldInsertNewPersistedRecord() {
    User newUser = new User();
    newUser.setName("Max");
    newUser.setHandle("max_fax");
    newUser.setId(25L);

    session.persist(newUser);
    session.close();

    Session anotherSession = sessionFactory.createSession();
    User insertedUser = anotherSession.find(User.class, newUser.getId());

    assertThat(newUser).isEqualTo(insertedUser);
  }

  @DisplayName("Entity should be removed upon the context closure")
  @Test
  void shouldRemoveEntity() {
    User user = session.find(User.class, 5L);
    session.remove(user);
    session.close();

    Session anotherSession = sessionFactory.createSession();
    assertThatThrownBy(() -> anotherSession.find(User.class, user.getId()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No records found for '%s' entity with id=%s",
            User.class.getSimpleName(), user.getId());
  }

  @DisplayName("Changes to entity within session should be persisted upon session closure")
  @Test
  void shouldPersistChangesToDbOnSessionClosureForDirtyEntity() {
    String newName = "Emily";

    User user = session.find(User.class, 2L);
    user.setName(newName);
    session.close();

    Session anotherSession = sessionFactory.createSession();
    User updatedUser = anotherSession.find(User.class, 2L);

    assertThat(updatedUser.getName()).isEqualTo(newName);
  }

  @DisplayName("Access to a closed session should result into IllegalStateException")
  @Test
  void shouldThrowIllegalStateExceptionIfAccessingClosedSession() {
    session.close();

    assertThatThrownBy(() -> session.find(User.class, 2L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot access already closed session.");
  }

  @DisplayName("IllegalStateException is thrown if no entity found by id")
  @Test
  void find_givenNoEntityFoundById_shouldThrowException() {
    var id = ThreadLocalRandom.current().nextInt(100, 200);
    var entityType = User.class.getSimpleName();
    assertThatThrownBy(() -> session.find(User.class, id))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No records found for '%s' entity with id=%s", entityType, id);
  }
}
