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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.cnx.repository.service.api.AddCollectionVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateCollectionResult;
import org.cnx.repository.service.api.ExportInfo;
import org.cnx.repository.service.api.GetCollectionInfoResult;
import org.cnx.repository.service.api.GetCollectionVersionInfoResult;
import org.cnx.repository.service.api.GetCollectionVersionResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.impl.schema.JdoCollectionEntity;
import org.cnx.repository.service.impl.schema.JdoCollectionVersionEntity;
import org.cnx.repository.service.impl.schema.JdoExportItemEntity;
import org.cnx.util.Nullable;

import com.google.appengine.api.datastore.Key;

/**
 * Implementation of the collection related operations of the repository service.
 * 
 * @author Tal Dayan
 */
public class CollectionOperations {
    private static final Logger log = Logger.getLogger(CollectionOperations.class.getName());

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<CreateCollectionResult> createCollection(
        RepositoryRequestContext context) {
        final String collectionId;
        final PersistenceManager pm = Services.datastore.getPersistenceManager();

        try {
            final JdoCollectionEntity entity = new JdoCollectionEntity();
            // The unique collection id is created the first time the entity is persisted.
            pm.makePersistent(entity);
            collectionId = checkNotNull(entity.getCollectionId(), "Null collection id");
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                "Error when trying to create a new collection", log, e);
        } finally {
            pm.close();
        }

        return ResponseUtil.loggedOk("New collection created: " + collectionId,
            new CreateCollectionResult(collectionId), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetCollectionInfoResult> getCollectionInfo(
        RepositoryRequestContext context, String collectionId) {
        final Key collectionKey = JdoCollectionEntity.collectionIdToKey(collectionId);
        if (collectionKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                "Collection id has invalid format: " + collectionId, log);
        }

        final PersistenceManager pm = Services.datastore.getPersistenceManager();
        final JdoCollectionEntity collectionEntity;
        final List<ExportInfo> exports;
        try {
            try {
                collectionEntity = pm.getObjectById(JdoCollectionEntity.class, collectionKey);
            } catch (Throwable e) {
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                    "Could not find collection " + collectionId, log, e);
            }

            // Get exports info
            final List<JdoExportItemEntity> exportEntities =
                ExportUtil.queryChildExports(pm, collectionKey);
            exports = ExportUtil.exportInfoList(exportEntities);
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                "Error fetching the info of collection " + collectionId, log, e);
        } finally {
            pm.close();
        }

        return ResponseUtil.loggedOk("Retrieved info of collection " + collectionId,
            new GetCollectionInfoResult(collectionId, collectionEntity.getVersionCount(), exports),
            log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<AddCollectionVersionResult> addCollectionVersion(
        RepositoryRequestContext context, String collectionId, String colxmlDoc) {

        final Key collectionKey = JdoCollectionEntity.collectionIdToKey(collectionId);
        if (collectionKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                "Cannot add collection version, collection id has bad format: [" + collectionId
                    + "]", log);
        }

        final PersistenceManager pm = Services.datastore.getPersistenceManager();
        final Transaction tx = pm.currentTransaction();
        final int newVersionNumber;
        try {
            tx.begin();

            // Read parent entity of this collection
            final JdoCollectionEntity collectionEntity;
            try {
                collectionEntity = pm.getObjectById(JdoCollectionEntity.class, collectionKey);
            } catch (Throwable e) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                    "Cannot add collection version, collection not found: " + collectionId, log, e);
            }

            // Updated number of versions in the collection entity
            newVersionNumber = collectionEntity.incrementVersionCount();

            // Create new version entity
            final JdoCollectionVersionEntity versionEntity =
                new JdoCollectionVersionEntity(collectionKey, newVersionNumber, colxmlDoc);

            // TODO(tal): If a collection version with this key already exists (due to data
            // inconsistency), return an error rather than overwriting it.

            pm.makePersistent(versionEntity);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                "Error while trying to add a version to collection " + collectionId, log, e);
        } finally {
            if (tx.isActive()) {
                log.severe("Transaction left opened when adding collection version:  "
                    + collectionId);
                tx.rollback();
            }
            pm.close();
        }

        // All done OK.
        return ResponseUtil
            .loggedOk("Added collection version " + collectionId + "/" + newVersionNumber,
                new AddCollectionVersionResult(collectionId, newVersionNumber), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetCollectionVersionResult> getCollectionVersion(
        RepositoryRequestContext context, String collectionId, @Nullable Integer collectionVersion) {

        if (collectionVersion != null && collectionVersion < 1) {
            ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                "Illegal collection version number " + collectionVersion, log);
        }

        final Key collectionKey = JdoCollectionEntity.collectionIdToKey(collectionId);
        if (collectionKey == null) {
            ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                "Collection id has bad format: [" + collectionId + "]", log);
        }

        PersistenceManager pm = Services.datastore.getPersistenceManager();

        final int versionToServe;
        final JdoCollectionVersionEntity versionEntity;
        try {
            // Determine collection version to serve. If 'latest' than read collection entity and
            // determine latest version.
            //
            // NOTE(tal): we don't need to use a transaction for the collection lookup and the
            // version entity since versions are never deleted and version count is monotonic.
            //
            if (collectionVersion == null) {
                final JdoCollectionEntity collectionEntity;
                try {
                    collectionEntity = pm.getObjectById(JdoCollectionEntity.class, collectionKey);
                } catch (Throwable e) {
                    return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                        "Could not locate collection " + collectionId, log);
                }
                // If collection has no versions than there is not latest version.
                if (collectionEntity.getVersionCount() < 1) {
                    ResponseUtil.loggedError(RepositoryStatus.STATE_MISMATCH,
                        "Collection has no versions: " + collectionId, log);
                }
                versionToServe = collectionEntity.getVersionCount();
            } else {
                versionToServe = collectionVersion;
            }

            // Fetch collection version entity
            final Key collectionVersionKey =
                JdoCollectionVersionEntity.collectionVersionKey(collectionKey, versionToServe);
            try {
                versionEntity =
                    pm.getObjectById(JdoCollectionVersionEntity.class, collectionVersionKey);
                checkState(versionEntity.getVersionNumber() == versionToServe,
                    "Inconsistent version in collection %s, expected %s found %s", collectionId,
                    versionToServe, versionEntity.getVersionNumber());
            } catch (Throwable e) {
                return ResponseUtil
                    .loggedError(RepositoryStatus.SERVER_ERRROR,
                        "Error while looking collection version " + collectionId + "/"
                            + versionToServe, log, e);
            }
        } finally {
            pm.close();
        }

        final GetCollectionVersionResult result =
            new GetCollectionVersionResult(collectionId, versionEntity.getVersionNumber(),
                versionEntity.getColxmlDoc());
        return ResponseUtil.loggedOk("Fetched collection version", result, log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetCollectionVersionInfoResult> getCollectionVersionInfo(
        RepositoryRequestContext context, String collectionId, @Nullable Integer collectionVersion) {

        if (collectionVersion != null && collectionVersion < 1) {
            ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                "Illegal collection version number " + collectionVersion, log);
        }

        final Key collectionKey = JdoCollectionEntity.collectionIdToKey(collectionId);
        if (collectionKey == null) {
            ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                "Collection id has bad format: [" + collectionId + "]", log);
        }

        PersistenceManager pm = Services.datastore.getPersistenceManager();

        final int versionToServe;
        final JdoCollectionVersionEntity versionEntity;
        final List<ExportInfo> exports;
        try {
            // Determine collection version to serve. If 'latest' than read collection entity and
            // determine latest version.
            //
            // NOTE(tal): we don't need to use a transaction for the collection lookup and the
            // version entity since versions are never deleted and version count is monotonic.
            //
            if (collectionVersion == null) {
                final JdoCollectionEntity collectionEntity;
                try {
                    collectionEntity = pm.getObjectById(JdoCollectionEntity.class, collectionKey);
                } catch (Throwable e) {
                    return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                        "Could not locate collection " + collectionId, log);
                }
                // If collection has no versions than there is not latest version.
                if (collectionEntity.getVersionCount() < 1) {
                    ResponseUtil.loggedError(RepositoryStatus.STATE_MISMATCH,
                        "Collection has no versions: " + collectionId, log);
                }
                versionToServe = collectionEntity.getVersionCount();
            } else {
                versionToServe = collectionVersion;
            }

            // Fetch collection version entity
            final Key collectionVersionKey =
                JdoCollectionVersionEntity.collectionVersionKey(collectionKey, versionToServe);
            try {
                versionEntity =
                    pm.getObjectById(JdoCollectionVersionEntity.class, collectionVersionKey);
                checkState(versionEntity.getVersionNumber() == versionToServe,
                    "Inconsistent version in collection %s, expected %s found %s", collectionId,
                    versionToServe, versionEntity.getVersionNumber());
            } catch (Throwable e) {
                return ResponseUtil
                    .loggedError(RepositoryStatus.SERVER_ERRROR,
                        "Error while looking collection version " + collectionId + "/"
                            + versionToServe, log, e);
            }

            final List<JdoExportItemEntity> exportEntities =
                ExportUtil.queryChildExports(pm, collectionVersionKey);
            exports = ExportUtil.exportInfoList(exportEntities);
        } finally {
            pm.close();
        }

        final GetCollectionVersionInfoResult result =
            new GetCollectionVersionInfoResult(collectionId, versionEntity.getVersionNumber(),
                exports);
        return ResponseUtil.loggedOk("Fetched collection version info", result, log);
    }

}
