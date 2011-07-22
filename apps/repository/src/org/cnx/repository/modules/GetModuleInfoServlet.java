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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoModuleEntity;

/**
 * An API servlet to get general information about a module.
 * 
 * TODO(tal): provide more details.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class GetModuleInfoServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(GetModuleInfoServlet.class.getName());

    private static final Pattern uriPattern = Pattern.compile("/module_info/([a-zA-Z0-9_-]+)");

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Parse request resource id from the query.
        // TODO(tal): refactor out module id parsing and share with other
        // servlets.
        final String moduleUri = req.getRequestURI();
        final Matcher matcher = uriPattern.matcher(moduleUri);
        if (!matcher.matches()) {
            final String message = "Could not parse module id in request URI [" + moduleUri + "]";
            log.log(Level.SEVERE, message);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }
        final String moduleIdString = matcher.group(1);

        final Long moduleId = JdoModuleEntity.stringToModuleId(moduleIdString);
        if (moduleId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid module id format: ["
                + moduleIdString + "]");
            return;
        }
        log.info("Module id: " + moduleId + ", moduleIdString: " + moduleIdString);

        PersistenceManager pm = Services.datastore.getPersistenceManager();

        final JdoModuleEntity moduleEntity;

        try {
            moduleEntity = pm.getObjectById(JdoModuleEntity.class, moduleId);
        } catch (Throwable e) {
            // TODO(tal): share a common message between resp and log?
            log.log(Level.SEVERE, "Could not find module by id " + moduleIdString, e);
            resp.sendError(HttpServletResponse.SC_NO_CONTENT,
                "Error looking up a module: " + e.getMessage());
            return;
        } finally {
            pm.close();
        }

        // All done OK. Return response.
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        // out.println();
        out.println("Module Info");

        out.println("* ID = " + moduleIdString);
        out.println("* Versions = " + moduleEntity.getVersionCount());
    }
}
