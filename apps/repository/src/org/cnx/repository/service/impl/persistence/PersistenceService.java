/*
 * Copyright (C) 2011 The CNX Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.cnx.repository.service.impl.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

public class PersistenceService {

    private final DatastoreService datastore;

    public PersistenceService(DatastoreService datastore) {
        this.datastore = checkNotNull(datastore);

        PersistenceMigrationUtil.protectAllReservedKeys(datastore);
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

    private static <T extends OrmEntity> T deserialize(Class<T> entityClass, Entity entity) {
        try {
            final Constructor<T> constructor = entityClass.getConstructor(Entity.class);
            return constructor.newInstance(entity);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public PersistenceTransaction beginTransaction() {
        return new PersistenceTransaction(datastore.beginTransaction());
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
        final OrmEntitySpec entitySpec = OrmEntity.entityClassSpec(entityClass);

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

    /**
     * Query entity key list with support for paging.
     * 
     * Result order is undefined. Retrieving the entire list (possibly using multiple calls)
     * guarantees returning all the matching entities that existed before from before the first call
     * until the end of the last call.
     * 
     * IMPORTANT, callers should impose a reasonable max on {@code maxResults} to avoid memory
     * explosion.
     * 
     * TODO(tal): consider to use QueryResultIterator instead of QueryResultList for reduced memory
     * footprint.
     * 
     * @param entityClass the result ORM entity class.
     * @param maxResults max results to return to the user. May return less than that, even zero.
     * @param startCursor null if to iterate the list from start. Returned end cursor from previous
     *            query to continue. Asserted to be >= 1.
     * 
     * @return a pair of result list and end cursor. If end cursor is null then end of data has been
     *         reached. Otherwise, caller can issue another call using the end cursor as start
     *         cursor to fetch the next page.
     */
    public <T extends OrmEntity> Pair<List<Key>, String> entityKeyList(Class<T> entityClass,
            int maxResults, @Nullable String startCursor) {
        checkArgument(maxResults >= 1);

        final OrmEntitySpec entitySpec = OrmEntity.entityClassSpec(entityClass);

        // Querying for keys only.
        final Query q = new Query(entitySpec.getKeyKind()).setKeysOnly();

        final PreparedQuery pq = datastore.prepare(q);

        final FetchOptions fetchOptions = FetchOptions.Builder.withLimit(maxResults);

        if (startCursor != null) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
        }

        // NOTE(tal): these entities have keys only since we queried above for keys only. They
        // cannot be deserialized into ORM entities.
        final QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);

        final List<Key> keys = Lists.newArrayList();

        for (Entity entity : results) {
            keys.add(entity.getKey());
        }

        final boolean endOfData = (results.size() < maxResults);

        @Nullable
        final String endCursor =
            endOfData ? null : checkNotNull(results.getCursor(), "Null end cursor")
                .toWebSafeString();

        return Pair.of(keys, endCursor);
    }
}
