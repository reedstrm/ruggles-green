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

package org.cnx.web.servlets;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cnx.common.repository.ContentType;

@SuppressWarnings("serial")
public class ClearCacheServlet extends HttpServlet {
    private static final String TEMPLATE_FILE = "clear-cache.html";

    private static final Logger log =
            Logger.getLogger(ClearCacheServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType(ContentType.TEXT_HTML_UTF8);
        ByteStreams.copy(new FileInputStream(new File(TEMPLATE_FILE)), resp.getOutputStream());
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        log.warning("Clearing the site cache!");
        final MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
        cache.clearAll();
        resp.sendRedirect("/");
    }
}
