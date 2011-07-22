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

package org.cnx.repository.service.api;

//import org.cnx.util.Assertions;
import org.cnx.util.Nullable;

import com.google.common.base.Preconditions;

/**
 * Result of a successful GetResourceInfo operation.
 * 
 * @author Tal Dayan
 * 
 */
public class GetResourceInfoResult {
    enum ResourceState {
        PENDING_UPLOAD, UPLOADED;

        // TODO(tal): better name?
        public boolean hasContent() {
            return this != PENDING_UPLOAD;
        }
    }

    final ResourceState resourceState;

    /**
     * Content info. Available only when {@link #hasContent()} is true;
     */
    @Nullable
    final UploadedResourceContentInfo contentInfo;

    private GetResourceInfoResult(ResourceState resourceState,
        UploadedResourceContentInfo contentInfo) {
        this.resourceState = resourceState;
        this.contentInfo = contentInfo;
    }

    public boolean hasContent() {
        return resourceState.hasContent();
    }

    public ResourceState getResourceState() {
        return resourceState;
    }

    /**
     * Asserts that {@link #hasContent()} is true.
     */
    public UploadedResourceContentInfo getContentInfo() {
        Preconditions.checkState(hasContent(), "Resource has no content");
        return contentInfo;
    }

    public static GetResourceInfoResult newPendingUploac() {
        return new GetResourceInfoResult(ResourceState.PENDING_UPLOAD, null);
    }

    public static GetResourceInfoResult newUploaded(UploadedResourceContentInfo contentInfo) {
        return new GetResourceInfoResult(ResourceState.UPLOADED,
            Preconditions.checkNotNull(contentInfo));
    }
}
