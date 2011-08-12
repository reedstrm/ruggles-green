package org.cnx.repository.service.impl.persistence;

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

    public void write(OrmEntity ormEntity) {
        final Entity entity = ormEntity.toEntity();
        datastore.put(entity);

        // If orm entity has no key, get the key assigned by the datastore.
        if (ormEntity.getKey() == null) {
            ormEntity.setKey(entity.getKey());
        }

        // Santiy check.
        checkState(ormEntity.getKey().equals(entity.getKey()));
    }

    // TODO(tal): add 'extends OrmEntity' everywhere.
    public <T> T read(Class<T> c, Key key) throws EntityNotFoundException {
        final Entity entity = datastore.get(key);
        return deserialize(c,  entity);
    }

    private static <T> T deserialize(Class<T> c, Entity entity)  {
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

    // TODO(tal): can we get rid of entitySpec or class c args?
    public <T> List<T> readChildren(Class<T> c, OrmEntitySpec entitySpec, Key parentKey)  {

        final Query query = new Query(entitySpec.getKeyKind());
        query.setAncestor(parentKey);

        final List<Entity> entities = datastore.prepare(query).asList(
                FetchOptions.Builder.withDefaults());

        final List<T> ormEntities = Lists.newArrayList();

        for (Entity entity : entities) {
            ormEntities.add(deserialize(c,  entity));
        }

        return ormEntities;
    }

    public void delete(Key...keys) {
        datastore.delete(keys);
    }
}
