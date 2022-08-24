package com.bobocode.orm.util;

import com.bobocode.orm.annotation.Column;
import com.bobocode.orm.annotation.Id;
import com.bobocode.orm.annotation.Table;
import com.bobocode.orm.exception.NoSuchFieldException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class EntityUtils {

  public static Field extractIdField(Class<?> entityType) {
    return Arrays.stream(entityType.getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(Id.class))
        .findFirst()
        .orElseThrow(
            () ->
                new NoSuchFieldException(
                    "%s entity has no field annotated with @Id".formatted(entityType)));
  }

  @SneakyThrows
  public static Object extractId(Object entity) {
    var idField = extractIdField(entity.getClass());
    idField.setAccessible(true);
    return idField.get(entity);
  }

  public static String extractTableName(Class<?> entityType) {
    return Optional.ofNullable(entityType.getAnnotation(Table.class))
        .map(Table::name)
        .orElse(entityType.getSimpleName().toLowerCase());
  }

  @SneakyThrows
  public static List<?> toSnapshot(Object entity) {
    var snapshotFields = new ArrayList<>();
    var entityType = entity.getClass();
    var sortedFields = getFieldsSortedByName(entityType);

    for (var field : sortedFields) {
      field.setAccessible(true);
      var fieldVal = field.get(entity);
      snapshotFields.add(fieldVal);
    }

    return snapshotFields;
  }

  public static List<Field> getFieldsSortedByName(Class<?> entityType) {
    return Arrays.stream(entityType.getDeclaredFields())
        .sorted(Comparator.comparing(Field::getName))
        .toList();
  }

  public static String extractFieldName(Field field) {
    return Optional.ofNullable(field.getAnnotation(Column.class))
        .map(Column::name)
        .orElse(field.getName());
  }
}
