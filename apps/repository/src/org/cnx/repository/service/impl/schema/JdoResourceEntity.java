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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cnx.util.Nullable;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * A JDO representing a resource entity.
 * 
 * @author Tal Dayan
 */
@PersistenceCapable(table = SchemaConsts.RESOURCE_KEY_KIND)
public class JdoResourceEntity extends CnxJdoEntity {
    /**
     * Each resource must be in one of these states.
     * 
     * TODO(tal): decouple java enum names from datastore values (how?)
     */
    public enum State {
        /**
         * Initial state upon instantiation. Entity is never persisted in this state.
         */
        IDLE,

        /**
         * Resource ID allocated and entity persisted, pending blob upload.
         */
        PENDING_UPLOAD,

        /**
         * Blob has been uploaded.
         */
        UPLOADED;

        // TODO(tal): add a PUBLISHED? state. Currently resources are published
        // as soon as they are uploaded. Can provide a method to publish
        // a resource as well as 'create published' flag in the repository API
        // creation method.

        /**
         * Test if a resource entity in this state should have a blob key.
         */
        public boolean hasBlobKey() {
            return this == UPLOADED;
        }
    }

    /**
     * The key of this resource in the data store. Assigned automatically by the data store first
     * time the entity is persisted. The kind of this key is always SchemaConsts.RESOURCE_KEY_KIND
     * and they have a Long id assigned by the persistence manager.
     */
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    /**
     * The state of this resource. This is a required attribute.
     */
    @Persistent
    private State state = State.IDLE;

    /**
     * The blob key of this resource. Exists in states where hasBlobKey() is true, null otherwise.
     */
    @Persistent
    private BlobKey blobKey;

    /**
     * @return the key of this resource entity. Guaranteed to be non null after the entity is
     *         persisted by the first time.
     */
    @Nullable
    public Key getKey() {
        return key;
    }

    /**
     * Get the id of this resource.
     * 
     * Resource ID is a web safe string that can be mapped back to the resource key. Resource ids
     * are permanent and can be safely used externally to refer to resources.
     * 
     * @return the resource id or null if the resource does not have a key yet.
     */
    @Nullable
    public String getResourceId() {
        if (key == null) {
            return null;
        }
        checkState(SchemaConsts.RESOURCE_KEY_KIND.equals(key.getKind()),
            "Unexpected kind: %s at key %s", key.getKind(), key);
        return KeyUtil.idToString(SchemaConsts.RESOURCE_ID_PREFIX, key.getId());
    }

    @Nullable
    public State getState() {
        return state;
    }

    @Nullable
    public BlobKey getBlobKey() {
        return blobKey;
    }

    public void idleToPendingTransition() {
        checkState(state == State.IDLE, "Encountered %s", state);
        state = State.PENDING_UPLOAD;
    }

    public void pendingToUploadedTransition(BlobKey newBlobKey) {
        checkState(state == State.PENDING_UPLOAD, "Encountered %s", state);

        blobKey = checkNotNull(newBlobKey, "Null blob key");
        JDOHelper.makeDirty(this, "blobKey");

        state = State.UPLOADED;
        JDOHelper.makeDirty(this, "state");
    }

    /**
     * Convert a resource id returned by getResouceId() back to the resource key. Returns null if
     * resource id has invalid format.
     */
    @Nullable
    public static Key resourceIdToKey(String resourceId) {
        final Long resourceIdLong = KeyUtil.stringToId(SchemaConsts.RESOURCE_ID_PREFIX, resourceId);
        return (resourceIdLong == null) ? null : KeyFactory.createKey(
            SchemaConsts.RESOURCE_KEY_KIND, resourceIdLong);
    }
}
