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

package org.cnx.repository.service.impl.operations;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import org.cnx.repository.service.api.ExportReference;
import org.cnx.repository.service.api.ExportType;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.impl.persistence.OrmCollectionEntity;
import org.cnx.repository.service.impl.persistence.OrmCollectionVersionEntity;
import org.cnx.repository.service.impl.persistence.OrmExportItemEntity;
import org.cnx.repository.service.impl.persistence.OrmModuleEntity;
import org.cnx.repository.service.impl.persistence.OrmModuleVersionEntity;

import com.google.appengine.api.datastore.Key;

/**
 * Result of validating an export reference.
 * 
 * @author Tal Dayan
 */
public class ExportReferenceValidationResult {
    private final RepositoryStatus repositoryStatus;
    private final String statusDescription;

    /**
     * The matching export type. Null IFF status is error.
     */
    @Nullable
    private final ExportType exportType;

    /**
     * The class of the parent entity of the export.
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    private final Class parentEntityClass;

    /**
     * The key of the parent entity of the export.
     * 
     * This is a computed value. The existence of the entity is not verified yet.
     */
    @Nullable
    private final Key parentKey;

    /**
     * The export entity key.
     * 
     * This is a computed value. The existence of the entity is not verified yet.
     */
    @Nullable
    private final Key exportKey;

    /**
     * Constructor for errors.
     * 
     * The param repositoryStatus must have isError() true.
     */
    private ExportReferenceValidationResult(RepositoryStatus repositoryStatus,
        String statusDescription) {
        this.repositoryStatus = checkNotNull(repositoryStatus);
        checkArgument(repositoryStatus.isError(), "Not an error");
        this.statusDescription = checkNotNull(statusDescription);
        this.exportType = null;
        this.parentEntityClass = null;
        this.parentKey = null;
        this.exportKey = null;
    }

    /**
     * Constructor for OK.
     */
    private ExportReferenceValidationResult(RepositoryStatus repositoryStatus,
        String statusDescription, ExportType exportType,
        @SuppressWarnings("rawtypes") Class parentEntityClass, Key parentKey, Key exportKey) {
        this.repositoryStatus = checkNotNull(repositoryStatus);
        checkArgument(repositoryStatus.isOk(), "Not an ok: %s", repositoryStatus);
        this.statusDescription = checkNotNull(statusDescription);
        this.exportType = checkNotNull(exportType);
        this.parentEntityClass = checkNotNull(parentEntityClass);
        this.parentKey = checkNotNull(parentKey);
        this.exportKey = checkNotNull(exportKey);
    }

    public RepositoryStatus getRepositoryStatus() {
        return repositoryStatus;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public ExportType getExportType() {
        return exportType;
    }

    @SuppressWarnings("rawtypes")
    public Class getParentEntityClass() {
        return parentEntityClass;
    }

    public Key getParentKey() {
        return parentKey;
    }

    public Key getExportKey() {
        return exportKey;
    }

    /**
     * Validate an export reference.
     */
    public static ExportReferenceValidationResult
            validateReference(ExportReference exportReference) {
        // Lookup export type by id.
        final ExportType exportType =
            Services.config.getExportTypes().get(exportReference.getExportTypeId());
        if (exportType == null) {
            return new ExportReferenceValidationResult(RepositoryStatus.BAD_REQUEST,
                "Unknown exporty type id: " + exportReference.getExportTypeId());
        }

        // Make sure no version number for non version scope.
        if (!exportReference.getScopeType().isVersion()
            && exportReference.getVersionNumber() != null) {
            return new ExportReferenceValidationResult(RepositoryStatus.BAD_REQUEST,
                "Found version number " + exportReference.getVersionNumber()
                    + " for non version export scope: " + exportReference.getScopeType());
        }

        // Version number should not be < 1.
        if (exportReference.getVersionNumber() != null && exportReference.getVersionNumber() < 1) {
            return new ExportReferenceValidationResult(RepositoryStatus.BAD_REQUEST,
                "Invalid version number: " + exportReference.getVersionNumber());
        }

        // Make sure export type is allowed for specified scope.
        if (!exportType.getAllowedScopeTypes().contains(exportReference.getScopeType())) {
            return new ExportReferenceValidationResult(RepositoryStatus.BAD_REQUEST, "Export type "
                + exportReference.getExportTypeId() + " not allowed for "
                + exportReference.getScopeType());
        }

        // Validate the object id format. The expected format depends on the scope.
        @SuppressWarnings("rawtypes")
        final Class parentEntityClass;
        final Key parentKey;
        switch (exportReference.getScopeType()) {
            case MODULE_VERSION:
            case MODULE:
                final Key moduleKey = OrmModuleEntity.moduleIdToKey(exportReference.getObjectId());
                if (moduleKey == null) {
                    return new ExportReferenceValidationResult(RepositoryStatus.BAD_REQUEST,
                        "Module id has bad format: " + exportReference.getObjectId());
                }
                if (exportReference.getScopeType().isVersion()) {
                    parentEntityClass = OrmModuleVersionEntity.class;
                    parentKey =
                        OrmModuleVersionEntity.moduleVersionKey(moduleKey,
                                exportReference.getVersionNumber());
                } else {
                    parentEntityClass = OrmModuleEntity.class;
                    parentKey = moduleKey;
                }
                break;
            case COLLECTION_VERSION:
            case COLLECTION:
                final Key collectionKey =
                    OrmCollectionEntity.collectionIdToKey(exportReference.getObjectId());
                if (collectionKey == null) {
                    return new ExportReferenceValidationResult(RepositoryStatus.BAD_REQUEST,
                        "Collection id has bad format: " + exportReference.getObjectId());
                }
                if (exportReference.getScopeType().isVersion()) {
                    parentEntityClass = OrmCollectionVersionEntity.class;
                    parentKey =
                        OrmCollectionVersionEntity.collectionVersionKey(collectionKey,
                                exportReference.getVersionNumber());
                } else {
                    parentEntityClass = OrmCollectionEntity.class;
                    parentKey = collectionKey;
                }
                break;
            default:
                return new ExportReferenceValidationResult(RepositoryStatus.SERVER_ERRROR,
                    "Unknown export reference scope type: " + exportReference.getScopeType());
        }

        final Key exportKey = OrmExportItemEntity.exportEntityKey(parentKey, exportType);

        // All is OK.
        return new ExportReferenceValidationResult(RepositoryStatus.OK, "ok", exportType,
            parentEntityClass, parentKey, exportKey);
    }
}
