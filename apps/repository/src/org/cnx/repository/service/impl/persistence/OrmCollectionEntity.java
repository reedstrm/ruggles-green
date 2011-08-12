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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * A POJO representing a collection entity.
 *
 * @author Tal Dayan
 */
public class OrmCollectionEntity extends OrmEntity {

    static final OrmEntitySpec ENTITY_SPEC = new OrmEntitySpec("Collection", "c");

    private static final String VERSION_COUNT_PROPERTY = "versions";

    /**
     * Number of versions of this collection. Value >= 0. If > 0, this is also the version of the
     * last version of this collection (version numbering is 1, 2, ...). Zero when the collection
     * was created but no version has been added.
     */
    private int versionCount = 0;

    /**
     * Construct an collection entity with zero versions.
     *
     * Useful when creating new collections.
     */
    public OrmCollectionEntity() {
        super(ENTITY_SPEC, null);
    }

    /**
     * Deserialize a collection version entity from a datastore entity.
     */
    public OrmCollectionEntity(Entity entity) {
        super(ENTITY_SPEC, checkNotNull(entity.getKey(), "No entity key"));
        // NOTE(tal): numeric values are stored as Longs.
        this.versionCount = ((Long) entity.getProperty(VERSION_COUNT_PROPERTY)).intValue();
    }

    public int getVersionCount() {
        return versionCount;
    }

    public int incrementVersionCount() {
        versionCount++;
        return versionCount;
    }

    /**
     * Convert a collection id returned by getCollectionId() back to the resource key. Returns null
     * if collection id has invalid format.
     */
    @Nullable
    public static Key collectionIdToKey(String collectionId) {
        final Long collectionIdLong =
            KeyUtil.stringToId(ENTITY_SPEC.getIdPrefix(), collectionId);
        return (collectionIdLong == null) ? null : KeyFactory.createKey(
                ENTITY_SPEC.getKeyKind(), collectionIdLong);
    }

    @Override
    protected void serializeToEntity(Entity entity) {
        entity.setProperty(VERSION_COUNT_PROPERTY, versionCount);  // serialized as Long
    }
}