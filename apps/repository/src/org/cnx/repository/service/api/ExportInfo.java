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
 * Immutable class with summary info of an export instance.
 * 
 * @author Tal Dayan
 */
public class ExportInfo {
    private final ExportType exportType;

    private final Date creationTime;

    private final long size;

    private final String md5Hash;

    public ExportInfo(ExportType exportType, Date creationTime, long size, String md5Hash) {
        this.exportType = checkNotNull(exportType);
        this.creationTime = checkNotNull(creationTime);
        this.size = size;
        this.md5Hash = checkNotNull(md5Hash);
    }

    public ExportType getExportType() {
        return exportType;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public long getSize() {
        return size;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    @Override
    public String toString() {
        return String.format("[id: %s, time: %s, size: %s, hash: %s]", exportType.getId(),
                creationTime, size, md5Hash);
    }
}
