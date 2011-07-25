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

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoResourceEntity;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateResourceResult;
import org.cnx.repository.service.api.GetResourceInfoResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.api.ServeResourceResult;
import org.cnx.repository.service.api.UploadedResourceContentInfo;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;

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
    static RepositoryResponse<CreateResourceResult>
                    createResource(RepositoryRequestContext context) {

        final String resourceId;
        final PersistenceManager pm = Services.datastore.getPersistenceManager();
        try {
            final JdoResourceEntity entity = new JdoResourceEntity();
            entity.idleToPendingTransition();

            // The unique resource id is created the first time the entity is persisted.
            pm.makePersistent(entity);
            resourceId = checkNotNull(entity.getResourceId(), "Null resource id");
        } finally {
            pm.close();
        }

        final String completionUrl = UPLOAD_COMPLETION_SERVLET_PATH + "/" + resourceId;
        final String uploadUrl = Services.blobstore.createUploadUrl(completionUrl);

        return RepositoryResponse.newOk("Resource created", new CreateResourceResult(
            resourceId, uploadUrl));
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    static RepositoryResponse<GetResourceInfoResult> getResourceInfo(
        RepositoryRequestContext context, String resourceId) {

        // Convert to internal id
        final Key resourceKey = JdoResourceEntity.resourceIdToKey(resourceId);
        if (resourceKey == null) {
            return RepositoryResponse.newError(RepositoryStatus.BAD_REQUEST,
                "Resource id has bad format: [" + resourceId + "]");
        }

        // Lookup resource entity
        final PersistenceManager pm = Services.datastore.getPersistenceManager();
        final JdoResourceEntity entity;
        try {
            entity = pm.getObjectById(JdoResourceEntity.class, resourceKey);
        } catch (Throwable e) {
            e.printStackTrace();
            return RepositoryResponse.newError(RepositoryStatus.NOT_FOUND,
                "Could not locate resource [" + resourceId + "]");
        } finally {
            pm.close();
        }

        // Construct result.
        final GetResourceInfoResult result;
        switch (entity.getState()) {
            case PENDING_UPLOAD:
                result = GetResourceInfoResult.newPendingUploac();
                break;
            case UPLOADED:
                // NOTE(tal): blob info could be cased in the resource entity when completing
                // the content upload.
                final BlobInfo blobInfo =
                    Services.blobInfoFactory.loadBlobInfo(entity.getBlobKey());
                if (blobInfo == null) {
                    return RepositoryResponse.newError(RepositoryStatus.SERVER_ERRROR,
                        "Could not locate blob at key [" + entity.getBlobKey() + "]");
                }
                final UploadedResourceContentInfo contentInfo =
                    new UploadedResourceContentInfo(blobInfo.getContentType(), blobInfo.getSize(),
                        blobInfo.getCreation(), blobInfo.getFilename());
                result = GetResourceInfoResult.newUploaded(contentInfo);
                break;
            default:
                return RepositoryResponse.newError(RepositoryStatus.SERVER_ERRROR,
                    "Unknown resource entity state [" + entity.getState() + "]");
        }

        return RepositoryResponse.newOk("Resource INFO retrieved", result);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    static RepositoryResponse<ServeResourceResult> serveResource(RepositoryRequestContext context,
        String resourceId, HttpServletResponse httpResponse) {

        final Key resourceKey = JdoResourceEntity.resourceIdToKey(resourceId);
        if (resourceKey == null) {
            return RepositoryResponse.newError(RepositoryStatus.BAD_REQUEST,
                "Resource id has bad format: [" + resourceId + "]");
        }

        PersistenceManager pm = Services.datastore.getPersistenceManager();
        final BlobKey blobKey;

        try {
            JdoResourceEntity entity = pm.getObjectById(JdoResourceEntity.class, resourceKey);
            if (entity.getState() != JdoResourceEntity.State.UPLOADED) {
                return RepositoryResponse.newError(RepositoryStatus.STATE_MISMATCH,
                    "Resource content has not been uploaded yet: [" + resourceId + "]");
            }
            blobKey = entity.getBlobKey();
        } catch (Throwable e) {
            e.printStackTrace();
            return RepositoryResponse.newError(RepositoryStatus.NOT_FOUND,
                "Could not locate resources: [" + resourceId + "]");
        } finally {
            pm.close();
        }

        // Serve the resource from Blobstore.
        try {
            Services.blobstore.serve(blobKey, httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return RepositoryResponse
                .newError(RepositoryStatus.SERVER_ERRROR, "Error serving the resource content: ["
                    + resourceId + "] (" + e.getMessage() + ")");
        }

        return RepositoryResponse.newOk("Resource sent", new ServeResourceResult());
    }
}
