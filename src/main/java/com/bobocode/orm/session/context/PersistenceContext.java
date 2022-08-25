package com.bobocode.orm.session.context;

import static com.bobocode.orm.util.EntityUtils.extractId;
import static com.bobocode.orm.util.EntityUtils.toSnapshot;

import com.bobocode.orm.util.EntityKey;
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

  public boolean containsKey(@NonNull Object entity) {
    EntityKey<?> key = new EntityKey<>(entity.getClass(), extractId(entity));
    return entitiesCache.containsKey(key);
  }

  public boolean containsKey(@NonNull EntityKey<?> key) {
    return entitiesCache.containsKey(key);
  }

  public <T> T getEntity(@NonNull EntityKey<T> key) {
    return key.type().cast(entitiesCache.get(key));
  }

  public void addEntity(@NonNull Object entity) {
    EntityKey<?> key = new EntityKey<>(entity.getClass(), extractId(entity));

    entitiesCache.put(key, entity);

    List<?> snapshot = toSnapshot(entity);
    log.trace("Created snapshot for {} entity: {}", entity.getClass().getSimpleName(), snapshot);
    entitiesSnapshots.put(key, snapshot);
  }

  public List<Object> getDirtyEntities() {
    var dirtyEntities = new ArrayList<>();

    entitiesCache.forEach(
        ((key, entity) -> {
          List<?> baselineSnapshot = entitiesSnapshots.get(key);
          List<?> currentSnapshot = toSnapshot(entity);

          if (!baselineSnapshot.equals(currentSnapshot)) {
            log.trace(
                "Found 'dirty' entity: \n baselineSnapshot: {}, \n currentSnapshot: {}",
                baselineSnapshot,
                currentSnapshot);

            dirtyEntities.add(entity);
          }
        }));

    return dirtyEntities;
  }

  public void clear() {
    log.debug("Clearing the persistence context");
    entitiesCache.clear();
    entitiesSnapshots.clear();
  }

  public void remove(@NonNull Object entity) {
    var key = new EntityKey<>(entity.getClass(), extractId(entity));

    entitiesCache.remove(key);
    entitiesSnapshots.remove(key);
  }
}
