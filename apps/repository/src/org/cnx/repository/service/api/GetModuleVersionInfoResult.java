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

import java.util.Date;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Result of a successful getModuleVersionInfo() operation.
 * 
 * @author Tal Dayan
 */
public class GetModuleVersionInfoResult {
    private final String moduleId;
    private final int versionNumber;
    private final Date creationTime;
    private final ImmutableList<ExportInfo> exports;

    public GetModuleVersionInfoResult(String moduleId, int versionNumber, Date creationTime,
            List<ExportInfo> exports) {
        checkArgument(versionNumber >= 1, "Invalid module version number: %s", versionNumber);
        this.moduleId = checkNotNull(moduleId);
        this.versionNumber = versionNumber;
        this.creationTime = checkNotNull(creationTime);
        this.exports = ImmutableList.copyOf(checkNotNull(exports));
    }

    public String getModuleId() {
        return moduleId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public ImmutableList<ExportInfo> getExports() {
        return exports;
    }
}
