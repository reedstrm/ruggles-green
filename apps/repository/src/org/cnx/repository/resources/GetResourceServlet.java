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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoResourceEntity;

import com.google.appengine.api.blobstore.BlobKey;

/**
 * An API servlet to serve a resource using a GET request.
 * 
 * TODO(tal): provide more details.
 * 
 * @author Tal Dayan
 */
public class GetResourceServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(GetResourceServlet.class.getName());

    private static final Pattern uriPattern = Pattern.compile("/resource/([a-zA-Z0-9_-]+)");

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Parse request resource id from the query.
        final String requestURI = req.getRequestURI();
        final Matcher matcher = uriPattern.matcher(requestURI);
        if (!matcher.matches()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Could parse resource id in request URI [" + requestURI + "]");
            return;
        }
        final String resourceIdString = matcher.group(1);

        final Long resourceId = JdoResourceEntity.stringToResourceId(resourceIdString); // KeyFactory.stringToKey(resourceId);
        if (resourceId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid resource id format: ["
                    + resourceIdString + "]");
            return;
        }

        PersistenceManager pm = Services.datastore.getPersistenceManager();
        final BlobKey blobKey;

        try {
            JdoResourceEntity entity = pm.getObjectById(JdoResourceEntity.class, resourceId);
            if (entity.getState() != JdoResourceEntity.State.UPLOADED) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Resource servlet expected an entity at state UPLOADED but found ["
                                + entity.getState() + "]");
                return;
            }
            blobKey = entity.getBlobKey();
        } catch (Throwable e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_NO_CONTENT,
                    "Error looking up a resource: " + e.getMessage());
            return;
        } finally {
            pm.close();
        }

        // Serve the resource from Blobstore.
        Services.blobstore.serve(blobKey, resp);
    }

}