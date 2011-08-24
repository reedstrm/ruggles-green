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

import java.util.Date;

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

    private final String contentOriginalFileName;

    private final long size;

    private final String md5Hash;

    public UploadedResourceContentInfo(String contentType, Long contentSize,
            Date contentUploadTime, String contentOriginalFileName, long size, String md5Hash) {
        this.contentType = checkNotNull(contentType);
        this.contentSize = checkNotNull(contentSize);
        this.contentUploadTime = checkNotNull(contentUploadTime);
        this.contentOriginalFileName = checkNotNull(contentOriginalFileName);
        this.size = size;
        this.md5Hash = checkNotNull(md5Hash);
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

    public long getSize() {
        return size;
    }

    public String getMd5Hash() {
        return md5Hash;
    }
}
