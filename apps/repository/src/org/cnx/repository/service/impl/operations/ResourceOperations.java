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

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateResourceResult;
import org.cnx.repository.service.api.GetResourceInfoResult;
import org.cnx.repository.service.api.GetResourceListResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.api.ServeResourceResult;
import org.cnx.repository.service.api.UploadedResourceContentInfo;
import org.cnx.repository.service.impl.persistence.IdUtil;
import org.cnx.repository.service.impl.persistence.OrmBlobInfo;
import org.cnx.repository.service.impl.persistence.OrmResourceEntity;
import org.cnx.repository.service.impl.persistence.PersistenceMigrationUtil;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Implementation of the resource related operations of the repository service.
 * 
 * @author Tal Dayan
 */
public class ResourceOperations {

    /**
     * Result count limit for {@link #getResourceList}. If the caller asks for a larger max value,
     * it is trim silently to this value.
     */
    private static final int MAX_RESOURCE_PER_LIST_QUERY = 1000;

    /**
     * Base path of the resource upload completion servlet. Should match servlet mapping in web.xml.
     * Servlet mapping should be this value with the suffix "/*".
     */
    private static final String UPLOAD_COMPLETION_SERVLET_PATH = "/_repo/resource_uploaded";

    private static final Logger log = Logger.getLogger(ResourceOperations.class.getName());

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<CreateResourceResult> createResource(
            RepositoryRequestContext context) {

        final Date transactionTime = new Date();
        final String resourceId;
        try {
            final OrmResourceEntity entity = new OrmResourceEntity(transactionTime);
            Services.persistence.write(entity);
            // NOTE(tal): resource id is available here after the entity has been persisted and
            // it was assigned a key.
            resourceId = entity.getId();
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                    "Failed to create a new resource", log, e);
        }

        final String completionUrl =
                UPLOAD_COMPLETION_SERVLET_PATH + "?"
                        + ResourceUtil.encodeUploadCompletionParameters(resourceId);

        String uploadUrl = Services.blobstore.createUploadUrl(completionUrl);

        return ResponseUtil.loggedOk("Resource created: " + resourceId, new CreateResourceResult(
                resourceId, uploadUrl), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     * 
     * TODO(tal): remove this method after completing the data migration.
     */
    public static RepositoryResponse<CreateResourceResult> migrationCreateResourceWithId(
            RepositoryRequestContext context, String forcedId) {

        final Date transactionTime = new Date();
        final Transaction tx = Services.persistence.beginTransaction();
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
            if (Services.persistence.hasObjectWithKey(forcedKey)) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.ALREADY_EXISTS,
                        "A resource with this forced id already exists: " + forcedId, log);
            }

            final OrmResourceEntity entity = new OrmResourceEntity(transactionTime);
            entity.setKey(OrmResourceEntity.resourceIdToKey(forcedId));
            Services.persistence.write(entity);
            checkArgument(forcedId.equals(entity.getId()), "%s vs %s", forcedId, entity.getId());
            tx.commit();

        } catch (Throwable e) {
            tx.rollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                    "Error when trying to create a new resource with forced id: " + forcedId, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active");
        }

        final String completionUrl =
                UPLOAD_COMPLETION_SERVLET_PATH + "?"
                        + ResourceUtil.encodeUploadCompletionParameters(forcedId);

        String uploadUrl = Services.blobstore.createUploadUrl(completionUrl);

        return ResponseUtil.loggedOk("Resource created with forced id: " + forcedId, new CreateResourceResult(
                forcedId, uploadUrl), log);

    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetResourceInfoResult> getResourceInfo(
            RepositoryRequestContext context, String resourceId) {

        // Convert to internal id
        final Key resourceKey = OrmResourceEntity.resourceIdToKey(resourceId);
        if (resourceKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Resource id has bad format: [" + resourceId + "]", log);
        }

        final OrmResourceEntity entity;
        try {
            entity = Services.persistence.read(OrmResourceEntity.class, resourceKey);
        } catch (EntityNotFoundException e) {
            return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND, "Resource not found: ["
                    + resourceId + "]", log, e);
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                    "Error when trying to retrieve resource: [" + resourceId + "]", log, e);
        }

        // Construct result.
        final GetResourceInfoResult result;
        switch (entity.getState()) {
            case UPLOAD_PENDING:
                result =
                GetResourceInfoResult
                .newPendingUploac(entity.getId(), entity.getCreationTime());
                break;
            case UPLOAD_COMPLETE:
                final OrmBlobInfo blobInfo = checkNotNull(entity.getBlobInfo());
                final UploadedResourceContentInfo contentInfo =
                        new UploadedResourceContentInfo(blobInfo.getContentType(), blobInfo.getSize(),
                                blobInfo.getCreationTime(), blobInfo.getFileName(), blobInfo.getSize(),
                                blobInfo.getMd5Hash());
                result =
                        GetResourceInfoResult.newUploaded(entity.getId(), entity.getCreationTime(),
                                contentInfo);
                break;
            default:
                return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                        "Unknown resource entity state:" + entity.getState(), log);
        }

        return ResponseUtil.loggedOk("Retrieved info of resource " + resourceId, result, log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<ServeResourceResult> serveResource(
            RepositoryRequestContext context, String resourceId, HttpServletResponse httpResponse) {

        final Key resourceKey = OrmResourceEntity.resourceIdToKey(resourceId);
        if (resourceKey == null) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Resource id has bad format: " + resourceId, log);
        }

        final BlobKey blobKey;
        try {
            final OrmResourceEntity ormEntity =
                    Services.persistence.read(OrmResourceEntity.class, resourceKey);
            if (ormEntity.getState() != OrmResourceEntity.State.UPLOAD_COMPLETE) {
                return ResponseUtil.loggedError(RepositoryStatus.STATE_MISMATCH,
                        "Resource content has not been uploaded yet: " + resourceId, log);
            }
            blobKey = ormEntity.getBlobInfo().getBlobKey();
        } catch (EntityNotFoundException e) {
            return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND, "Resource not found: ["
                    + resourceId + "]", log, e);
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                    "Error when trying to retrieve resource: " + resourceId, log, e);
        }

        // Serve the resource from Blobstore.
        try {
            Services.blobstore.serve(blobKey, httpResponse);
        } catch (IOException e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERROR,
                    "Error serving the resource content: " + resourceId, log, e);
        }

        /*
         * Appengine uses Blobstore for storing big blobs. Upload and download from Blobstores is
         * done separately from normal App. At the time of serving the blob, Blobstore service sets
         * a header with "BlobKey = <value>" and then App Engine replaces the body of the response
         * with the content of the blob.
         */
        final ImmutableMap<String, String> additionalHeaders =
                ImmutableMap.of(BlobstoreUtil.BLOB_KEY_HEADER_NAME, blobKey.toString());
        ServeResourceResult result = new ServeResourceResult(additionalHeaders);
        return ResponseUtil.loggedOk("Resource served: " + resourceId, result, log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<GetResourceListResult> getResourceList(
            RepositoryRequestContext context, String startCursor, int maxResults) {

        if (maxResults < 1) {
            return ResponseUtil.loggedError(RepositoryStatus.BAD_REQUEST,
                    "Max result should be >= 1, found: " + maxResults, log);
        }

        if (maxResults > MAX_RESOURCE_PER_LIST_QUERY) {
            log.info("Reducing caller resource maxResults from " + maxResults + " to "
                    + MAX_RESOURCE_PER_LIST_QUERY);
            maxResults = MAX_RESOURCE_PER_LIST_QUERY;
        }

        Pair<List<Key>, String> results =
                Services.persistence.entityKeyList(OrmResourceEntity.class, maxResults, startCursor);

        final ImmutableList<String> resourceIds =
                IdUtil.keysToIds(OrmResourceEntity.class, results.first);

        return ResponseUtil.loggedOk("Retrieve resource list page with " + resourceIds.size()
                + " module ids", new GetResourceListResult(resourceIds, results.second), log);
    }
}
