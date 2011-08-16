/*
 *  Copyright 2011 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cnx.web;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton public class FrontPageServlet extends HttpServlet {
    private static final String TEMPLATE_NAME = "org.cnx.web.index";
    private static final String MIME_TYPE = "text/html; charset=utf-8";

    protected final SoyTofu tofu;

    @Inject public FrontPageServlet(@WebViewTemplate SoyTofu tofu) {
        this.tofu = tofu;
    }

    @Override public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType(MIME_TYPE);
        resp.getWriter().print(tofu.render(TEMPLATE_NAME, new SoyMapData(), null));
    }
}
