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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Result of a successful resource creation operation.
 * 
 * @author Tal Dayan
 */
public class AddResourceResult {

    private final String resourceId;
    private final String resourceUploadUrl;

    /**
     * @param resourceId the id of the new resource. This is a web safe string that is unique among
     *            all the resource IDs.
     * @param resourceUploadUrl a full URL to which the resource content should be uploaded.
     */
    public AddResourceResult(String resourceId, String resourceUploadUrl) {
        this.resourceId = checkNotNull(resourceId);
        this.resourceUploadUrl = checkNotNull(resourceUploadUrl);
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceUploadUrl() {
        return resourceUploadUrl;
    }
}
