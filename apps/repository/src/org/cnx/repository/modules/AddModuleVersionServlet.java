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

package org.cnx.repository.modules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoModuleEntity;
import org.cnx.repository.schema.JdoModuleVersionEntity;
import org.cnx.repository.schema.SchemaConsts;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * An API servlet to add a version for an existing module.
 * 
 * TODO(tal): describe in more details.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class AddModuleVersionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AddModuleVersionServlet.class.getName());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // TODO(tal): validate parameters.

        final String cnxml =
            checkNotNull(req.getParameter("cnxml"), "Missing post param \"cnxml\"");
        final String manifest =
            checkNotNull(req.getParameter("manifest"),
                "Missing post param \"manifest\"");
        final String moduleIdParam =
            checkNotNull(req.getParameter("module_id"),
                "Missing post param \"module_id\"");

        // TODO(tal): switch go Guava Preconditions and discard our own
        // Assertions.
        checkArgument(req.getParameterMap().size() == 3,
            "Expected 3 post parameters, found %s", req.getParameterMap().size());

        final Long moduleId = JdoModuleEntity.stringToModuleId(moduleIdParam);
        if (moduleId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid module id format: ["
                + moduleIdParam + "]");
            return;
        }
        log.info("Module id: " + moduleId);

        PersistenceManager pm = Services.datastore.getPersistenceManager();
        Transaction tx = pm.currentTransaction();

        final int versionNumber;

        try {
            tx.begin();

            // Read parent entity of this module
            final JdoModuleEntity moduleEntity;
            try {
                moduleEntity = pm.getObjectById(JdoModuleEntity.class, moduleId);
            } catch (Throwable e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not locate module ["
                    + moduleIdParam + "]: " + e.getMessage());
                return;
            }

            // Updated number of versions in the parent
            versionNumber = moduleEntity.incrementVersionCount();

            // Create child key
            final Key parentKey =
                KeyFactory.createKey(SchemaConsts.MODULE_KEY_KIND, moduleEntity.getId());
            final Key childKey =
                KeyFactory
                    .createKey(parentKey, SchemaConsts.MODULE_VERSION_KEY_KIND, versionNumber);

            // TODO(tal): If version already exists due to internal
            // inconsistency, report and error rather than overwriting.

            // Create new version entity
            final JdoModuleVersionEntity versionEntity =
                new JdoModuleVersionEntity(childKey, moduleId, versionNumber, cnxml, manifest);
            pm.makePersistent(versionEntity);

            tx.commit();
            // TODO(tal): (in all mutating servlets), add a log message about the change.
        } catch (Throwable e) {
            log.log(Level.SEVERE, "Exception", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "General error: [" + e.getMessage() + "]");
            return;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }

        // All done OK.
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("version number: " + versionNumber);
    }
}