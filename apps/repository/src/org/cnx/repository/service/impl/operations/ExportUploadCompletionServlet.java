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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.ExportReference;
import org.cnx.repository.service.impl.persistence.OrmEntity;
import org.cnx.repository.service.impl.persistence.OrmExportItemEntity;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

/**
 * An internal API servlet to handle the completion call back of a resource upload to the blobstore.
 * 
 * TODO(tal): add code to verify that the request is indeed from the blobstore service.
 * 
 * TODO(tal): verify the uploading user against the identity of the user that created the upload URL
 * and reject if does not match.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class ExportUploadCompletionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ExportUploadCompletionServlet.class
            .getName());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final Map<String, BlobKey> incomingBlobs = Services.blobstore.getUploadedBlobs(req);

        // List of blobs to delete upon return, each associated with a reason describing
        // why it is deleted. We update it as we go. At any point that can cause an exception
        // or return, it is set to contains exactly the (possibly empty) list of blobs that
        // should be deleted.
        //
        // TODO(tal): *** since blob deletion and data store entity update cannot be done in
        // one atomic transaction, we err on the safe side and prefer to leave garbage blobs
        // rahter than breaking blob references in active exports. If we will have a significant
        // number of garbage blobs, consider to implement a garbage collection or another safe
        // mechanism.
        //
        final List<Pair<BlobKey, String>> blobsToDeleteOnExit = Lists.newArrayList();
        for (BlobKey blobKey : incomingBlobs.values()) {
            blobsToDeleteOnExit.add(Pair.of(blobKey, "Unused incoming export blob"));
        }

        ExportReference exportReference;

        // NOTE(tal): this try/catch/finally clause is used not only to handle exception but also
        // to delete unused blobs when leaving the method.
        Transaction tx = null;
        try {
            exportReference = ExportUtil.exportReferenceFromRequestParameters(req);

            // Validate incoming export reference
            final ExportReferenceValidationResult validationResult =
                    ExportReferenceValidationResult.validateReference(exportReference);
            checkArgument(validationResult.getRepositoryStatus().isOk(),
                    "Invalid export reference: %s, error: %s", exportReference,
                    validationResult.getStatusDescription());

            // We expect exactly one blob
            if (incomingBlobs.size() != 1) {
                ServletUtil.setServletError(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Resource upload completion handler "
                                + "expected to find exactly one blob but found ["
                                + incomingBlobs.size() + "]", null, log, Level.WARNING);
                return;
            }

            // Here we have exactly one incoming blob.
            final BlobKey newBlobKey = (BlobKey) incomingBlobs.values().toArray()[0];
            checkNotNull(newBlobKey);

            // Validate blob info
            //
            // NOTE(tal): it is important to fetch the blob info outside of the transaction
            // since it is not in the same entity group as the resource entity we fetch below.
            final BlobInfo blobInfo = Services.blobInfoFactory.loadBlobInfo(newBlobKey);
            if (blobInfo == null) {
                ServletUtil.setServletError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Could not find info of new blob: " + newBlobKey, null, log, Level.SEVERE);
                return;
            }
            if (blobInfo.getSize() > validationResult.getExportType().getMaxSizeInBytes()) {
                ServletUtil.setServletError(resp, HttpServletResponse.SC_NOT_ACCEPTABLE,
                        "Export too large: " + blobInfo + " vs. "
                                + validationResult.getExportType().getMaxSizeInBytes() + ", export: "
                                + exportReference, null, log, Level.WARNING);
                return;
            }
            if (!validationResult.getExportType().getContentType()
                    .equals(blobInfo.getContentType())) {
                ServletUtil.setServletError(resp, HttpServletResponse.SC_NOT_ACCEPTABLE,
                        "Expected content type "
                                + validationResult.getExportType().getContentType() + ", found "
                                + blobInfo.getContentType(), null, log, Level.WARNING);
                return;
            }

            // Verify (within the transaction) that the parent entity still exists.
            // This throws an exception of it does not. We don't consider this
            // to be a user error but a server error since we already verified when providing
            // the upload URL so it is treated as a server error.
            tx = Services.persistence.beginTransaction();

            @SuppressWarnings({ "unused", "unchecked" })
            final OrmEntity parentEntity =
            (OrmEntity) Services.persistence.read(validationResult.getParentEntityClass(),
                    validationResult.getParentKey());

            // Lookup for existing export entity, if found then overwrite, otherwise create a
            // new one.
            @Nullable
            BlobKey oldBlobKey = null;
            OrmExportItemEntity exportItemEntity;
            try {
                exportItemEntity =
                        Services.persistence.read(OrmExportItemEntity.class,
                                validationResult.getExportKey());

                // Keep around the old blob key and overwrite with the new one.
                oldBlobKey = checkNotNull(exportItemEntity.getBlobKey());
                exportItemEntity.setBlobKey(newBlobKey);

            } catch (EntityNotFoundException e) {
                // New export, not overwriting an existing one.
                exportItemEntity =
                        new OrmExportItemEntity(validationResult.getExportKey(), newBlobKey);
            }
            Services.persistence.write(exportItemEntity);

            tx.commit();

            // Now that we committed with reference to the new blob, we need to delete the old
            // one if found.
            blobsToDeleteOnExit.clear();
            if (oldBlobKey != null) {
                blobsToDeleteOnExit.add(Pair.of(oldBlobKey, "Overwritten old export blob"));
            }
        } catch (Throwable e) {
            if (tx != null) {
                tx.rollback();
            }
            final String message = "Error when writing export item: " + e.getMessage();
            log.log(Level.SEVERE, message, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            return;
        } finally {
            checkArgument((tx == null) || !tx.isActive(), "Transaction left active: %s",
                    req.getQueryString());

            // Delete on exit blobs
            for (Pair<BlobKey, String> item : blobsToDeleteOnExit) {
                log.info("Deleting blob: " + item.first + " (" + item.second + ")");
                Services.blobstore.delete(item.first);
            }
        }

        log.info("Written export " + exportReference);
        resp.sendRedirect(BlobServingDoneServlet.REDIRECTION_URL);
    }
}
