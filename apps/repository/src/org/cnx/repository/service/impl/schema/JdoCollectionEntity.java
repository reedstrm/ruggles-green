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

package org.cnx.repository.service.impl.schema;

import static com.google.common.base.Preconditions.checkState;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cnx.util.Nullable;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * A JDO representing a collection entity.
 * 
 * @author Tal Dayan
 */
@PersistenceCapable(table = SchemaConsts.COLLECTION_KEY_KIND)
public class JdoCollectionEntity extends CnxJdoEntity {

    /**
     * The key of this collection entity in the data store. Assigned automatically by the data store
     * first time the entity is persisted. The kind of this key is always
     * {@link SchemaConsts#COLLECTION_KEY_KIND} and they have a Long id assigned by the persistence
     * layer.
     */
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    /**
     * Number of versions of this collection. Value >= 0. If > 0, this is also the version of the
     * last version of this collection (version numbering is 1, 2, ...). Zero when the collection
     * was created but no version has been added.
     */
    @Persistent
    private Integer versionCount = 0;

    /**
     * @return the key of this collection entity. Guaranteed to be non null after the entity is
     *         persisted by the first time.
     */
    @Nullable
    public Key getKey() {
        return key;
    }

    /**
     * Get the id of this collection.
     * 
     * Collection ID is a web safe string that can be mapped back to the collection key. Collection
     * ids are permanent and can be safely used externally to refer to collections.
     * 
     * @return the collection id or null if the resource does not have a key yet.
     */
    @Nullable
    public String getCollectionId() {
        if (key == null) {
            return null;
        }
        checkState(SchemaConsts.COLLECTION_KEY_KIND.equals(key.getKind()),
                "Unexpected kind: %s at key %s", key.getKind(), key);
        return KeyUtil.idToString(SchemaConsts.COLLECTION_ID_PREFIX, key.getId());
    }

    public int getVersionCount() {
        return versionCount;
    }

    public int incrementVersionCount() {
        versionCount++;
        JDOHelper.makeDirty(this, "versionCount");
        return versionCount;
    }

    /**
     * Convert a collection id returned by getCollectionId() back to the resource key. Returns null
     * if collection id has invalid format.
     */
    @Nullable
    public static Key collectionIdToKey(String collectionId) {
        final Long collectionIdLong =
            KeyUtil.stringToId(SchemaConsts.COLLECTION_ID_PREFIX, collectionId);
        return (collectionIdLong == null) ? null : KeyFactory.createKey(
                SchemaConsts.COLLECTION_KEY_KIND, collectionIdLong);
    }
}
