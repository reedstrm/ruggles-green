/*
    Copyright 2011 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.cnx.web;

import com.google.common.collect.ImmutableSet;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.io.*;
import javax.servlet.ServletConfig;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.cnx.html.CNXML;
import org.cnx.html.ContentMathMLProcessor;
import org.cnx.html.HTMLGenerator;
import org.cnx.html.Processor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
    HTMLRenderServlet is a simple servlet that displays a form for pasting in XML
    and then renders it upon POST.

    This servlet is only for testing, it will not be used in the final viewer.
*/
public class HTMLRenderServlet extends HttpServlet {
    private static final String mimeType = "text/html; charset=utf-8";
    private static final String sourceParam = "source";

    private SoyTofu tofu;
    private HTMLGenerator htmlGenerator;

    @Override public void init(ServletConfig config) {
        htmlGenerator = new HTMLGenerator(ImmutableSet.<Processor>of(new ContentMathMLProcessor()));

        SoyFileSet.Builder builder = new SoyFileSet.Builder();
        builder.add(new File("base.soy"));
        builder.add(new File("web.soy"));
        tofu = builder.build().compileToJavaObj().forNamespace("org.cnx.web");
    }

    @Override public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType(mimeType);
        resp.getWriter().print(tofu.render(".submitForm", new SoyMapData(), null));
    }

    @Override public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // Parse source
        final String source = req.getParameter(sourceParam);
        DocumentBuilder builder;
        Document sourceDoc;

        try {
            builder = CNXML.getBuilder();
        } catch (ParserConfigurationException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           "XML parser could not be set up");
            return;
        }
        try {
            sourceDoc = builder.parse(new ByteArrayInputStream(source.getBytes()));
        } catch (SAXException e) {
            final SoyMapData params = new SoyMapData("source", source, "reason", e.toString());
            resp.setContentType(mimeType);
            resp.getWriter().print(tofu.render(".renderFailed", params, null));
            return;
        }

        // Generate HTML
        String docHtml = null;
        try {
            docHtml = htmlGenerator.generate(sourceDoc);
        } catch (Exception e) {
            final SoyMapData params = new SoyMapData("source", source, "reason", e.toString());
            resp.setContentType(mimeType);
            resp.getWriter().print(tofu.render(".renderFailed", params, null));
            return;
        }

        // Render response
        final SoyMapData params = new SoyMapData("source", source, "docHtml", docHtml);
        resp.setContentType(mimeType);
        resp.getWriter().print(tofu.render(".render", params, null));
    }
}
