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

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoResourceEntity;
import org.cnx.util.Assertions;

import com.google.appengine.api.datastore.Key;

/**
 * An API servlet to create a new resource.
 * 
 * TODO(tal): describe in more details.
 * 
 * @author Tal Dayan
 */
public class CreateResourceServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final Long resourceId;
        PersistenceManager pm = Services.datastore.getPersistenceManager();

        try {
            final JdoResourceEntity entity = new JdoResourceEntity();
            entity.idleToPendingTransition();
            // The unique resource id is created the first time the entity is persisted.
            pm.makePersistent(entity);
            resourceId = Assertions.checkNotNull(entity.getId(), "Null resource id");;
        } finally {
            pm.close();
        }

        final String resourceIdString = JdoResourceEntity.resoureIdToString(resourceId);
        final String completionUrl = "/resourcefactory/uploaded/" + resourceIdString;
        final String uploadUrl = Services.blobstore.createUploadUrl(completionUrl);

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("resource id: " + resourceIdString);
        out.println("upload url: " + uploadUrl);
    }
}