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

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.cnx.repository.service.api.AddCollectionVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateCollectionResult;
import org.cnx.repository.service.api.ExportInfo;
import org.cnx.repository.service.api.GetCollectionInfoResult;
import org.cnx.repository.service.api.GetCollectionListResult;
import org.cnx.repository.service.api.GetCollectionVersionInfoResult;
import org.cnx.repository.service.api.GetCollectionVersionResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.impl.persistence.IdUtil;
import org.cnx.repository.service.impl.persistence.OrmCollectionEntity;
import org.cnx.repository.service.impl.persistence.OrmCollectionVersionEntity;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;

/**
 * Implementation of the collection related operations of the repository service.
 * 
 * @author Tal Dayan
 */
public class CollectionOperations {
    /**
     * Result count limit for {@link getCollectionList}. If the caller asks for a larger max value,
     * it is trim silently to this value.
     */
    private static final int MAX_COLLECTIONS_PER_LIST_QUERY = 1000;

    private static final Logger log = Logger.getLogger(CollectionOperations.class.getName());

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<CreateCollectionResult> createCollection(
            RepositoryRequestContext context) {
        final String collectionId;

        final Date transactionTime = new Date();

        try {
            final OrmCollectionEntity entity = new OrmCollectionEntity(transactionTime);

            // The unique collection id is created the first time the entity is persisted.
            Services.persistence.write(entity);
            collectionId = checkNotNull(entity.getId(), "Null collection id");
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error when trying to create a new collection", log, e);
        }

        return ResponseUtil.loggedOk("New collection created: " + collectionId,
                new CreateCollectionResult(collectionId), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetCollectionInfoResult> getCollectionInfo(
            RepositoryRequestContext context, String collectionId) {
        final Key collectionKey = OrmCollectionEntity.collectionIdToKey(collectionId);
        if (collectionKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Collection id has invalid format: " + collectionId, log);
        }

        final OrmCollectionEntity collectionEntity;
        final List<ExportInfo> exports;
        final Transaction tx = Services.persistence.beginTransaction();
        try {
            // Read collection entity
            try {
                collectionEntity =
                    Services.persistence.read(OrmCollectionEntity.class, collectionKey);
            } catch (EntityNotFoundException e) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                        "Could not find collection " + collectionId, log, e);
            }

            // Read exports
            exports = ExportUtil.fetchParentEportInfoList(Services.persistence, collectionKey);

            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error fetching the info of collection " + collectionId, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active: %s/%s", collectionId);
        }

        return ResponseUtil.loggedOk("Retrieved info of collection " + collectionId,
                new GetCollectionInfoResult(collectionId, collectionEntity.getCreationTime(),
                    collectionEntity.getVersionCount(), exports), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetCollectionListResult> getCollectionList(
            RepositoryRequestContext context, @Nullable String startCursor, int maxResults) {
        if (maxResults < 1) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Max result should be >= 1, found: " + maxResults, log);
        }

        if (maxResults > MAX_COLLECTIONS_PER_LIST_QUERY) {
            log.info("Reducing caller collection maxResults from " + maxResults + " to "
                + MAX_COLLECTIONS_PER_LIST_QUERY);
            maxResults = MAX_COLLECTIONS_PER_LIST_QUERY;
        }

        Pair<List<Key>, String> results =
            Services.persistence.entityKeyList(OrmCollectionEntity.class, maxResults, startCursor);

        final ImmutableList<String> collectionIds =
            IdUtil.keysToIds(OrmCollectionEntity.class, results.first);

        return ResponseUtil.loggedOk("Retrieve collection list page with " + collectionIds.size()
            + " module ids", new GetCollectionListResult(collectionIds, results.second), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<AddCollectionVersionResult> addCollectionVersion(
            RepositoryRequestContext context, String collectionId,
            @Nullable Integer expectedVersionNumber, String colxmlDoc) {

        if (expectedVersionNumber != null && expectedVersionNumber < 1) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Invalid expected version number: " + expectedVersionNumber
                        + ", should be >= 1", log);
        }

        final Key collectionKey = OrmCollectionEntity.collectionIdToKey(collectionId);
        if (collectionKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Cannot add collection version, collection id has bad format: [" + collectionId
                        + "]", log);
        }

        final int newVersionNumber;
        final Date transactionTime = new Date();
        final Transaction tx = Services.persistence.beginTransaction();
        try {
            // Read collection entity
            final OrmCollectionEntity collectionEntity;
            try {
                collectionEntity =
                    Services.persistence.read(OrmCollectionEntity.class, collectionKey);
            } catch (EntityNotFoundException e) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                        "Cannot add collection version, collection not found: " + collectionId,
                        log, e);
            }

            // Increment the collection version count
            newVersionNumber = collectionEntity.incrementVersionCount();

            // If version conflict reject operation
            if (expectedVersionNumber != null && !expectedVersionNumber.equals(newVersionNumber)) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.VERSION_CONFLICT,
                        "Version conflict in collection " + collectionId + ", expected: "
                            + expectedVersionNumber + ", actual: " + newVersionNumber, log);
            }

            // Create new version entity
            final OrmCollectionVersionEntity versionEntity =
                new OrmCollectionVersionEntity(collectionKey, transactionTime, newVersionNumber,
                    colxmlDoc);

            // Sanity check that we don't overwrite an existing version. Should never be
            // triggered if the persisted data is consistent.
            if (Services.persistence.hasObjectWithKey(versionEntity.getKey())) {
                tx.rollback();
                return ResponseUtil
                    .loggedError(RepositoryStatus.SERVER_ERRROR,
                            "Server collection data inconsistency. Key: " + versionEntity.getKey(),
                            log);
            }

            // Update persistence
            Services.persistence.write(collectionEntity, versionEntity);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error while trying to add a version to collection " + collectionId, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active: %s", collectionId);
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
            RepositoryRequestContext context, String collectionId,
            @Nullable Integer collectionVersion) {

        if (collectionVersion != null && collectionVersion < 1) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Illegal collection version number " + collectionVersion, log);
        }

        final Key collectionKey = OrmCollectionEntity.collectionIdToKey(collectionId);
        if (collectionKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Collection id has bad format: [" + collectionId + "]", log);
        }

        final int versionToServe;
        final OrmCollectionVersionEntity versionEntity;
        try {
            // Determine collection version to serve. If 'latest' than read collection entity and
            // determine latest version.
            //
            // NOTE(tal): we don't need to use a transaction for the collection lookup and the
            // version entity since versions are never deleted and version count is monotonic.
            //
            if (collectionVersion == null) {
                final OrmCollectionEntity collectionEntity;
                try {
                    collectionEntity =
                        Services.persistence.read(OrmCollectionEntity.class, collectionKey);
                } catch (EntityNotFoundException e) {
                    return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                            "Could not locate collection " + collectionId, log);
                }
                // If collection has no versions than there is not latest version.
                if (collectionEntity.getVersionCount() < 1) {
                    return ResponseUtil.loggedError(RepositoryStatus.STATE_MISMATCH,
                            "Collection has no versions: " + collectionId, log);
                }
                versionToServe = collectionEntity.getVersionCount();
            } else {
                versionToServe = collectionVersion;
            }

            // Fetch collection version entity
            final Key collectionVersionKey =
                OrmCollectionVersionEntity.collectionVersionKey(collectionKey, versionToServe);

            // NOTE(tal): if we read the collectionEntity and versiontToServe is in its
            // valid version range than this is actually a server error.
            try {
                versionEntity =
                    Services.persistence.read(OrmCollectionVersionEntity.class,
                            collectionVersionKey);
            } catch (EntityNotFoundException e) {
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                        "Could not locate collection version " + collectionId + "/"
                            + versionToServe, log);
            }

            checkState(versionEntity.getVersionNumber() == versionToServe,
                    "Inconsistent version in collection %s, expected %s found %s", collectionId,
                    versionToServe, versionEntity.getVersionNumber());

        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error while looking collection version " + collectionId + "/"
                        + collectionVersion, log, e);
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
            RepositoryRequestContext context, String collectionId,
            @Nullable Integer collectionVersion) {

        if (collectionVersion != null && collectionVersion < 1) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Illegal collection version number " + collectionVersion, log);
        }

        final Key collectionKey = OrmCollectionEntity.collectionIdToKey(collectionId);
        if (collectionKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Collection id has bad format: [" + collectionId + "]", log);
        }

        final int versionToServe;
        final OrmCollectionVersionEntity versionEntity;
        final List<ExportInfo> exports;
        final Transaction tx = Services.persistence.beginTransaction();
        try {
            // Determine collection version to serve. If 'latest' than read collection entity and
            // determine latest version.
            //
            // NOTE(tal): we don't need to use a transaction for the collection lookup and the
            // version entity since versions are never deleted and version count is monotonic.
            //
            if (collectionVersion == null) {
                final OrmCollectionEntity collectionEntity;
                try {
                    collectionEntity =
                        Services.persistence.read(OrmCollectionEntity.class, collectionKey);
                } catch (EntityNotFoundException e) {
                    tx.rollback();
                    return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                            "Could not locate collection " + collectionId, log);
                }
                // If collection has no versions than there is no latest version.
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
                OrmCollectionVersionEntity.collectionVersionKey(collectionKey, versionToServe);
            try {
                versionEntity =
                    Services.persistence.read(OrmCollectionVersionEntity.class,
                            collectionVersionKey);
            } catch (EntityNotFoundException e) {
                // NOTE(tal): if we read the collection entity and versionToServe is within its
                // valid version range that this is actually a server error.
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                        "Collection version not found: " + collectionId + "/" + versionToServe,
                        log, e);
            }

            checkState(versionEntity.getVersionNumber() == versionToServe,
                    "Inconsistent version in collection %s, expected %s found %s", collectionId,
                    versionToServe, versionEntity.getVersionNumber());

            // Get exports
            exports =
                ExportUtil.fetchParentEportInfoList(Services.persistence, collectionVersionKey);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            return ResponseUtil
                .loggedError(RepositoryStatus.SERVER_ERRROR, "Collection version not found"
                    + collectionId + "/" + collectionVersion, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active: %s", collectionId);
        }

        final GetCollectionVersionInfoResult result =
            new GetCollectionVersionInfoResult(collectionId, versionEntity.getVersionNumber(),
                versionEntity.getCreationTime(), exports);
        return ResponseUtil.loggedOk("Fetched collection version info", result, log);
    }

}
