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

package org.cnx.repository.service.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Date;

import javax.annotation.Nullable;

/**
 * Result of a successful getResourceInfo operation.
 * 
 * @author Tal Dayan
 */
public class GetResourceInfoResult {
    enum ResourceState {
        PENDING_UPLOAD,
        UPLOADED;

        /** Tests if a resource in this state has content. */
        public boolean hasContent() {
            return this != PENDING_UPLOAD;
        }
    }

    private final String resourceId;
    private final Date creationTime;
    private final ResourceState resourceState;

    /**
     * Content info. Available only when {@link #hasContent()} is true;
     */
    @Nullable
    private final UploadedResourceContentInfo contentInfo;

    /**
     * @param resourceId the id of this resource.
     * @param creationTime time this resource was created (not to be confused with upload time).
     * @param resourceState the state of this resource.
     * @param contentInfo if resource state hasContent() is true then this is the content info.
     *            Otherwise it should be null.
     */
    private GetResourceInfoResult(String resourceId, Date creationTime,
        ResourceState resourceState, @Nullable UploadedResourceContentInfo contentInfo) {
        this.resourceId = checkNotNull(resourceId);
        this.creationTime = checkNotNull(creationTime);
        this.resourceState = checkNotNull(resourceState);
        this.contentInfo = contentInfo;

        // Has content only IFF state say so.
        checkArgument((resourceState.hasContent()) == (contentInfo != null), "State: %s",
                resourceState);
    }

    public String getResourceId() {
        return resourceId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public ResourceState getResourceState() {
        return resourceState;
    }

    public boolean hasContent() {
        return resourceState.hasContent();
    }

    /**
     * Asserts that {@link #hasContent()} is true.
     */
    public UploadedResourceContentInfo getContentInfo() {
        checkState(hasContent(), "Resource has no content");
        return contentInfo;
    }

    public static GetResourceInfoResult newPendingUploac(String resourceId, Date creationTime) {
        return new GetResourceInfoResult(resourceId, creationTime, ResourceState.PENDING_UPLOAD,
            null);
    }

    public static GetResourceInfoResult newUploaded(String resourceId, Date creationTime,
            UploadedResourceContentInfo contentInfo) {
        return new GetResourceInfoResult(resourceId, creationTime, ResourceState.UPLOADED,
            checkNotNull(contentInfo));
    }
}
