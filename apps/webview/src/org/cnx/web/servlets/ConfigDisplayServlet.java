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

import static org.cnx.web.WebViewConfiguration.getPropertyHtmlString;

import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.Injector;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cnx.common.repository.ContentType;
import org.cnx.web.WebViewConfiguration;

/**
 * Servlet to display configuration information for WebViewer.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ConfigDisplayServlet extends HttpServlet {
    private Injector injector;
    private WebViewConfiguration configuration;

    @Override
    public void init(final ServletConfig config) {
        this.injector =
                (Injector) config.getServletContext().getAttribute(Injector.class.getName());
        configuration = injector.getInstance(WebViewConfiguration.class);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        StringBuilder builder = new StringBuilder();

        builder.append(getPropertyHtmlString("application.id",
                SystemProperty.Environment.applicationId.get()));
        builder.append(getPropertyHtmlString("application.version",
                SystemProperty.applicationVersion.get()));
        builder.append(getPropertyHtmlString("appengine.environment", SystemProperty.environment
                .get()));
        builder
                .append(getPropertyHtmlString("appengine.sdk.version", SystemProperty.version.get()));

        builder.append(configuration.getConfiguration());

        resp.setContentType(ContentType.TEXT_HTML_UTF8);
        resp.getWriter().print(builder.toString());
    }
}
