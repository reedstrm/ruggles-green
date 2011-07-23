/*
 * Copyright 2011 Google Inc.
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

package org.cnx.repository.schema;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cnx.repository.common.KeyUtil;
import org.cnx.util.Nullable;

import com.google.appengine.api.blobstore.BlobKey;

/**
 * A JDO representing a resource entity.
 * 
 * @author Tal Dayan
 */
@PersistenceCapable(table = SchemaConsts.RESOURCE_KEY_KIND)
public class JdoResourceEntity {

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
     * time the entity is persisted. Unique only within the key of this entity type. The externally
     * exposed resource id is derived from this key.
     */
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

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
     * Get the id of this resource. The id is unique only among resource entities.
     * 
     * Entity id is populated first time the entity is persisted. Until then this method returns
     * null.
     */
    @Nullable
    public Long getId() {
        return id;
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
     * Convert a resource entity id to externally visible string encoded id. This is the inverse
     * operation of stringToResouceId();
     * 
     * @param resourceKey a valid resource entity key.
     * @return an externally visible resource id of this resource.
     */
    public static String resoureIdToString(Long resourceId) {
        return KeyUtil.idToString(SchemaConsts.RESOURCE_ID_PREFIX, resourceId);
    }

    /**
     * Convert a resource entity id to externally visible resource id. The is the inverse operation
     * of resourceIdToString();
     * 
     * @param idString an externally visible resource id generated by keyToString();
     * @return resource key or null if invalid resource id string.
     */
    @Nullable
    public static Long stringToResourceId(String idString) {
        return KeyUtil.stringToId(SchemaConsts.RESOURCE_ID_PREFIX, idString);
    }
}
