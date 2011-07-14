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

package org.cnx.repository.resources;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoResourceEntity;
import org.cnx.repository.schema.SchemaConsts;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * An internal API servlet to handle the completion call back of resource upload
 * to the blobstore.
 * 
 * TODO(tal): add code to verify that the request is indeed from the blobstore
 * service. TODO(tal): validate the blob (e.g. against max size and reject if
 * does not pass). TODO(tal): verify the uploading user against the resource
 * creating user and reject if failed.
 * 
 * @author Tal Dayan
 * 
 */

public class UploadCompletionServlet extends HttpServlet {
    
    private static final Logger log = Logger.getLogger(UploadCompletionServlet.class.getName());

    private static final Pattern uriPattern = Pattern.compile("/resourcefactory/uploaded/([a-zA-Z0-9_-]+)");

    /**
     * Service entry point for all HTTP methods.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Parse encoded resource id from the request
        final String requestURI = req.getRequestURI();
        Matcher matcher = uriPattern.matcher(requestURI);
        if (!matcher.matches()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Resource factory completion handler could not match request URI: ["
                            + requestURI + "]");
            return;
        }
        final String resourceIdString = matcher.group(1);
        
        // Convert encoded resource id to internal resource id
        final Long resourceId = JdoResourceEntity.stringToResourceId(resourceIdString); // KeyFactory.stringToKey(resourceId);
        if (resourceId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid resource id format: ["
                    + resourceIdString + "]");
            return;
        }

        // Get blob id from the request
        Map<String, BlobKey> blobs = Services.blobstore.getUploadedBlobs(req);
        if (blobs.size() != 1) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Resource factory completion handler expected to find exactly one blob but found ["
                            + blobs.size() + "]");
            return;
        }
        BlobKey blobKey = (BlobKey) blobs.values().toArray()[0];


        // Promote the entity to UPLOADED state.
        PersistenceManager pm = Services.datastore.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {

            tx.begin();
            log.info("** Resource id: " + resourceId + ", resource id string = " + resourceIdString);
            final JdoResourceEntity entity =  pm.getObjectById(JdoResourceEntity.class, resourceId);
            if (entity.getState() != JdoResourceEntity.State.PENDING_UPLOAD) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Resource factory completion handler expected resource [" + resourceId
                                + "] to be in state PENDING_UPLOAD but found [" + entity.getState()
                                + "]");
                return;
            }
            entity.pendingToUploadedTransition(blobKey);
            tx.commit();
        } catch (Throwable e) {
            e.printStackTrace();
            if (tx.isActive()) {
                tx.rollback();
            }
            resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
                    "Resource factory completion handle encountered an exception: [" + e.getMessage()
                            + "]");
            return;
        } finally {
            pm.close();
        }
        // TODO(tal): is this is where we want to redirect?
        resp.sendRedirect("/");
    }
}
