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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * A POJO representing a datastore resource entity.
 * 
 * @author Tal Dayan
 */
public class OrmResourceEntity extends OrmEntity {

    private static final OrmEntitySpec ENTITY_SPEC = new OrmEntitySpec("Resource", "r");

    private static final String STATE_PROPERTY = "state";
    private static final String BLOB_KEY_PROPERTY = "blob_key";

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

    /**
     * The state of this resource. This is a required attribute.
     */
    private State state = State.UPLOAD_PENDING;

    /**
     * The blob key of this resource. Exists in states where hasBlobKey() is true, null otherwise.
     */
    private BlobKey blobKey;

    /**
     * Construct a resource entity in the {@link State#UPLOAD_PENDING} state with null key.
     * 
     * Useful when creating new resources.
     */
    public OrmResourceEntity() {
        super(ENTITY_SPEC, null);
        this.state = State.UPLOAD_PENDING;
        this.blobKey = null;
    }

    /**
     * Construct a resource entity from a data store entity.
     * 
     * Useful when retrieving a resource from the datastore.
     */
    public OrmResourceEntity(Entity entity) {
        super(ENTITY_SPEC, checkNotNull(entity.getKey(), "No entity key"));
        this.state = State.valueOf((String) entity.getProperty(STATE_PROPERTY));
        this.blobKey = (BlobKey) entity.getProperty(BLOB_KEY_PROPERTY);
        checkArgument(state.hasBlobKey() == (blobKey != null), "Inconsistent state: %s", state);
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
        checkState(getKey() != null, "Resource entity has no key");
        blobKey = checkNotNull(newBlobKey, "Null blob key");
        state = State.UPLOAD_COMPLETE;
    }

    /**
     * Convert a resource id returned by {@link #getId()} back to the resource key. Returns null if
     * resource id has invalid format.
     */
    @Nullable
    public static Key resourceIdToKey(String resourceId) {
        final Long resourceIdLong = IdUtil.stringToId(ENTITY_SPEC.getIdPrefix(), resourceId);
        return (resourceIdLong == null) ? null : KeyFactory.createKey(ENTITY_SPEC.getKeyKind(),
                resourceIdLong);
    }

    @Override
    protected void serializeToEntity(Entity entity) {
        // TODO(tal): *** can we persist the enum directly, not as a string?
        entity.setProperty(STATE_PROPERTY, state.toString());
        entity.setProperty(BLOB_KEY_PROPERTY, blobKey);
    }

    public static OrmEntitySpec getSpec() {
        return ENTITY_SPEC;
    }

}
