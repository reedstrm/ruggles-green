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

package org.cnx.repository.service.impl.operations;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.cnx.repository.service.api.AddModuleVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateModuleResult;
import org.cnx.repository.service.api.GetModuleInfoResult;
import org.cnx.repository.service.api.GetModuleVersionResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.impl.schema.JdoModuleEntity;
import org.cnx.repository.service.impl.schema.JdoModuleVersionEntity;
import org.cnx.util.Nullable;

import com.google.appengine.api.datastore.Key;

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
        final PersistenceManager pm = Services.datastore.getPersistenceManager();

        try {
            final JdoModuleEntity entity = new JdoModuleEntity();
            // The unique module id is created the first time the entity is persisted.
            pm.makePersistent(entity);
            moduleId = checkNotNull(entity.getModuleId(), "Null module id");
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                "Error when trying to create a new module", log, Level.SEVERE, e);
        } finally {
            pm.close();
        }

        return ResponseUtil.loggedOk("New module created: " + moduleId, new CreateModuleResult(
            moduleId), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<AddModuleVersionResult> addModuleVersion(
        RepositoryRequestContext context, String moduleId, String cnxmlDoc, String resourceMapDoc) {

        final Key moduleKey = JdoModuleEntity.moduleIdToKey(moduleId);
        if (moduleKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                "Cannot add module version, module id has bad format: [" + moduleId + "]", log,
                Level.WARNING);
        }

        final PersistenceManager pm = Services.datastore.getPersistenceManager();
        final Transaction tx = pm.currentTransaction();
        final int newVersionNumber;
        try {
            tx.begin();

            // Read parent entity of this module
            final JdoModuleEntity moduleEntity;
            try {
                moduleEntity = pm.getObjectById(JdoModuleEntity.class, moduleKey);
            } catch (Throwable e) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                    "Cannot add module version, module not found: " + moduleId, log, Level.WARNING,
                    e);
            }

            // Updated number of versions in the module entity
            newVersionNumber = moduleEntity.incrementVersionCount();

            // Create new version entity
            final JdoModuleVersionEntity versionEntity =
                new JdoModuleVersionEntity(moduleKey, newVersionNumber, cnxmlDoc, resourceMapDoc);

            // TODO(tal): If a module version with this key already exists (due to data
            // inconsistency), return an error rather than overwriting it.

            pm.makePersistent(versionEntity);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                "Error while trying to add a version to module " + moduleId, log, Level.SEVERE, e);
        } finally {
            if (tx.isActive()) {
                log.severe("Transaction left opened when adding module version:  " + moduleId);
                tx.rollback();
            }
            pm.close();
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
                + moduleVersion, log, Level.WARNING);
        }

        final Key moduleKey = JdoModuleEntity.moduleIdToKey(moduleId);
        if (moduleKey == null) {
            ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST, "Module id has bad format: ["
                + moduleId + "]", log, Level.WARNING);
        }

        PersistenceManager pm = Services.datastore.getPersistenceManager();

        final int versionToServe;
        final JdoModuleVersionEntity versionEntity;
        try {
            // Determine module version to serve. If 'latest' than read module entity and
            // determine latest version.
            //
            // NOTE(tal): we don't need to use a transaction for the module lookup and the
            // version entity since versions are never deleted and version count is monotonic.
            //
            if (moduleVersion == null) {
                final JdoModuleEntity moduleEntity;
                try {
                    moduleEntity = pm.getObjectById(JdoModuleEntity.class, moduleKey);
                } catch (Throwable e) {
                    return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                        "Could not locate module " + moduleId, log, Level.WARNING);
                }
                // If module has no versions than there is not latest version.
                if (moduleEntity.getVersionCount() < 1) {
                    ResponseUtil.loggedError(RepositoryStatus.STATE_MISMATCH,
                        "Module has no versions: " + moduleId, log, Level.WARNING);
                }
                versionToServe = moduleEntity.getVersionCount();
            } else {
                versionToServe = moduleVersion;
            }

            // Fetch module version entity
            final Key moduleVersionKey =
                JdoModuleVersionEntity.moduleVersionKey(moduleKey, versionToServe);
            try {
                versionEntity = pm.getObjectById(JdoModuleVersionEntity.class, moduleVersionKey);
                checkState(versionEntity.getVersionNumber() == versionToServe,
                    "Inconsistent version in module %s, expected %s found %s", moduleId,
                    versionToServe, versionEntity.getVersionNumber());
            } catch (Throwable e) {
                return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error while looking module version " + moduleId + "/" + versionToServe, log,
                    Level.SEVERE, e);
            }
        } finally {
            pm.close();
        }

        final GetModuleVersionResult result =
            new GetModuleVersionResult(moduleId, versionEntity.getVersionNumber(), versionEntity
                .getCNXMLDoc(), versionEntity.getResourceMapDoc());
        return ResponseUtil.loggedOk("Fetched module version", result, log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetModuleInfoResult> getModuleInfo(
        RepositoryRequestContext context, String moduleId) {
        final Key moduleKey = JdoModuleEntity.moduleIdToKey(moduleId);
        if (moduleKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                "Module id has invalid format: " + moduleId, log, Level.WARNING);
        }

        final PersistenceManager pm = Services.datastore.getPersistenceManager();
        final JdoModuleEntity moduleEntity;
        try {
            moduleEntity = pm.getObjectById(JdoModuleEntity.class, moduleKey);
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND, "Could not find module "
                + moduleId, log, Level.WARNING, e);
        } finally {
            pm.close();
        }

        return ResponseUtil.loggedOk("Retrieved info of module " + moduleId,
            new GetModuleInfoResult(moduleId, moduleEntity.getVersionCount()), log);
    }
}
