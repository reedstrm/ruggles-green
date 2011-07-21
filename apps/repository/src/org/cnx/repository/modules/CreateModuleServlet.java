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

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoModuleEntity;
import org.cnx.util.Assertions;

/**
 * An API servlet to create a new module.
 * 
 * The module is created with zero versions. Must add at least one version before the module becomes
 * useful.
 * 
 * TODO(tal): describe in more details.
 * 
 * @author Tal Dayan
 */
public class CreateModuleServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final Long moduleId;
        PersistenceManager pm = Services.datastore.getPersistenceManager();

        try {
            final JdoModuleEntity entity = new JdoModuleEntity();
            // The unique module id is created the first time the entity is
            // persisted.
            pm.makePersistent(entity);
            moduleId = Assertions.checkNotNull(entity.getId(), "Null module id");
        } finally {
            pm.close();
        }

        final String moduleIdString = JdoModuleEntity.moduleIdToString(moduleId);

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("module id: " + moduleIdString);
    }
}