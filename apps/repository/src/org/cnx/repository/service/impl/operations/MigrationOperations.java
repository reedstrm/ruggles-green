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

import java.util.Date;
import java.util.logging.Logger;

import org.cnx.repository.service.api.AddCollectionVersionResult;
import org.cnx.repository.service.api.AddModuleVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.AddCollectionResult;
import org.cnx.repository.service.api.AddModuleResult;
import org.cnx.repository.service.api.AddResourceResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.impl.persistence.OrmCollectionEntity;
import org.cnx.repository.service.impl.persistence.OrmCollectionVersionEntity;
import org.cnx.repository.service.impl.persistence.OrmModuleEntity;
import org.cnx.repository.service.impl.persistence.OrmModuleVersionEntity;
import org.cnx.repository.service.impl.persistence.OrmResourceEntity;
import org.cnx.repository.service.impl.persistence.PersistenceMigrationUtil;
import org.cnx.repository.service.impl.persistence.PersistenceTransaction;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

/**
 * Implementation of the migration specific operations of the repository service.
 * <p>
 * TODO(tal): delete these operations after completing the migration to the app engine repository.
 * 
 * @author Tal Dayan
 */
public class MigrationOperations {

    private static final Logger log = Logger.getLogger(MigrationOperations.class.getName());

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<AddCollectionResult> addCollectionForMigration(
            RepositoryRequestContext context, String forcedId) {
        final Date transactionTime = new Date();
        final PersistenceTransaction tx = Services.persistence.beginTransaction();

        try {

            // Validate forced id
            final Key forcedKey = OrmCollectionEntity.collectionIdToKey(forcedId);
            if (forcedKey == null) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                        "Invalid forced collection id format " + forcedId, log);
            }
            if (!PersistenceMigrationUtil.isCollectionKeyProtected(forcedKey)) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.OUT_OF_RANGE,
                        "Forced collection id is not out of protected id range " + forcedId, log);
            }

            // We allow to overwrite existing entity only if it has no versions. This enables
            // retries by the migrator.
            try {
                final OrmCollectionEntity entity = Services.persistence.read(OrmCollectionEntity.class, forcedKey);
                if (entity.getVersionCount() != 0) {
                    tx.rollback();
                    return ResponseUtil.loggedError(RepositoryStatus.STATE_MISMATCH,
                            "Collection already exists and has at least one version: " + forcedId, log);
                }
                log.warning("Overwriting existing collection with zero versions: " + forcedId);
                // Fall through and override this entity
            } catch (EntityNotFoundException e){
                // Normal case, fall through and create a new entity
            }

            // Setup and save entity
            final OrmCollectionEntity entity = new OrmCollectionEntity(transactionTime);
            entity.setKey(forcedKey);
            Services.persistence.write(entity);
            checkArgument(forcedId.equals(entity.getId()), "%s vs %s", forcedId, entity.getId());
            tx.commit();

        } catch (Throwable e) {
            tx.safeRollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                    "Error when trying to create a new collection with forced id: " + forcedId,
                    log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active");
        }

        return ResponseUtil.loggedOk("New collection created: " + forcedId,
                new AddCollectionResult(forcedId), log);
    }


    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<AddCollectionVersionResult> addCollectionVersionForMigration(
            RepositoryRequestContext context, String collectionId,
            int versionNumber, String colxmlDoc) {

        if (versionNumber < 1) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Invalid version number: " + versionNumber
                    + ", should be >= 1", log);
        }

        final Key collectionKey = OrmCollectionEntity.collectionIdToKey(collectionId);
        if (collectionKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Cannot add collection version, collection id has bad format: [" + collectionId + "]", log);
        }

        if (colxmlDoc.length() > Services.config.getMaxColxmlDocSize()) {
            return ResponseUtil.loggedError(RepositoryStatus.OVERSIZE, "COLXML doc oversize, limit:"
                    + Services.config.getMaxColxmlDocSize() + ", found: " + colxmlDoc.length(), log);
        }

        final Date transactionTime = new Date();
        final int newVersionCount;
        final PersistenceTransaction tx = checkNotNull(Services.persistence.beginTransaction());

        try {
            // Read parent module entity of the target module version
            final OrmCollectionEntity collectionEntity;
            try {
                collectionEntity = Services.persistence.read(OrmCollectionEntity.class, collectionKey);
            } catch (EntityNotFoundException e) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                        "Cannot add collection version, collection not found: " + collectionId, log, e);
            }

            // Verify and adjust version number. Since this is the migration version of
            // adding a version, we allow takedown gaps and allow overwriting latest
            // version in case of a migrator retry.
            newVersionCount = Math.max(collectionEntity.getVersionCount(), versionNumber);

            if (versionNumber < newVersionCount) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                        "Trying to overwrite a non latest collection version: [" + collectionId + "/" + versionNumber + "]", log);
            }

            collectionEntity.setVersionCount(newVersionCount);

            // Create new collection version entity
            final OrmCollectionVersionEntity versionEntity =
                    new OrmCollectionVersionEntity(collectionKey, transactionTime, versionNumber, colxmlDoc);



            //            // Sanity check that we don't overwrite an existing version. Should never be
            //            // triggered if the persisted data is consistent.
            //            if (Services.persistence.hasObjectWithKey(versionEntity.getKey())) {
            //                tx.rollback();
            //                return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
            //                        "Server module data inconsistency. Key: " + versionEntity.getKey(), log);
            //            }

            // Update the persistence
            Services.persistence.write(collectionEntity, versionEntity);
            tx.commit();
        } catch (Throwable e) {
            tx.safeRollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                    "Error while trying to add a version to collection " + collectionId, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active: %s", collectionId);
        }

        // All done OK.
        return ResponseUtil.loggedOk("Wrote collection version " + collectionId + "/" + versionNumber,
                new AddCollectionVersionResult(collectionId, versionNumber), log);
    }


    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<AddModuleVersionResult> addModuleVersionForMigration(
            RepositoryRequestContext context, String moduleId,
            int versionNumber, String cnxmlDoc, String resourceMapDoc) {

        if (versionNumber < 1) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Invalid version number: " + versionNumber
                    + ", should be >= 1", log);
        }

        final Key moduleKey = OrmModuleEntity.moduleIdToKey(moduleId);
        if (moduleKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Cannot add module version, module id has bad format: [" + moduleId + "]", log);
        }

        if (cnxmlDoc.length() > Services.config.getMaxCnxmlDocSize()) {
            return ResponseUtil.loggedError(RepositoryStatus.OVERSIZE, "CNXML doc oversize, limit:"
                    + Services.config.getMaxCnxmlDocSize() + ", found: " + cnxmlDoc.length(), log);
        }

        if (resourceMapDoc.length() > Services.config.getMaxResourceMapDocSize()) {
            return ResponseUtil.loggedError(
                    RepositoryStatus.OVERSIZE,
                    "Module resource map doc oversize, limit:"
                            + Services.config.getMaxResourceMapDocSize() + ", found: "
                            + resourceMapDoc.length(), log);
        }

        // We read the module entity and its child export entities in one transaction. This is
        // OK since they are in the same entity group.

        final Date transactionTime = new Date();
        final int newVersionCount;
        final PersistenceTransaction tx = checkNotNull(Services.persistence.beginTransaction());

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

            // Verify and adjust version number. Since this is the migration version of
            // adding a version, we allow takedown gaps and allow overwriting latest
            // version in case of a migrator retry.
            newVersionCount = Math.max(moduleEntity.getVersionCount(), versionNumber);

            if (versionNumber < newVersionCount) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                        "Trying to overwrite an old module version: [" + moduleId + "/" + versionNumber + "]", log);
            }

            moduleEntity.setVersionCount(newVersionCount);

            // Create new module version entity
            final OrmModuleVersionEntity versionEntity =
                    new OrmModuleVersionEntity(moduleKey, transactionTime, versionNumber, cnxmlDoc,
                            resourceMapDoc);

            //            // Sanity check that we don't overwrite an existing version. Should never be
            //            // triggered if the persisted data is consistent.
            //            if (Services.persistence.hasObjectWithKey(versionEntity.getKey())) {
            //                tx.rollback();
            //                return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
            //                        "Server module data inconsistency. Key: " + versionEntity.getKey(), log);
            //            }

            // Update the persistence
            Services.persistence.write(moduleEntity, versionEntity);
            tx.commit();
        } catch (Throwable e) {
            tx.safeRollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                    "Error while trying to add a version to module " + moduleId, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active: %s", moduleId);
        }

        // All done OK.
        return ResponseUtil.loggedOk("Wrote module version " + moduleId + "/" + versionNumber,
                new AddModuleVersionResult(moduleId, versionNumber), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<AddModuleResult> addModuleForMigration(
            RepositoryRequestContext context, String forcedId) {
        final Date transactionTime = new Date();
        final PersistenceTransaction tx = Services.persistence.beginTransaction();
        try {
            // Validate forced id
            final Key forcedKey = OrmModuleEntity.moduleIdToKey(forcedId);
            if (forcedKey == null) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                        "Invalid forced module id format " + forcedId, log);
            }
            if (!PersistenceMigrationUtil.isModuleKeyProtected(forcedKey)) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.OUT_OF_RANGE,
                        "Forced module id is not out of protected id range " + forcedId, log);
            }

            // We allow to overwrite existing entity only if it has no versions. This enables
            // retries by the migrator.
            try {
                OrmModuleEntity entity = Services.persistence.read(OrmModuleEntity.class, forcedKey);
                if (entity.getVersionCount() != 0) {
                    tx.rollback();
                    return ResponseUtil.loggedError(RepositoryStatus.STATE_MISMATCH,
                            "Module already exists and has at least one version: " + forcedId, log);
                }
                log.warning("Overwriting existing module with zero versions: " + forcedId);
                // Fall through and override this entity
            } catch (EntityNotFoundException e){
                // Normal case, fall through and create a new entity
            }

            // Create and save an entity
            final OrmModuleEntity entity = new OrmModuleEntity(transactionTime);
            entity.setKey(forcedKey);
            Services.persistence.write(entity);
            checkArgument(forcedId.equals(entity.getId()), "%s vs %s", forcedId, entity.getId());
            tx.commit();

        } catch (Throwable e) {
            tx.safeRollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                    "Error when trying to create a new module with forced id: " + forcedId, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active");
        }

        return ResponseUtil.loggedOk("New module created: " + forcedId, new AddModuleResult(
                forcedId), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     * 
     * TODO(tal): remove this method after completing the data migration.
     */
    public static RepositoryResponse<AddResourceResult> addResourceForMigration(
            RepositoryRequestContext context, String forcedId, Date forcedCreationTime) {

        final PersistenceTransaction tx = Services.persistence.beginTransaction();
        try {
            // Validate forced id
            final Key forcedKey = OrmResourceEntity.resourceIdToKey(forcedId);
            if (forcedKey == null) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                        "Invalid forced resource id format " + forcedId, log);
            }
            if (!PersistenceMigrationUtil.isResourceKeyProtected(forcedKey)) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.OUT_OF_RANGE,
                        "Forced resource id is not out of protected id range " + forcedId, log);
            }

            try {
                // Entity already exists. To faciliate migrator error recovery we allow
                // to recreate a reouruce that does not have yet a blob.
                final OrmResourceEntity entity =
                        Services.persistence.read(OrmResourceEntity.class, forcedKey);
                if (entity.getState() != OrmResourceEntity.State.UPLOAD_PENDING) {
                    tx.rollback();
                    return ResponseUtil
                            .loggedError(RepositoryStatus.ALREADY_EXISTS,
                                    "A resource with this forced id has already been uploaded: "
                                            + forcedId, log);
                }
                log.info("Allowing to recreate empty resources " + forcedId);
            } catch (EntityNotFoundException e) {
                // Entity does not exist, this is the normal case
                final OrmResourceEntity entity = new OrmResourceEntity(forcedCreationTime);
                entity.setKey(forcedKey);
                Services.persistence.write(entity);
                checkArgument(forcedId.equals(entity.getId()), "%s vs %s", forcedId, entity.getId());
            }

            tx.commit();

        } catch (Throwable e) {
            tx.safeRollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                    "Error when trying to create a new resource with forced id: " + forcedId, log,
                    e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active");
        }

        final String completionUrl =
                ResourceOperations.RESOURCE_UPLOAD_COMPLETION_SERVLET_PATH + "?"
                        + ResourceUtil.encodeUploadCompletionParameters(forcedId);

        String uploadUrl = Services.blobstore.createUploadUrl(completionUrl);

        return ResponseUtil.loggedOk("Resource created with forced id: " + forcedId,
                new AddResourceResult(forcedId, uploadUrl), log);
    }



}
