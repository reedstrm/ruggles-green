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
 * Immutable class with summary info of module instance.
 * 
 * @author Tal Dayan
 */
public class ModuleInfo {
    private final String moduleId;

    private final int versionCount;

    public ModuleInfo(String moduleId, int versionCount) {
        this.moduleId = checkNotNull(moduleId);
        this.versionCount = versionCount;
    }

    public String getModuleId() {
        return moduleId;
    }

    public int getVersionCount() {
        return versionCount;
    }
}
