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
 * Result of a successful getModuleInfo() operation.
 * 
 * @author Tal Dayan
 */
public class GetModuleInfoResult {
    private final String moduleId;
    private final Date creationTime;
    private final int versionCount;
    private final ImmutableList<ExportInfo> exports;

    public GetModuleInfoResult(String moduleId, Date creationTime, int versionCount, List<ExportInfo> exports) {
        checkArgument(versionCount >= 0, "Negative version count: %s", versionCount);
        this.moduleId = checkNotNull(moduleId);
        this.creationTime = checkNotNull(creationTime);
        this.versionCount = versionCount;
        this.exports = ImmutableList.copyOf(checkNotNull(exports));
    }

    public String getModuleId() {
        return moduleId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public int getVersionCount() {
        return versionCount;
    }

    public ImmutableList<ExportInfo> getExports() {
        return exports;
    }
}
