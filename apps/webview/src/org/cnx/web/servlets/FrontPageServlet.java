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

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cnx.common.repository.atompub.CnxMediaTypes;
import org.cnx.web.CommonHack;
import org.cnx.web.WebViewConfiguration;
import org.cnx.web.WebViewTemplate;

@SuppressWarnings("serial")
public class FrontPageServlet extends HttpServlet {
    private Injector injector;

    @Override public void init(final ServletConfig config) {
        this.injector = (Injector)config.getServletContext().getAttribute(Injector.class.getName());
    }

    @Override public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final SoyTofu tofu = injector.getInstance(Key.get(SoyTofu.class, WebViewTemplate.class));
        final WebViewConfiguration configuration = injector.getInstance(WebViewConfiguration.class);
        resp.setContentType(CnxMediaTypes.TEXT_HTML_UTF8);
        resp.getWriter().print(tofu.render(CommonHack.FRONT_PAGE_TEMPLATE_NAME, new SoyMapData(
                "collectionUri", configuration.getRichsCollectionUrl()), null));
    }
}
