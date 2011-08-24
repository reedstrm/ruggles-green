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

package org.cnx.repository.tempservlets.experimental;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.impl.operations.Services;
import org.cnx.repository.service.impl.persistence.OrmModuleEntity;

/**
 * Experimental module to test mixed mode module id allocation.
 * 
 * Example for creating moduels with forced ids:<br>
 * http://localhost:8888/experimental/create_module_range?base=1000&n=100
 * 
 * Example for creating moduels with auto ids:<br>
 * http://localhost:8888/experimental/create_module_range?n=100
 * 
 * TODO(tal): delete after completing the experiment
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class CreateModuleRangeServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(CreateModuleRangeServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        @Nullable
        final String baseParam = req.getParameter("base");
        @Nullable
        final Long base = (baseParam == null) ? null : Long.parseLong(baseParam);

        final String nParam = checkNotNull(req.getParameter("n"), "Missing n param");
        final long n = Long.parseLong(nParam);

        checkArgument(base == null || base >= 1, "Invalid base value: %s", base);
        checkArgument(n >= 0, "Invalid n value: %s", n);

        resp.setContentType("text/plain");
        final PrintWriter out = resp.getWriter();
        out.println("base: " + base);
        out.println("n:    " + n);
        out.println();

        for (long i = 0; i < n; i++) {
            final OrmModuleEntity moduleEntity = new OrmModuleEntity(new Date());
            if (base != null) {
                final String moduleId =
                    String.format("%s%s", OrmModuleEntity.getSpec().getIdPrefix(), base + i);
                moduleEntity.setKey(OrmModuleEntity.moduleIdToKey(moduleId));
            }
            Services.persistence.write(moduleEntity);
            final String msg = String.format("Wrote module %s", moduleEntity.getId());
            log.info(msg);
            out.println(msg);
        }

        out.println();
        out.println("Done");
    }
}
