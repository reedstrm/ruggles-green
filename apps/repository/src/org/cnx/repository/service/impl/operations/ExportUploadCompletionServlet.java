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
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.ExportReference;
import org.cnx.repository.service.api.ExportScopeType;
import org.cnx.repository.service.api.ExportType;
import org.cnx.repository.service.impl.configuration.ExportTypesConfiguration;
import org.cnx.repository.service.impl.schema.CnxJdoEntity;
import org.cnx.repository.service.impl.schema.JdoExportItemEntity;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

/**
 * An internal API servlet to handle the completion call back of a resource upload to the blobstore.
 *
 * TODO(tal): add code to verify that the request is indeed from the blobstore service.
 *
 * TODO(tal): validate the blob (e.g. against max size and reject if does not pass).
 *
 * TODO(tal): verify the uploading user against the URL creating user and reject if failed.
 *
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class ExportUploadCompletionServlet extends HttpServlet {
    /**
     * Max allowed export size in bytes. This is an arbitrary limit.
     */
    private static final long MAX_EXPORT_SIZE = 100000000;

    private static final Logger log = Logger.getLogger(ExportUploadCompletionServlet.class
        .getName());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final Map<String, BlobKey> incomingBlobs = Services.blobstore.getUploadedBlobs(req);

        final PersistenceManager pm = Services.datastore.getPersistenceManager();
        final Transaction tx = pm.currentTransaction();

        // List of blobs to delete upon return. We update it as we go. At any point that
        // can cause an exception or return, it is set to contains exactly the (possibly empty)
        // list of blobs that should be deleted.
        //
        // TODO(tal): since blob deletion and data store entity update cannot be done in
        // one atomic transaction, we err on the safe side and prefer to leave garbage blobs
        // rahter than breaking blob references in active exports. If we will have a significant
        // number of garbage blobs, consider to implement a garbage collection or another safe
        // mechanism.
        //
        final List<BlobKey> blobsToDeleteOnExit = Lists.newArrayList(incomingBlobs.values());

        ExportReference exportReference;

        // NOTE(tal): this try/catch/finaly clause is used not only to handle exception but also
        // to delete unused blobs when leaving the method.
        try {
            exportReference = parseExportReference(req);

            // Validate incoming export reference
            final ExportReferenceValidationResult validationResult =
                ExportReferenceValidationResult.validateReference(exportReference);
            checkArgument(validationResult.getRepositoryStatus().isOk(),
                    "Invalid export reference: %s, error: %s", exportReference,
                    validationResult.getStatusDescription());

            // We expect exactly one blob
            if (incomingBlobs.size() != 1) {
                setServletError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Resource upload completion handler expected to find exactly one blob but found ["
                            + incomingBlobs.size() + "]", null, log, Level.WARNING);
                return;
            }

            // Here we have exactly one incoming blob.
            final BlobKey newBlobKey = (BlobKey) incomingBlobs.values().toArray()[0];
            checkNotNull(newBlobKey);

            // Validate blob info
            final BlobInfo blobInfo = Services.blobInfoFactory.loadBlobInfo(newBlobKey);
            if (blobInfo == null) {
                setServletError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Could not find info of new blob: " + newBlobKey, null, log, Level.SEVERE);
                return;
            }
            if (blobInfo.getSize() > MAX_EXPORT_SIZE) {
                setServletError(resp, HttpServletResponse.SC_NOT_ACCEPTABLE, "Export too large: "
                    + blobInfo + " vs. " + MAX_EXPORT_SIZE, null, log, Level.WARNING);
                return;
            }
            if (!validationResult.getExportType().getContentType()
                .equals(blobInfo.getContentType())) {
                setServletError(resp, HttpServletResponse.SC_NOT_ACCEPTABLE,
                        "Expected content type "
                            + validationResult.getExportType().getContentType() + ", found "
                            + blobInfo.getContentType(), null, log, Level.WARNING);
                return;
            }

            // Everything looks good. Time to hit the data store.
            tx.begin();

            // Verify within a transaction that the parent entity still exists.
            // This throws an exception of it does not. We don't consider this
            // to be a user error but a server error since we already verified when providing
            // the upload URL so it is treated as a server error.
            @SuppressWarnings({ "unused", "unchecked" })
            final CnxJdoEntity parentEntity =
                (CnxJdoEntity) pm.getObjectById(validationResult.getParentEntityClass(),
                        validationResult.getParentKey());

            // Lookup for existing export entity, if found then overwrite, otherwise create a
            // new one.
            @Nullable
            BlobKey oldBlobKey = null;
            JdoExportItemEntity exportItemEntity;
            try {
                exportItemEntity =
                    pm.getObjectById(JdoExportItemEntity.class, validationResult.getExportKey());
                // Save the old blob key and overwrite with the new one.
                oldBlobKey = checkNotNull(exportItemEntity.getBlobKey());
                exportItemEntity.setBlobKey(newBlobKey);

            } catch (JDOObjectNotFoundException e) {
                exportItemEntity =
                    new JdoExportItemEntity(validationResult.getExportKey(), newBlobKey);
                pm.makePersistent(exportItemEntity);
            }

            tx.commit();

            // Now that we committed with reference to the new blob, we need to delete the old
            // one if found.
            blobsToDeleteOnExit.clear();
            if (oldBlobKey != null) {
                blobsToDeleteOnExit.add(oldBlobKey);
            }
        } catch (Throwable e) {
            tx.rollback();
            final String message = "Error when writing export item: " + e.getMessage();
            log.log(Level.SEVERE, message, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            return;
        } finally {
            checkArgument(!tx.isActive(), "Transaction left active: %s", req.getQueryString());
            pm.close();
            for (BlobKey key : blobsToDeleteOnExit) {
                log.info("Deleting blob: " + key);
                Services.blobstore.delete(key);
            }
        }

        log.info("Written export " + exportReference);
        // TODO(tal): is this is where we want to redirect to?
        resp.sendRedirect("/");
    }

    /**
     * Construct export reference from the incoming request.
     */
    private static ExportReference parseExportReference(HttpServletRequest req) {
        final ExportScopeType scopeType =
            ParamUtil.paramToEnum(ExportScopeType.class, req.getParameter("scope"));
        final String objectId = req.getParameter("id");
        final ExportType exportType =
            ExportTypesConfiguration.getExportTypes().get(req.getParameter("type"));
        final String versionNumberParam = req.getParameter("version");
        final Integer versionNumber =
            versionNumberParam.equals("null") ? null : Integer.valueOf(versionNumberParam);

        final ExportReference exportReference =
            new ExportReference(scopeType, objectId, versionNumber, exportType.getId());
        return exportReference;
    }

    /**
     * Setup to return a servlet error status.
     *
     * @param resp the servlet response.
     * @param httpStatus the http status to return
     * @param message diagnostic text message to return
     * @param e optional exception to log, ignored if null.
     * @param log the logger to use.
     * @param level the log level to use.
     * @throws IOException
     */
    // TODO(tal): move to a common class and share with the resource completion servlet.
    private static void setServletError(HttpServletResponse resp, int httpStatus, String message,
            @Nullable Throwable e, Logger log, Level level) throws IOException {
        final String httpMessage;
        if (e != null) {
            log.log(level, message, e);
            httpMessage = message + " " + e.getMessage();
        } else {
            log.log(level, message);
            httpMessage = message;
        }
        resp.sendError(httpStatus, httpMessage);
    }
}
