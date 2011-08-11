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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nullable;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * A Java class representing a datastore resource entity.
 *
 * This class does not perform any persistence operations, just data
 * mapping to/from datastore Entity.
 *
 * @author Tal Dayan
 */
public class OrmResourceEntity extends CnxJdoEntity {

    /** Resource keys always have this kind */
    public static final String KEY_KIND = "Resource";

    /** Resources ids are prefixed with this string */
    public static final String ID_PREFIX = "R";

    /**
     * Each resource must be in one of these states.
     *
     * NOTE(tal): the enum value names are used to persist the state.
     */
    public static enum State {
        /** Resource ID allocated and entity persisted, pending blob upload. */
        UPLOAD_PENDING,

        /** Blob has been uploaded and is ready to be served. */
        UPLOAD_COMPLETE;

        /** Do resources in this state have blob key? */
        public boolean hasBlobKey() {
            return this == UPLOAD_COMPLETE;
        }
    }

    private static final String STATE_PROPERTY = "state";
    private static final String BLOB_KEY_PROPERTY = "blob_key";

    /**
     * Entity key. Null until entity is persisted for the first time
     */
    @Nullable
    private Key key;

    /**
     * The state of this resource. This is a required attribute.
     */
    private State state = State.UPLOAD_PENDING;

    /**
     * The blob key of this resource. Exists in states where hasBlobKey() is true, null otherwise.
     */
    private BlobKey blobKey;

    /**
     * Construct an entity in the {@link State#UPLOAD_PENDING} state with null key.
     */
    public OrmResourceEntity() {
        this.key = null;
        this.state = State.UPLOAD_PENDING;
        this.blobKey = null;
    }

    /**
     * Construct an entity from a data store entity.
     */
    public OrmResourceEntity(Entity entity) {
        checkNotNull(entity.getKey());
        checkArgument(entity.getKey().getKind() == KEY_KIND, "Unexpected resource key kind: %s",
                entity.getKey().getKind());
        this.key = checkNotNull(entity.getKey(), "No entity key");
        this.state = State.valueOf((String) entity.getProperty(STATE_PROPERTY));
        this.blobKey = (BlobKey) entity.getProperty(BLOB_KEY_PROPERTY);
        checkArgument(state.hasBlobKey() == (blobKey != null), "Inconsistent state: %s", state);
    }

    /**
     * Constructs a datastore entity for persistence.
     */
    public Entity toEntity() {
        final Entity result = (key == null) ? new Entity(KEY_KIND) : new Entity(key);
        result.setProperty(STATE_PROPERTY, state.toString());
        result.setProperty(BLOB_KEY_PROPERTY, blobKey);
        return result;
    }

    @Nullable
    public Key getKey() {
        return key;
    }

    /**
     * Get the id of this resource. Asserts that this entity has key.
     */
    public String getResourceId() {
        checkState(key != null);
        return KeyUtil.idToString(ID_PREFIX, key.getId());
    }

    public State getState() {
        return state;
    }

    @Nullable
    public BlobKey getBlobKey() {
        return blobKey;
    }

    /**
     * Transition the entity from {@link State#UPLOAD_PENDING} state to {@link State#UPLOADED}
     * state.
     *
     * Asserts that the entity has key and is in {@link State#UPLOAD_PENDING} state.
     *
     * @param newBlobKey key of the resource blob.
     */
    public void pendingToUploadedTransition(BlobKey newBlobKey) {
        checkState(state == State.UPLOAD_PENDING, "Encountered %s", state);
        checkState(key != null, "Resource entity has no key");
        blobKey = checkNotNull(newBlobKey, "Null blob key");
        state = State.UPLOAD_COMPLETE;
    }

    /**
     * Convert a resource id returned by {@link #getResouceId()} back to the resource key. Returns
     * null if resource id has invalid format.
     */
    @Nullable
    public static Key resourceIdToKey(String resourceId) {
        final Long resourceIdLong = KeyUtil.stringToId(ID_PREFIX, resourceId);
        return (resourceIdLong == null) ? null : KeyFactory.createKey(KEY_KIND, resourceIdLong);
    }
}
