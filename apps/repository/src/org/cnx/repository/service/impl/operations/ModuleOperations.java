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
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.cnx.repository.service.api.AddModuleVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateModuleResult;
import org.cnx.repository.service.api.ExportInfo;
import org.cnx.repository.service.api.GetModuleInfoResult;
import org.cnx.repository.service.api.GetModuleVersionInfoResult;
import org.cnx.repository.service.api.GetModuleVersionResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.impl.persistence.OrmModuleEntity;
import org.cnx.repository.service.impl.persistence.OrmModuleVersionEntity;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;

/**
 * Implementation of the module related operations of the repository service.
 * 
 * @author Tal Dayan
 */
public class ModuleOperations {
    private static final Logger log = Logger.getLogger(ModuleOperations.class.getName());

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<CreateModuleResult> createModule(
            RepositoryRequestContext context) {
        final String moduleId;

        try {
            final OrmModuleEntity moduleEntity = new OrmModuleEntity();
            // The unique module id is created the first time the entity is persisted.
            Services.persistence.write(moduleEntity);
            moduleId = checkNotNull(moduleEntity.getId(), "Null module id");
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error when trying to create a new module", log, e);
        }

        return ResponseUtil.loggedOk("New module created: " + moduleId, new CreateModuleResult(
            moduleId), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetModuleInfoResult> getModuleInfo(
            RepositoryRequestContext context, String moduleId) {
        final Key moduleKey = OrmModuleEntity.moduleIdToKey(moduleId);
        if (moduleKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Module id has invalid format: " + moduleId, log);
        }

        final OrmModuleEntity moduleEntity;
        final List<ExportInfo> exports;

        // We read the module entity and its child export entities in one transation. This is
        // OK since they are in the same entity group.
        final Transaction tx = Services.persistence.beginTransaction();

        try {
            // Read module
            try {
                moduleEntity = Services.persistence.read(OrmModuleEntity.class, moduleKey);
            } catch (EntityNotFoundException e) {
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                        "Could not find module " + moduleId, log, e);
            }
            // Read exports
            exports = ExportUtil.fetchParentEportInfoList(Services.persistence, moduleKey);

            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error while fetching info of module " + moduleId, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active: %s", moduleId);
        }

        return ResponseUtil.loggedOk("Retrieved info of module " + moduleId,
                new GetModuleInfoResult(moduleId, moduleEntity.getVersionCount(), exports), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<AddModuleVersionResult> addModuleVersion(
            RepositoryRequestContext context, String moduleId, String cnxmlDoc,
            String resourceMapDoc) {

        final Key moduleKey = OrmModuleEntity.moduleIdToKey(moduleId);
        if (moduleKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Cannot add module version, module id has bad format: [" + moduleId + "]", log);
        }

        // We read the module entity and its child export entities in one transation. This is
        // OK since they are in the same entity group.

        final int newVersionNumber;
        final Transaction tx = checkNotNull(Services.persistence.beginTransaction());

        try {
            // Read parent module entity of this module version
            final OrmModuleEntity moduleEntity;
            try {
                moduleEntity = Services.persistence.read(OrmModuleEntity.class, moduleKey);
            } catch (EntityNotFoundException e) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                        "Cannot add module version, module not found: " + moduleId, log, e);
            }

            // Updated number of versions in the module entity
            newVersionNumber = moduleEntity.incrementVersionCount();
            Services.persistence.write(moduleEntity);

            // Create new version entity
            final OrmModuleVersionEntity versionEntity =
                new OrmModuleVersionEntity(moduleKey, newVersionNumber, cnxmlDoc, resourceMapDoc);

            // TODO(tal): If a module version with this key already exists (due to data
            // inconsistency), return an error rather than overwriting it.

            Services.persistence.write(versionEntity);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error while trying to add a version to module " + moduleId, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active: %s", moduleId);
        }

        // All done OK.
        return ResponseUtil.loggedOk("Added module version " + moduleId + "/" + newVersionNumber,
                new AddModuleVersionResult(moduleId, newVersionNumber), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetModuleVersionResult> getModuleVersion(
            RepositoryRequestContext context, String moduleId, @Nullable Integer moduleVersion) {

        if (moduleVersion != null && moduleVersion < 1) {
            ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST, "Illegal module version number "
                + moduleVersion, log);
        }

        final Key moduleKey = OrmModuleEntity.moduleIdToKey(moduleId);
        if (moduleKey == null) {
            ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST, "Module id has bad format: ["
                + moduleId + "]", log);
        }

        // final DatastoreService datastore = Services.ormDatastore;
        final int versionToServe;
        final OrmModuleVersionEntity versionEntity;
        try {
            // Determine module version to serve. If 'latest' than read module entity and
            // determine latest version.
            //
            // NOTE(tal): we don't need to use a transaction for the module lookup and the
            // version entity since versions are never deleted and version count is monotonic.
            //
            if (moduleVersion == null) {
                // Handle the case of 'latest'
                final OrmModuleEntity moduleEntity;
                try {
                    moduleEntity = Services.persistence.read(OrmModuleEntity.class, moduleKey);
                } catch (EntityNotFoundException e) {
                    return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                            "Could not locate module " + moduleId, log);
                }
                // If module has no versions than there is not latest version.
                if (moduleEntity.getVersionCount() < 1) {
                    ResponseUtil.loggedError(RepositoryStatus.STATE_MISMATCH,
                            "Module has no versions: " + moduleId, log);
                }
                versionToServe = moduleEntity.getVersionCount();
            } else {
                // Handle the case of a pinned version
                versionToServe = moduleVersion;
            }

            // Fetch module version entity
            final Key moduleVersionKey =
                OrmModuleVersionEntity.moduleVersionKey(moduleKey, versionToServe);

            versionEntity =
                Services.persistence.read(OrmModuleVersionEntity.class, moduleVersionKey);
            checkState(versionEntity.getVersionNumber() == versionToServe,
                    "Inconsistent version in module %s, expected %s found %s", moduleId,
                    versionToServe, versionEntity.getVersionNumber());
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error while looking module version " + moduleId + "/" + moduleVersion, log, e);
        }

        final GetModuleVersionResult result =
            new GetModuleVersionResult(moduleId, versionEntity.getVersionNumber(),
                versionEntity.getCNXMLDoc(), versionEntity.getResourceMapDoc());
        return ResponseUtil.loggedOk("Fetched module version", result, log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetModuleVersionInfoResult> getModuleVersionInfo(
            RepositoryRequestContext context, String moduleId, @Nullable Integer moduleVersion) {

        if (moduleVersion != null && moduleVersion < 1) {
            ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST, "Illegal module version number "
                + moduleVersion, log);
        }

        final Key moduleKey = OrmModuleEntity.moduleIdToKey(moduleId);
        if (moduleKey == null) {
            ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST, "Module id has bad format: ["
                + moduleId + "]", log);
        }

        // final DatastoreService datastore = Services.ormDatastore;
        final int versionToServe;
        final List<ExportInfo> exports;
        final OrmModuleVersionEntity versionEntity;
        final Transaction tx = Services.persistence.beginTransaction();

        try {
            // TODO(tal): refactor this and share with other operations in this file.
            //
            // Determine module version to serve. If 'latest' than read module entity and
            // determine latest version.
            //
            // NOTE(tal): we don't need to use a transaction for the module lookup and the
            // version entity since versions are never deleted and version count is monotonic.
            //
            if (moduleVersion == null) {
                final OrmModuleEntity moduleEntity;
                try {
                    moduleEntity = Services.persistence.read(OrmModuleEntity.class, moduleKey);
                } catch (EntityNotFoundException e) {
                    return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                            "Could not locate module " + moduleId, log);
                }
                // If module has no versions than there is not latest version.
                if (moduleEntity.getVersionCount() < 1) {
                    ResponseUtil.loggedError(RepositoryStatus.STATE_MISMATCH,
                            "Module has no versions: " + moduleId, log);
                }
                versionToServe = moduleEntity.getVersionCount();
            } else {
                versionToServe = moduleVersion;
            }

            // Fetch the module version entity
            final Key moduleVersionKey =
                OrmModuleVersionEntity.moduleVersionKey(moduleKey, versionToServe);

            versionEntity =
                Services.persistence.read(OrmModuleVersionEntity.class, moduleVersionKey);
            checkState(versionEntity.getVersionNumber() == versionToServe,
                    "Inconsistent version in module %s, expected %s found %s", moduleId,
                    versionToServe, versionEntity.getVersionNumber());

            // Fetch the module version child exports
            exports = ExportUtil.fetchParentEportInfoList(Services.persistence, moduleVersionKey);

            tx.commit();

        } catch (Throwable e) {
            tx.rollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error while looking module version " + moduleId + "/" + moduleVersion, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active: %s/%s", moduleId, moduleVersion);
        }

        final GetModuleVersionInfoResult result =
            new GetModuleVersionInfoResult(moduleId, versionEntity.getVersionNumber(), exports);
        return ResponseUtil.loggedOk("Fetched module version info", result, log);
    }
}
