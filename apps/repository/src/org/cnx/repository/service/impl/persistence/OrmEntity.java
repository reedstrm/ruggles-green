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

import javax.annotation.Nullable;

import org.cnx.repository.service.impl.persistence.KeyUtil;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * Base class of all orm entities.
 *
 * Orm entities are POJO's representing respective datastore entities.
 * They do not perform any persistence operations, and just provides data
 * mapping to/from datastore Entity.
 *
 * @author Tal Dayan
 */
public abstract class OrmEntity {

    private final OrmEntitySpec entitySpec;

    /**
     * Entity key. Can be assigned by user or assigned by the data store first time the entity is
     * persisted.
     */
    @Nullable
    private Key key;

    protected OrmEntity(OrmEntitySpec entitySpec, @Nullable Key key) {
        this.entitySpec = entitySpec;
        this.key = key;
    }

    @Nullable
    public Key getKey() {
        return key;
    }

    /**
     * Set entity key.
     *
     * Asserts that the entity does not have already a key. Useful for setting keys assigned by the
     * data store after first persistence.
     */
    public void setKey(Key key) {
        checkNotNull(key);
        checkState(this.key == null, "Entity already has a key");
        checkArgument(entitySpec.getKeyKind().equals(key.getKind()), "Entity kind mismatch: %s",
                key.getKind());
        this.key = key;
    }

    /**
     * Get the id of this entity. Asserts that this entity has key and that this entity kind has id
     * prefix.
     */
    public String getId() {
        checkState(entitySpec.supportsIds(), "Entities of kind %s have no ids, just keys",
                entitySpec.getKeyKind());
        checkState(getKey() != null, "Entity has no key (kind = %s)", entitySpec.getKeyKind());
        // TODO(tal): assert here that the key has an id and has no name.
        return KeyUtil.idToString(entitySpec.getIdPrefix(), key.getId());
    }

    public Entity toEntity() {
        final Entity result =
            (getKey() == null) ? new Entity(entitySpec.getKeyKind()) : new Entity(key);
        serializeToEntity(result);
        return result;
    }

    /**
     * Sub classes hook to serialize their properties into an entity.
     */
    protected abstract void serializeToEntity(Entity entity);
}
