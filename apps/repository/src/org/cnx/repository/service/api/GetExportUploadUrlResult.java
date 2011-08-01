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
 * Result of a successful getExportUploadUrl
 * 
 * @author Tal Dayan
 */
public class GetExportUploadUrlResult {

    private final String exportUploadUrl;

    private final String expectedContentType;

    /**
     * @param exportUploadUrl an absolute URL to which the export content should be posted.
     * @param expectedContentType the expected content type of the uploaded export. Any other
     *            content will cause the post to be rejected.
     */
    public GetExportUploadUrlResult(String exportUploadUrl, String expectedContentType) {
        this.exportUploadUrl = checkNotNull(exportUploadUrl);
        this.expectedContentType = checkNotNull(expectedContentType);
    }

    public String getExportUploadUrl() {
        return exportUploadUrl;
    }

    public String getExpectedContentType() {
        return expectedContentType;
    }
}
