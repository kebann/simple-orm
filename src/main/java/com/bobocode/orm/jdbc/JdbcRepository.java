package com.bobocode.orm.jdbc;

import static com.bobocode.orm.util.EntityUtils.extractFieldName;
import static com.bobocode.orm.util.EntityUtils.extractFieldValue;
import static com.bobocode.orm.util.EntityUtils.extractId;
import static com.bobocode.orm.util.EntityUtils.extractIdField;
import static com.bobocode.orm.util.EntityUtils.getFieldsSortedByName;
import static java.util.function.Predicate.not;

import com.bobocode.orm.exception.OrmException;
import com.bobocode.orm.session.context.PersistenceContext;
import com.bobocode.orm.util.EntityKey;
import com.bobocode.orm.util.EntityUtils;
import com.bobocode.orm.util.SqlUtils;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JdbcRepository {

  private final DataSource dataSource;
  private final PersistenceContext context;

  public <T> T findOneById(Class<T> entityType, Object id) {
    var key = new EntityKey<>(entityType, id);

    if (context.containsKey(key)) {
      T entity = context.getEntity(key);
      log.info("Returning cached entity {}", entity);
      return entity;
    }

    var idField = extractIdField(entityType);
    var entities = findAllByField(entityType, idField, id);

    checkOnlyOneEntityFound(entityType, id, entities);

    return entities.get(0);
  }

  private void checkOnlyOneEntityFound(Class<?> entityType, Object id, List<?> entities) {
    if (entities.isEmpty()) {
      throw new IllegalStateException(
          "No records found for '%s' entity with id=%s".formatted(entityType.getSimpleName(), id));
    }
    if (entities.size() > 1) {
      throw new IllegalStateException(
          "Found more than 1 record for entity %s with id=%s"
              .formatted(entityType.getSimpleName(), id));
    }
  }

  <T> List<T> findAllByField(Class<T> entityType, Field filterField, Object fieldVal) {
    try (var connection = dataSource.getConnection()) {

      String sql = SqlUtils.createSelectByFieldSql(entityType, filterField);
      try (var statement = connection.prepareStatement(sql)) {
        statement.setObject(1, fieldVal);
        log.debug("Executing '{}'", statement);
        var resultSet = statement.executeQuery();

        List<T> entities = new ArrayList<>();
        while (resultSet.next()) {
          var entity = extractEntityFromResultSet(entityType, resultSet);
          entities.add(entity);
        }

        return entities;
      }
    } catch (Exception e) {
      throw new OrmException(e.getMessage(), e);
    }
  }

  @SneakyThrows
  private <T> T extractEntityFromResultSet(Class<T> entityType, ResultSet resultSet) {
    log.debug(
        "Parsing the result set to create instance of '{}' entity", entityType.getSimpleName());
    var instance = entityType.getConstructor().newInstance();

    for (var field : entityType.getDeclaredFields()) {
      field.setAccessible(true);
      var fieldName = extractFieldName(field);
      var fieldValue = resultSet.getObject(fieldName);

      log.trace("Setting '{}' field to '{}'", fieldName, fieldValue);
      field.set(instance, fieldValue);
    }

    context.addEntity(instance);
    return instance;
  }

  public void update(Object entity) {
    String sql = SqlUtils.createUpdateSql(entity.getClass());

    try (var connection = dataSource.getConnection()) {
      try (var statement = connection.prepareStatement(sql)) {
        List<Field> updatableFields =
            getFieldsSortedByName(entity.getClass()).stream()
                .filter(not(EntityUtils::isIdField))
                .toList();

        populatePreparedStatement(entity, statement, updatableFields);
        statement.setObject(updatableFields.size() + 1, extractId(entity));
        log.debug("Executing '{}'", statement);

        int updatedRowsCount = statement.executeUpdate();
        log.debug("Updated {} records ", updatedRowsCount);
      }
    } catch (SQLException e) {
      throw new OrmException(e.getMessage(), e);
    }
  }

  @SneakyThrows
  private void populatePreparedStatement(
      Object entity, PreparedStatement statement, List<Field> fields) {
    var idx = 1;

    for (var field : fields) {
      statement.setObject(idx, extractFieldValue(entity, field));
      idx++;
    }
  }

  public void insert(Object entity) {
    String sql = SqlUtils.createInsertSql(entity);

    try (var connection = dataSource.getConnection()) {
      try (var statement = connection.prepareStatement(sql)) {
        populatePreparedStatement(entity, statement, getFieldsSortedByName(entity.getClass()));

        log.debug("Executing '{}'", statement);
        var insertedRowsCount = statement.executeUpdate();

        log.debug("Inserted {} records", insertedRowsCount);
      }
    } catch (SQLException e) {
      throw new OrmException(e.getMessage(), e);
    }
  }

  public void remove(Object entity) {
    String sql = SqlUtils.createDeleteSql(entity.getClass());

    try (var connection = dataSource.getConnection()) {
      try (var statement = connection.prepareStatement(sql)) {
        statement.setObject(1, extractId(entity));
        log.debug("Executing '{}'", statement);
        var deletedRowsCount = statement.executeUpdate();

        log.debug("Deleted {} records ", deletedRowsCount);
      }
    } catch (SQLException e) {
      throw new OrmException(e.getMessage(), e);
    }
  }
}
