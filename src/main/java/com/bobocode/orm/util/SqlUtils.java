package com.bobocode.orm.util;

import static com.bobocode.orm.util.EntityUtils.extractFieldName;
import static com.bobocode.orm.util.EntityUtils.extractTableName;
import static com.bobocode.orm.util.EntityUtils.getFieldsSortedByName;
import static java.util.stream.Collectors.joining;

import com.bobocode.orm.annotation.Id;
import java.lang.reflect.Field;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlUtils {

  public static final String SELECT_BY_ID_STATEMENT = "SELECT * FROM %s WHERE %s = ?;";
  public static final String UPDATE_STATEMENT = "UPDATE %s SET %s WHERE id=?;";

  public static String createSelectByFieldSql(Class<?> entityType, Field filterField) {
    var tableName = extractTableName(entityType);
    var filterFieldName = extractFieldName(filterField);
    return SELECT_BY_ID_STATEMENT.formatted(tableName, filterFieldName);
  }

  public static String createUpdateSql(Class<?> entityType) {
    List<Field> sortedFields = getFieldsSortedByName(entityType);

    String updatableColumns =
        sortedFields.stream()
            .filter(f -> !f.isAnnotationPresent(Id.class))
            .map(EntityUtils::extractFieldName)
            .map(name -> name + "=?")
            .collect(joining(","));

    String tableName = extractTableName(entityType);

    return UPDATE_STATEMENT.formatted(tableName, updatableColumns);
  }
}
