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

import javax.annotation.Nullable;

/**
 * Represents an immutable reference to an export.
 * 
 * @author Tal Dayan
 * 
 */
public class ExportReference {

    private final ExportScopeType scopeType;

    private final String objectId;

    @Nullable
    private final Integer versionNumber;

    private final String exportTypeId;

    /**
     * TODO(tal): make the notion of 'latest version' more explicit? Currently we overload the null
     * value.
     * 
     * @param scopeType The type of the object to which the export can be attached.
     * @param objectId The id of the object to which this object is attached. The interpretation of
     *            this id depends on scopeType. For example, if scope type is MODULE or
     *            MODULE_VERSION than this is a module id.
     * @param versionNumber If the scope type is version than this is the version id or null to
     *            refer to the latest version. If scope type is not version, this should be null.
     * @export exportTypeId the export type id. Must match one of the ids returned by
     *         {@link CnxRepositoryService#getExportTypes()}.
     */
    public ExportReference(ExportScopeType scopeType, String objectId,
        @Nullable Integer versionNumber, String exportTypeId) {
        this.scopeType = checkNotNull(scopeType);
        this.objectId = checkNotNull(objectId);
        this.versionNumber = versionNumber;
        this.exportTypeId = checkNotNull(exportTypeId);

        checkArgument(scopeType.isVersion() || versionNumber == null,
                "Non null version number for non version scope: %s", scopeType);
    }

    public ExportScopeType getScopeType() {
        return scopeType;
    }

    public String getObjectId() {
        return objectId;
    }

    @Nullable
    public Integer getVersionNumber() {
        return versionNumber;
    }

    public String getExportTypeId() {
        return exportTypeId;
    }

    @Override
    public String toString() {
        return String.format("scope=%s, id=%s, version=%s, type=%s", scopeType, objectId,
                versionNumber, exportTypeId);
    }
}
