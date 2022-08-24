package com.bobocode.orm.context;

import com.bobocode.orm.util.EntityKey;
import com.bobocode.orm.util.EntityUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersistenceContext {

  private final Map<EntityKey<?>, Object> entitiesCache = new HashMap<>();
  private final Map<EntityKey<?>, List<?>> entitiesSnapshots = new HashMap<>();

  public boolean contains(@NonNull EntityKey<?> key) {
    return entitiesCache.containsKey(key);
  }

  public <T> T getEntity(@NonNull EntityKey<T> key) {
    return key.getType().cast(entitiesCache.get(key));
  }

  public void addEntity(@NonNull Object entity) {
    Object id = EntityUtils.extractId(entity);
    EntityKey<?> key = EntityKey.of(entity.getClass(), id);

    entitiesCache.put(key, entity);

    List<?> snapshot = EntityUtils.toSnapshot(entity);
    log.trace("Created snapshot for {} entity: {}", entity.getClass().getSimpleName(), snapshot);
    entitiesSnapshots.put(key, snapshot);
  }

  public List<?> getDirtyEntities() {
    var dirtyEntities = new ArrayList<>();

    entitiesCache.forEach(
        ((key, entity) -> {
          List<?> initialSnapshot = entitiesSnapshots.get(key);
          List<?> currentSnapshot = EntityUtils.toSnapshot(entity);

          if (!initialSnapshot.equals(currentSnapshot)) {
            log.trace(
                "Found 'dirty' entity: \n initialSnapshot: {}, \n currentSnapshot: {}",
                initialSnapshot,
                currentSnapshot);

            dirtyEntities.add(entity);
          }
        }));

    return dirtyEntities;
  }

  public void clear() {
    entitiesCache.clear();
    entitiesSnapshots.clear();
  }
}
