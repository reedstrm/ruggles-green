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

import com.google.common.base.Preconditions;

public class CreateResourceResult {

    private final String resourceId;
    private final String resourceUploadUrl;

    /**
     * Results of a successful resource creation operation.
     * 
     * @param resourceId the id of the new resource. This is a thread safe string that is unique
     *            among all the resource IDs.
     * @param resourceUploadUrl a full URL to which the resource content should be uploaded.
     */
    public CreateResourceResult(String resourceId, String resourceUploadUrl) {
        this.resourceId = Preconditions.checkNotNull(resourceId);
        this.resourceUploadUrl = Preconditions.checkNotNull(resourceUploadUrl);
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceUploadUrl() {
        return resourceUploadUrl;
    }
}
