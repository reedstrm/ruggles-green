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

package org.cnx.repository.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoModuleEntity;
import org.cnx.repository.schema.JdoModuleVersionEntity;
import org.cnx.repository.schema.SchemaConsts;
import org.cnx.repository.service.api.AddModuleVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateModuleResult;
import org.cnx.repository.service.api.GetModuleInfoResult;
import org.cnx.repository.service.api.GetModuleVersionResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.util.Nullable;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Implementation of the module related operations of the repository service.
 * 
 * @author Tal Dayan
 */
public class ModuleOperations {

    /**
     * See description in {@link CnxRepositoryService}
     */
    static RepositoryResponse<CreateModuleResult> createModule(RepositoryRequestContext context) {
        final Long moduleIdLong;
        final PersistenceManager pm = Services.datastore.getPersistenceManager();

        try {
            final JdoModuleEntity entity = new JdoModuleEntity();
            // The unique module id is created the first time the entity is
            // persisted.
            pm.makePersistent(entity);
            moduleIdLong = checkNotNull(entity.getId(), "Null module id");
        } catch (Throwable e) {
            e.printStackTrace();
            return RepositoryResponse.newError(RepositoryStatus.SERVER_ERRROR,
                "Error when trying to create a new module: " + e.getMessage());
        } finally {
            pm.close();
        }

        final String moduleId = JdoModuleEntity.moduleIdToString(moduleIdLong);
        return RepositoryResponse.newOk("New module created", new CreateModuleResult(moduleId));
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    static RepositoryResponse<AddModuleVersionResult> addModuleVersion(
        RepositoryRequestContext context, String moduleId, String cnxmlDoc, String resourceMapDoc) {

        final Long moduleIdLong = JdoModuleEntity.stringToModuleId(moduleId);

        if (moduleIdLong == null) {
            return RepositoryResponse.newError(RepositoryStatus.BAD_REQUEST,
                "Module id has bad format: [" + moduleId + "]");
        }

        final PersistenceManager pm = Services.datastore.getPersistenceManager();
        final Transaction tx = pm.currentTransaction();

        final int newVersionNumber;

        try {
            tx.begin();

            // Read parent entity of this module
            final JdoModuleEntity moduleEntity;
            try {
                moduleEntity = pm.getObjectById(JdoModuleEntity.class, moduleId);
            } catch (Throwable e) {
                tx.rollback();
                return RepositoryResponse.newError(RepositoryStatus.NOT_FOUND,
                    "Could not find module " + moduleId + " (" + e.getMessage() + ")");
            }

            // Updated number of versions in the module entity
            newVersionNumber = moduleEntity.incrementVersionCount();

            // Create child key for the module version entity
            final Key parentKey =
                KeyFactory.createKey(SchemaConsts.MODULE_KEY_KIND, moduleEntity.getId());
            final Key childKey =
                KeyFactory.createKey(parentKey, SchemaConsts.MODULE_VERSION_KEY_KIND,
                    newVersionNumber);

            // TODO(tal): If version already exists due to internal
            // inconsistency, report and error rather than overwriting.

            // Create new version entity
            final JdoModuleVersionEntity versionEntity =
                new JdoModuleVersionEntity(childKey, moduleIdLong, newVersionNumber, cnxmlDoc,
                    resourceMapDoc);
            pm.makePersistent(versionEntity);

            tx.commit();
        } catch (Throwable e) {
            e.printStackTrace();
            tx.rollback();
            return RepositoryResponse.newError(RepositoryStatus.SERVER_ERRROR,
                "Error while adding module version: " + e.getMessage());
        } finally {
            checkState(!tx.isActive(), "Transaction remained active");
            pm.close();
        }

        // All done OK.
        return RepositoryResponse.newOk(
            "Added module version " + moduleId + "/" + newVersionNumber,
            new AddModuleVersionResult(moduleId, newVersionNumber));
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    static RepositoryResponse<GetModuleVersionResult> getModuleVersion(
        RepositoryRequestContext context, String moduleId, @Nullable Integer moduleVersion) {

        if (moduleVersion != null && moduleVersion < 1) {
            return RepositoryResponse.newError(RepositoryStatus.BAD_REQUEST,
                "Illegal module version number " + moduleVersion);
        }

        final Long moduleIdLong = JdoModuleEntity.stringToModuleId(moduleId);
        if (moduleIdLong == null) {
            return RepositoryResponse.newError(RepositoryStatus.BAD_REQUEST,
                "Module id has bad format: [" + moduleId + "]");
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
                    moduleEntity = pm.getObjectById(JdoModuleEntity.class, moduleId);
                } catch (Throwable e) {
                    e.printStackTrace();
                    return RepositoryResponse.newError(RepositoryStatus.NOT_FOUND,
                        "Could not locate module " + moduleId);
                }
                // If module has no versions than there is not latest version.
                if (moduleEntity.getVersionCount() < 1) {
                    return RepositoryResponse.newError(RepositoryStatus.STATE_MISMATCH,
                        "Module has no versions: " + moduleId);
                }
                versionToServe = moduleEntity.getVersionCount();
            } else {
                versionToServe = moduleVersion;
            }

            // Fetch module version entity
            //
            // TODO(tal) refactor and share this with other module servlets.
            final Key parentKey = KeyFactory.createKey(SchemaConsts.MODULE_KEY_KIND, moduleId);
            final Key childKey =
                KeyFactory.createKey(parentKey, SchemaConsts.MODULE_VERSION_KEY_KIND,
                    versionToServe);

            try {
                versionEntity = pm.getObjectById(JdoModuleVersionEntity.class, childKey);
                checkState(versionEntity.getVersionNumber() == versionToServe,
                    "Inconsistent version in module %s, expected %s found %s", moduleId,
                    versionToServe, versionEntity.getVersionNumber());
            } catch (Throwable e) {
                return RepositoryResponse.newError(RepositoryStatus.SERVER_ERRROR,
                    "Error while looking module version " + moduleId + "/" + versionToServe + ": "
                        + e.getMessage());
            }
        } finally {
            pm.close();
        }

        return RepositoryResponse.newOk("Fetched module version", new GetModuleVersionResult(
            moduleId, versionEntity.getVersionNumber(), versionEntity.getCNXMLDoc(), versionEntity
                .getManifestDoc()));
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    static RepositoryResponse<GetModuleInfoResult> getModuleInfo(RepositoryRequestContext context,
        String moduleId) {
        final Long moduleIdLong = JdoModuleEntity.stringToModuleId(moduleId);
        if (moduleIdLong == null) {
            return RepositoryResponse.newError(RepositoryStatus.BAD_REQUEST,
                "Module id has invalid format: " + moduleId);
        }

        final PersistenceManager pm = Services.datastore.getPersistenceManager();
        final JdoModuleEntity moduleEntity;
        try {
            moduleEntity = pm.getObjectById(JdoModuleEntity.class, moduleId);
        } catch (Throwable e) {
            return RepositoryResponse.newError(RepositoryStatus.NOT_FOUND, "Could not find module "
                + moduleId + " (" + e.getMessage() + ")");
        } finally {
            pm.close();
        }

        return RepositoryResponse.newOk("Module found", new GetModuleInfoResult(moduleId,
            moduleEntity.getVersionCount()));
    }
}
