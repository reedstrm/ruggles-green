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
import java.util.Date;

import com.google.common.base.Preconditions;

/**
 * Represents the info of an uploaded resource info.
 * 
 * @author Tal Dayan
 * 
 */
public class UploadedResourceContentInfo {

    private final String contentType;

    private final Long contentSize;

    private final Date contentUploadTime;

    // TODO(tal): any privacy issue with exposing this value?
    private final String contentOriginalFileName;

    public UploadedResourceContentInfo(String contentType, Long contentSize,
        Date contentUploadTime, String contentOriginalFileName) {
        this.contentType = Preconditions.checkNotNull(contentType);
        this.contentSize = Preconditions.checkNotNull(contentSize);
        this.contentUploadTime = Preconditions.checkNotNull(contentUploadTime);
        this.contentOriginalFileName = Preconditions.checkNotNull(contentOriginalFileName);
    }

    public String getContentType() {
        return contentType;
    }

    public Long getContentSize() {
        return contentSize;
    }

    public Date getContentUploadTime() {
        return contentUploadTime;
    }

    public String getContentOriginalFileName() {
        return contentOriginalFileName;
    }
}
