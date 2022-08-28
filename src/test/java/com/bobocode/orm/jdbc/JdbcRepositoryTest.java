package com.bobocode.orm.jdbc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.bobocode.orm.entity.User;
import com.bobocode.orm.session.context.PersistenceContext;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JdbcRepositoryTest {

  @Mock
  PersistenceContext persistenceContext;
  @Mock
  DataSource dataSource;
  @InjectMocks
  @Spy
  JdbcRepository repositorySpy;

  @DisplayName("findOneById method should throw exception of multiple records found")
  @Test
  void shouldThrowExceptionIfMultipleEntitiesFoundById() {
    var id = 22L;

    List<Object> users = List.of(new User().setId(id), new User().setId(id));
    doReturn(users).when(repositorySpy).findAllByField(any(), any(), any());
    doReturn(false).when(persistenceContext).containsKey(any());

    var entityType = User.class;
    assertThatThrownBy(() -> repositorySpy.findOneById(entityType, id))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Found more than 1 record for entity %s with id=%s", entityType.getSimpleName(), id);
  }
}
