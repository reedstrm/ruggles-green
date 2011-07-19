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
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoResourceEntity;

import com.google.appengine.api.blobstore.BlobInfo;

/**
 * An API servlet to serve metadata of a resource.
 * 
 * TODO(tal): provide more details.
 * 
 * @author Tal Dayan
 */
public class GetResourceInfoServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(GetResourceInfoServlet.class.getName());

    private static final Pattern uriPattern = Pattern.compile("/resourceinfo/([a-zA-Z0-9_-]+)");

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
        log.info("Resource id: " + resourceId + ", requestIdString: " + resourceIdString);

        PersistenceManager pm = Services.datastore.getPersistenceManager();
        //final BlobKey blobKey;
        
        final JdoResourceEntity entity;

        try {
            entity = pm.getObjectById(JdoResourceEntity.class, resourceId);
            if (entity.getState() != JdoResourceEntity.State.UPLOADED) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Resource info servlet expected an entity at state UPLOADED but found ["
                                + entity.getState() + "]");
                return;
            }
            //blobKey = entity.blobKey;
        } catch (Throwable e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_NO_CONTENT,
                    "Error looking up a resource: " + e.getMessage());
            return;
        } finally {
            pm.close();
        }

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        
        out.println("Resource info:");

        out.println("* resource state: " + entity.getState());
        out.println("* resource id: " + entity.getId());
        
        if (entity.getState().hasBlobKey()) {
          out.println("* blob key: " + entity.getBlobKey());
          // NOTE(tal): if performance is an issue, could cache this in the resource
          // entity upon blob uploading.
          BlobInfo blobInfo = Services.blobInfoFactory.loadBlobInfo(entity.getBlobKey());
          if (blobInfo == null) {
              out.println("*** error, could not locate info for blob key " + entity.getBlobKey());
          } else {
              out.println("* content type: " + blobInfo.getContentType());
              out.println("* file name: " + blobInfo.getFilename());
              out.println("* size: " + blobInfo.getSize());
              out.println("* creation time: " + blobInfo.getCreation());

          }
        }       
    }
}
