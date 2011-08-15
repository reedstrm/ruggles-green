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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.impl.persistence.OrmResourceEntity;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

/**
 * An internal API servlet to handle the completion call back of resource upload to the blobstore.
 * 
 * TODO(tal): add code to verify that the request is indeed from the blobstore service.
 * 
 * TODO(tal): verify the uploading user against the resource creating user and reject if failed.
 * 
 * @author Tal Dayan
 * 
 */

@SuppressWarnings("serial")
public class ResourceUploadCompletionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ResourceUploadCompletionServlet.class
        .getName());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final Map<String, BlobKey> incomingBlobs = Services.blobstore.getUploadedBlobs(req);

        // List of blobs to delete upon return, each associated with a reason describing
        // why it is deleted. We update it as we go. At any point that can cause an exception
        // or return, it is set to contains exactly the (possibly empty) list of blobs that
        // should be deleted.
        //
        // TODO(tal): since blob deletion and data store entity update cannot be done in
        // one atomic transaction, we err on the safe side and prefer to leave garbage blobs
        // rather than breaking blob references in active exports. If we will have a significant
        // number of garbage blobs, consider to implement a garbage collection or another safe
        // mechanism.
        //
        final List<Pair<BlobKey, String>> blobsToDeleteOnExit = Lists.newArrayList();
        for (BlobKey blobKey : incomingBlobs.values()) {
            blobsToDeleteOnExit.add(Pair.of(blobKey, "Unused incoming resource blob"));
        }

        final String resourceId = ResourceUtil.getResourceIdParam(req, "");

        // NOTE(tal): this try/catch/finally clause is used not only to handle exception but also
        // to delete unused blobs when leaving the method.
        Transaction tx = null;
        try {
            // Convert encoded resource id to internal resource id
            final Key resourceKey = OrmResourceEntity.resourceIdToKey(resourceId);
            // NOTE(tal): this can happen only due to programming error since it is a callback
            // from blobstore.
            checkArgument(resourceKey != null, "Invalid resource id: [%s]", resourceId);

            // Get blob id from the request
            if (incomingBlobs.size() != 1) {
                final String message =
                    "Resource factory completion handler expected to find "
                        + "exactly one blob but found [" + incomingBlobs.size() + "]";
                log.severe(message);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
                return;
            }
            BlobKey blobKey = (BlobKey) incomingBlobs.values().toArray()[0];

            // Validate blob info
            //
            // NOTE(tal): it is important to fetch the blob info outside of the transaction
            // since it is not in the same entity group as the resource entity we fetch below.
            final BlobInfo blobInfo = Services.blobInfoFactory.loadBlobInfo(blobKey);
            if (blobInfo == null) {
                ServletUtil.setServletError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Could not find info of incoming resource blob: " + blobKey.toString(),
                        null, log, Level.SEVERE);
                return;
            }
            if (blobInfo.getSize() > Services.config.getMaxResourceSize()) {
                ServletUtil.setServletError(
                        resp,
                        HttpServletResponse.SC_NOT_ACCEPTABLE,
                        "Export too large: " + blobInfo + " vs. "
                            + Services.config.getMaxResourceSize(), null, log, Level.WARNING);
                return;
            }

            // TODO(tal): if needed, add here validation of resource content type (available from
            // blobkInfo). We can use whitelist or blacklist of content types.

            // We are done with blob info access. Start the update transaction.
            tx = checkNotNull(Services.persistence.beginTransaction());

            // Read the resource entity
            final OrmResourceEntity ormEntity =
                Services.persistence.read(OrmResourceEntity.class, resourceKey);

            // Promote the resource entity to UPLOADED state with the incoming blob.
            if (ormEntity.getState() != OrmResourceEntity.State.UPLOAD_PENDING) {
                tx.rollback();
                ServletUtil.setServletError(resp, HttpServletResponse.SC_BAD_REQUEST, "Resource "
                    + resourceId + " is not in pending upload state: " + ormEntity.getState(),
                        null, log, Level.WARNING);
                return;
            }
            ormEntity.pendingToUploadedTransition(blobKey);
            Services.persistence.write(ormEntity);

            tx.commit();
            // New blob is now in use. Make sure we don't delete it upon exit.
            blobsToDeleteOnExit.clear();
        } catch (Throwable e) {
            ServletUtil.setServletError(resp, HttpServletResponse.SC_NOT_ACCEPTABLE,
                    "Resource upload completion handler encountered an error. id = " + resourceId,
                    e, log, Level.SEVERE);
            if (tx != null) {
                tx.rollback();
            }
            return;
        } finally {
            checkArgument(tx == null || !tx.isActive(), "Transaction left active: %s",
                    req.getRequestURI());

            // Delete on exit blobs, if any
            for (Pair<BlobKey, String> item : blobsToDeleteOnExit) {
                log.info("Deleting blob: " + item.first + " (" + item.second + ")");
                Services.blobstore.delete(item.first);
            }
        }

        log.info("Uploaded content of resource " + resourceId);
        resp.sendRedirect("/");
    }
}
