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

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.ExportReference;
import org.cnx.repository.service.api.GetExportUploadUrlResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.api.ServeExportResult;
import org.cnx.repository.service.impl.schema.CnxJdoEntity;
import org.cnx.repository.service.impl.schema.JdoExportItemEntity;

import com.google.appengine.api.blobstore.BlobKey;

/**
 * Implementation of the export related operations of the repository service.
 * 
 * @author Tal Dayan
 */
public class ExportOperations {

    // TODO(tal): Move to a shared place. See ResourceOperations, also used there.
    private static final String DEFAULT_UPLOAD_URL_PREFIX = "http://127.0.0.1:8888";

    private static final Logger log = Logger.getLogger(ExportOperations.class.getName());

    /**
     * URI of export upload completion servlet. Should match the servlet mapping in web.xml.
     */
    private static final String UPLOAD_COMPLETION_SERVLET_PATH = "/exports/uploaded";

    /**
     * See description in {@link CnxRepositoryService#getExportUploadUrl}
     */
    public static RepositoryResponse<GetExportUploadUrlResult> getExportUploadUrl(
        RepositoryRequestContext context, ExportReference exportReference) {

        // Validate the export reference
        final ExportReferenceValidationResult validationResult =
            ExportReferenceValidationResult.forReference(exportReference);
        if (validationResult.getRepositoryStatus().isError()) {
            return ResponseUtil.loggedError(validationResult.getRepositoryStatus(),
                validationResult.getStatusDescription(), log);
        }

        // Verify that the parent entity exist.
        final PersistenceManager pm = Services.datastore.getPersistenceManager();
        try {
            @SuppressWarnings({ "unused", "unchecked" })
            final CnxJdoEntity parentEntity =
                pm.getObjectById(validationResult.getParentEntityClass(), validationResult
                    .getParentKey());
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                "Export parent object not found: " + exportReference, log, e);
        } finally {
            pm.close();
        }

        // Construct completion URL that includes the export reference. This will triger
        // the completion servlet when the blob upload is completed.
        final String completionUrl =
            UPLOAD_COMPLETION_SERVLET_PATH + "?scope=" + exportReference.getScopeType() + "&id="
                + exportReference.getObjectId() + "&version=" + exportReference.getVersionNumber()
                + "&type=" + exportReference.getExportTypeId();

        // Workaround for runs locally in eclipse.
        //
        // TODO(tal): share the prefix logic with resource creation.
        String uploadUrl = Services.blobstore.createUploadUrl(completionUrl);
        if (uploadUrl.startsWith("/")) {
            log.warning("Prefexing resource upload url with " + DEFAULT_UPLOAD_URL_PREFIX);
            uploadUrl = DEFAULT_UPLOAD_URL_PREFIX + uploadUrl;
        }

        // All done OK.
        return ResponseUtil.loggedOk("Created upload URL for export: " + exportReference,
            new GetExportUploadUrlResult(uploadUrl, validationResult.getExportType()
                .getContentType()), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<ServeExportResult> serveExport(
        RepositoryRequestContext context, ExportReference exportReference,
        HttpServletResponse httpResponse) {

        // Validate the export reference.
        final ExportReferenceValidationResult validationResult =
            ExportReferenceValidationResult.forReference(exportReference);
        if (validationResult.getRepositoryStatus().isError()) {
            return ResponseUtil.loggedError(validationResult.getRepositoryStatus(),
                validationResult.getStatusDescription(), log);
        }

        // Lookup the export entity and fetch the blob key.
        PersistenceManager pm = Services.datastore.getPersistenceManager();
        final BlobKey blobKey;
        try {
            final JdoExportItemEntity entity =
                pm.getObjectById(JdoExportItemEntity.class, validationResult.getExportKey());
            blobKey = checkNotNull(entity.getBlobKey(), "null blobkey");
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND, "Could not locate export: "
                + exportReference, log, e);
        } finally {
            pm.close();
        }

        // Serve the export from Blobstore.
        try {
            Services.blobstore.serve(blobKey, httpResponse);
        } catch (IOException e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                "Error serving the resource content: " + exportReference, log, e);
        }

        // TODO(tal): should do here the same header trick as in serving a resource?
        return ResponseUtil.loggedOk("Resource served: " + exportReference.toString(),
            new ServeExportResult(), log);
    }
}
