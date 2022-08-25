package com.bobocode.orm.util;

import static com.bobocode.orm.util.EntityUtils.extractFieldName;
import static com.bobocode.orm.util.EntityUtils.extractIdField;
import static com.bobocode.orm.util.EntityUtils.extractTableName;
import static com.bobocode.orm.util.EntityUtils.getFieldsSortedByName;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlUtils {

  private static final String SELECT_BY_ID_STATEMENT = "SELECT * FROM %s WHERE %s = ?;";
  private static final String DELETE_STATEMENT = "DELETE FROM %s WHERE id = ?;";
  private static final String UPDATE_STATEMENT = "UPDATE %s SET %s WHERE %s = ?;";
  private static final String INSERT_STATEMENT = "INSERT INTO %s(%s) VALUES(%s);";

  public static String createSelectByFieldSql(Class<?> entityType, Field filterField) {
    var tableName = extractTableName(entityType);
    var filterFieldName = extractFieldName(filterField);
    return SELECT_BY_ID_STATEMENT.formatted(tableName, filterFieldName);
  }

  public static String createDeleteSql(Class<?> entityType) {
    return DELETE_STATEMENT.formatted(extractTableName(entityType));
  }

  public static String createUpdateSql(Class<?> entityType) {
    List<Field> sortedFields = getFieldsSortedByName(entityType);

    String updatableColumns =
        sortedFields.stream()
            .filter(not(EntityUtils::isIdField))
            .map(EntityUtils::extractFieldName)
            .map(name -> name + "=?")
            .collect(joining(","));

    String tableName = extractTableName(entityType);
    String idFieldName = extractFieldName(extractIdField(entityType));

    return UPDATE_STATEMENT.formatted(tableName, updatableColumns, idFieldName);
  }

  public static String createInsertSql(Object entity) {
    List<Field> sortedFields = getFieldsSortedByName(entity.getClass());

    String insertableColumns =
        sortedFields.stream().map(EntityUtils::extractFieldName).collect(joining(","));

    String valuePlaceholders = sortedFields.stream().map(f -> "?").collect(joining(","));

    String tableName = extractTableName(entity.getClass());

    return INSERT_STATEMENT.formatted(tableName, insertableColumns, valuePlaceholders);
  }
}
