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
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.DeleteExportResult;
import org.cnx.repository.service.api.ExportReference;
import org.cnx.repository.service.api.GetExportUploadUrlResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;
import org.cnx.repository.service.api.ServeExportResult;
import org.cnx.repository.service.impl.persistence.OrmEntity;
import org.cnx.repository.service.impl.persistence.OrmExportItemEntity;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.ImmutableMap;

/**
 * Implementation of the export related operations of the repository service.
 * 
 * @author Tal Dayan
 */
public class ExportOperations {

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
            ExportReferenceValidationResult.validateReference(exportReference);
        if (validationResult.getRepositoryStatus().isError()) {
            return ResponseUtil.loggedError(validationResult.getRepositoryStatus(),
                    validationResult.getStatusDescription(), log);
        }

        // Verify that the parent entity exist.
        try {
            @SuppressWarnings({ "unused", "unchecked" })
            final OrmEntity parentEntity =
                (OrmEntity) Services.persistence.read(validationResult.getParentEntityClass(),
                        validationResult.getParentKey());
        } catch (EntityNotFoundException e) {
            return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND,
                    "Export parent object not found: " + exportReference, log, e);
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error when looking up parent entity of export: " + exportReference, log, e);
        }

        final String completionUrl =
            ExportOperations.UPLOAD_COMPLETION_SERVLET_PATH + "?"
                + ExportUtil.exportReferenceToRequestParameters(exportReference);

        // Workaround for runs locally in eclipse.
        //
        // TODO(tal): remove after blobstore get fixed (ETA Aug 15 2011)
        String uploadUrl = Services.blobstore.createUploadUrl(completionUrl);
        if (uploadUrl.startsWith("/")) {
            log.warning("Prefexing resource upload url with '" + context.hostUrl + "'");
            uploadUrl = context.hostUrl + uploadUrl;
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
            ExportReferenceValidationResult.validateReference(exportReference);
        if (validationResult.getRepositoryStatus().isError()) {
            return ResponseUtil.loggedError(validationResult.getRepositoryStatus(),
                    validationResult.getStatusDescription(), log);
        }

        // Lookup the export entity and fetch the blob key.
        final BlobKey blobKey;
        try {
            final OrmExportItemEntity entity =
                Services.persistence.read(OrmExportItemEntity.class,
                        validationResult.getExportKey());
            blobKey = checkNotNull(entity.getBlobKey(), "null blobkey");
        } catch (EntityNotFoundException e) {
            return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND, "Could not locate export: "
                + exportReference, log, e);
        } catch (Throwable e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error when looking up export: " + exportReference, log, e);
        }

        // Serve the export from Blobstore.
        try {
            Services.blobstore.serve(blobKey, httpResponse);
        } catch (IOException e) {
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error serving the resource content: " + exportReference, log, e);
        }

        final ImmutableMap<String, String> additionalHeaders =
            ImmutableMap.of(BlobstoreUtil.BLOB_KEY_HEADER_NAME, blobKey.toString());
        return ResponseUtil.loggedOk("Export served: " + exportReference.toString(),
                new ServeExportResult(additionalHeaders), log);
    }

    /**
     * See description in {@link CnxRepositoryService}
     */
    public static RepositoryResponse<DeleteExportResult> deleteExport(
            RepositoryRequestContext context, ExportReference exportReference) {

        // Validate the export reference.
        final ExportReferenceValidationResult validationResult =
            ExportReferenceValidationResult.validateReference(exportReference);
        if (validationResult.getRepositoryStatus().isError()) {
            return ResponseUtil.loggedError(validationResult.getRepositoryStatus(),
                    validationResult.getStatusDescription(), log);
        }

        final BlobKey blobKey;
        final Transaction tx = Services.persistence.beginTransaction();
        try {
            // Lookup export, return NOT FOUND it not found.
            final OrmExportItemEntity entity;
            try {
                entity =
                    Services.persistence.read(OrmExportItemEntity.class,
                            validationResult.getExportKey());
            } catch (EntityNotFoundException e) {
                tx.rollback();
                return ResponseUtil.loggedError(RepositoryStatus.NOT_FOUND, "Export not found: "
                    + exportReference, log, e);
            }

            // Export entity found. Delete it.
            blobKey = entity.getBlobKey();
            Services.persistence.delete(checkNotNull(entity.getKey()));

            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            return ResponseUtil.loggedError(RepositoryStatus.SERVER_ERRROR,
                    "Error when deleting export" + exportReference, log, e);
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active: %s", exportReference);
        }

        // TODO(tal): *** what should be the best sequence to delete the entity and its blob?
        // Ideally they should be done in one transaction but app engine does not support it.
        Services.blobstore.delete(blobKey);

        return ResponseUtil.loggedOk("Export found and deleted: " + exportReference + ", blobKey: "
            + blobKey.getKeyString(), new DeleteExportResult(), log);
    }
}
