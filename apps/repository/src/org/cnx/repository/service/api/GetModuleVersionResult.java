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
 * Result of a successful getModuleVersion() operation.
 * 
 * @author Tal Dayan
 */
public class GetModuleVersionResult {
    private final String moduleId;
    private final int versionNumber;
    private final String cnxmlDoc;
    private final String resourceMapDoc;

    /**
     * @param moduleId the mdoule id
     * @param versionNumber the version number of the returned version (1 is first, 2 is second,
     *            etc).
     * @param cnxmlDoc the CNXML (XML) doc of this version.
     * @param resourceMapDoc the resource map (XML) doc of this version
     */
    public GetModuleVersionResult(String moduleId, int versionNumber, String cnxmlDoc,
        String resourceMapDoc) {
        this.moduleId = checkNotNull(moduleId);
        this.versionNumber = versionNumber;
        this.cnxmlDoc = checkNotNull(cnxmlDoc);
        this.resourceMapDoc = checkNotNull(resourceMapDoc);
    }

    public String getModuleId() {
        return moduleId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getCnxmlDoc() {
        return cnxmlDoc;
    }

    public String getResourceMapDoc() {
        return resourceMapDoc;
    }
}
