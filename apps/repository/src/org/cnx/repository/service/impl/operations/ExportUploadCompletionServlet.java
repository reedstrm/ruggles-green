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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.google.appengine.api.blobstore.BlobKey;

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
    private static final Logger log = Logger.getLogger(ExportUploadCompletionServlet.class
        .getName());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final ExportScopeType scopeType =
            ParamUtil.paramToEnum(ExportScopeType.class, req.getParameter("scope"));
        final String objectId = req.getParameter("id");
        final ExportType exportType =
            ExportTypesConfiguration.getExportTypes().get(req.getParameter("type"));
        final String versionNumberParam = req.getParameter("version");
        final Integer versionNumber =
            versionNumberParam.equals("null") ? null : Integer.valueOf(versionNumberParam);

        // Validate the export reference. Just in case, even though it was validate by the get
        // upload URL method.
        final ExportReference exportReference =
            new ExportReference(scopeType, objectId, versionNumber, exportType.getId());
        final ExportReferenceValidationResult validationResult =
            ExportReferenceValidationResult.validateReference(exportReference);
        checkArgument(validationResult.getRepositoryStatus().isOk(),
            "Invalid export reference: %s, error: %s", exportReference, validationResult
                .getStatusDescription());

        // Get blob id from the request
        // TODO(tal): move to common place and share with resource uploader
        final Map<String, BlobKey> blobs = Services.blobstore.getUploadedBlobs(req);
        if (blobs.size() != 1) {
            final String message =
                "Resource factory completion handler expected to find exactly one blob but found ["
                    + blobs.size() + "]";
            log.severe(message);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }
        final BlobKey blobKey = (BlobKey) blobs.values().toArray()[0];
        checkNotNull(blobKey);

        PersistenceManager pm = Services.datastore.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        tx.begin();
        try {
            // Verify within a transaction that the parent entity still exists.
            @SuppressWarnings({ "unused", "unchecked" })
            final CnxJdoEntity parentEntity =
                (CnxJdoEntity) pm.getObjectById(validationResult.getParentEntityClass(),
                    validationResult.getParentKey());

            // TODO(tal): if export already exists, delete old blob. Currently we orphan it.
            // TODO(tal): verify blob param and delete if bad (type mismatch, size, etc).

            JdoExportItemEntity exportItemEntity =
                new JdoExportItemEntity(validationResult.getExportKey(), blobKey);

            pm.makePersistent(exportItemEntity);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            final String message = "Error when writing export item: " + e.getMessage();
            log.log(Level.SEVERE, message, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            return;
        } finally {
            pm.close();
        }

        log.info("Written export " + exportReference);
        // TODO(tal): is this is where we want to redirect to?
        resp.sendRedirect("/");
    }
}
