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

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateResourceResult;
import org.cnx.repository.service.api.GetResourceInfoResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.api.ServeResourceResult;
import org.cnx.repository.service.api.UploadedResourceContentInfo;
import org.cnx.repository.service.impl.persistence.OrmResourceEntity;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.common.collect.ImmutableMap;

/**
 * Implementation of the resource related operations of the repository service.
 * 
 * @author Tal Dayan
 */
public class ResourceOperations {

    private static final Logger log = Logger.getLogger(ResourceOperations.class.getName());

    /**
     * Base path of the resource upload completion servlet. Should match servlet mapping in web.xml.
     * Servlet mapping should be this value with the suffix "/*".
     */
    private static final String UPLOAD_COMPLETION_SERVLET_PATH = "/resource_factory/uploaded";

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<CreateResourceResult> createResource(
            RepositoryRequestContext context) {

        final String resourceId;
        try {
            final OrmResourceEntity entity = new OrmResourceEntity();
            Services.persistence.write(entity);
            // NOTE(tal): resource id is available here after the entity has been persisted and
            // it was assigned a key.
            resourceId = entity.getId();
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
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
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error when trying to retrieve resource: [" + resourceId + "]", log, e);
        }

        // Construct result.
        final GetResourceInfoResult result;
        switch (entity.getState()) {
            case UPLOAD_PENDING:
                result = GetResourceInfoResult.newPendingUploac();
                break;
            case UPLOAD_COMPLETE:
                // NOTE(tal): blob info could be cased in the resource entity when completing
                // the content upload.
                final BlobInfo blobInfo =
                Services.blobInfoFactory.loadBlobInfo(entity.getBlobKey());
                if (blobInfo == null) {
                    return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                            "Could not locate blob at key: " + entity.getBlobKey(), log);
                }
                final UploadedResourceContentInfo contentInfo =
                        new UploadedResourceContentInfo(blobInfo.getContentType(), blobInfo.getSize(),
                                blobInfo.getCreation(), blobInfo.getFilename());
                result = GetResourceInfoResult.newUploaded(contentInfo);
                break;
            default:
                return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
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
            blobKey = ormEntity.getBlobKey();
        } catch (EntityNotFoundException e) {
            return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND, "Resource not found: ["
                    + resourceId + "]", log, e);
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error when trying to retrieve resource: " + resourceId, log, e);
        }

        // Serve the resource from Blobstore.
        try {
            Services.blobstore.serve(blobKey, httpResponse);
        } catch (IOException e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error serving the resource content: " + resourceId, log, e);
        }

        /*
         * Appengine uses Blobstore for storing big blobs. Upload and download from Blobstores is
         * done separately from normal App. At the time of serving the blob, Blobstore service sets
         * a header with "BlobKey = <value>" and then App Engine replaces the body of the response
         * with the content of the blob.
         */
        final ImmutableMap<String, String> additionalHeaders = ImmutableMap.of(BlobstoreUtil.BLOB_KEY_HEADER_NAME, blobKey.toString());
        ServeResourceResult result = new ServeResourceResult(additionalHeaders);
        return ResponseUtil.loggedOk("Resource served: " + resourceId, result, log);
    }
}
