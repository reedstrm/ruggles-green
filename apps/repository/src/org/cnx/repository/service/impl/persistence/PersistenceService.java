package org.cnx.repository.service.impl.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Constructor;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

public class PersistenceService {

    private final DatastoreService datastore;

    public PersistenceService(DatastoreService datastore) {
        this.datastore = checkNotNull(datastore);
    }

    /**
     * Write entities to the persistence layer.
     * 
     * If an orm entity has a key than it is stored under this key. If an entity of that key already
     * exists, it is overwritten. If an orm entity does not have a key, a unique key is assign to it
     * by this method.
     */
    public void write(OrmEntity... ormEntities) {
        final int n = ormEntities.length;

        final List<Entity> entities = Lists.newArrayList();
        for (int i = 0; i < n; i++) {
            entities.add(ormEntities[i].toEntity());
        }

        final List<Key> keys = datastore.put(entities);

        // If an orm entity has no key, get the key assigned by the datastore.
        for (int i = 0; i < n; i++) {
            if (ormEntities[i].getKey() == null) {
                ormEntities[i].setKey(keys.get(i));
            } else {
                // Santiy check.
                checkState(ormEntities[i].getKey().equals(keys.get(i)));
            }
        }
    }

    public <T extends OrmEntity> T read(Class<T> entityClass, Key key)
        throws EntityNotFoundException {
        final Entity entity = datastore.get(key);
        return deserialize(entityClass, entity);
    }

    private static <T extends OrmEntity> T deserialize(Class<T> c, Entity entity) {
        try {
            final Constructor<T> constructor = c.getConstructor(Entity.class);
            return constructor.newInstance(entity);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Transaction beginTransaction() {
        return datastore.beginTransaction();
    }

    /**
     * Read the list of direct children entities of a given parent.
     * 
     * @param entityClass the children entity class. Only children of this class are read.
     * @param parentKey the parent object key.
     * 
     * @return a list of the child entities.
     */
    public <T extends OrmEntity> List<T> readChildren(Class<T> entityClass, Key parentKey) {
        final OrmEntitySpec entitySpec = entityClassSpec(entityClass);

        final Query query = new Query(entitySpec.getKeyKind());
        query.setAncestor(parentKey);

        final List<Entity> entities =
            datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

        final List<T> ormEntities = Lists.newArrayList();

        for (Entity entity : entities) {
            ormEntities.add(deserialize(entityClass, entity));
        }

        return ormEntities;
    }

    /**
     * Invoke the getSpec() method of an entity class to get its spec.
     */
    private static <T extends OrmEntity> OrmEntitySpec entityClassSpec(Class<T> entityClass) {
        try {
            return (OrmEntitySpec) entityClass.getDeclaredMethod("getSpec").invoke(null);
        } catch (Throwable e) {
            throw new RuntimeException("Error involing static method getSpec() of class "
                + entityClass, e);
        }
    }

    /**
     * Delete entities with given keys.
     */
    public void delete(Key... keys) {
        datastore.delete(keys);
    }

    /**
     * Test if an object of this key already exists. This operation has the cost of persistence
     * lookup.
     */
    public boolean hasObjectWithKey(Key key) {
        checkArgument(key != null);
        try {
            @SuppressWarnings("unused")
            final Entity entity = datastore.get(key);
        } catch (EntityNotFoundException e) {
            return false;
        }
        return true;
    }
}
