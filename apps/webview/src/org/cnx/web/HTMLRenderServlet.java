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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.SoyModule;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.io.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletConfig;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import org.cnx.html.DefaultModule;
import org.cnx.html.DefaultProcessorModule;
import org.cnx.html.HTMLGenerator;
import org.cnx.html.RenderScope;
import org.w3c.dom.Document;

/**
 *  HTMLRenderServlet is a simple servlet that displays a form for pasting in XML
 *  and then renders it upon POST.
 *
 *  This servlet is only for testing, it will not be used in the final viewer.
 */
public class HTMLRenderServlet extends HttpServlet {
    private static final String mimeType = "text/html; charset=utf-8";
    private static final String sourceParam = "source";
    private static final String moduleIdParam = "module";

    private SoyTofu tofu;
    private Provider<HTMLGenerator> generatorProvider;
    private Provider<DocumentBuilder> documentBuilderProvider;
    private RenderScope renderScope;

    @Override public void init(ServletConfig config) {
        Injector injector = Guice.createInjector(
                new SoyModule(),
                new DefaultModule(),
                new DefaultProcessorModule(),
                new WebViewModule()
        );
        generatorProvider = injector.getProvider(HTMLGenerator.class);
        documentBuilderProvider = injector.getProvider(DocumentBuilder.class);
        renderScope = injector.getInstance(RenderScope.class);

        SoyFileSet.Builder builder = injector.getInstance(SoyFileSet.Builder.class);
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
        final String source = req.getParameter(sourceParam);
        final String moduleId = req.getParameter(moduleIdParam);
        final String docHtml;
        try {
            docHtml = render(moduleId, source);
        } catch (Exception e) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            final SoyMapData params = new SoyMapData(
                    "source", source,
                    "moduleId", moduleId,
                    "reason", sw.toString()
            );
            resp.setContentType(mimeType);
            resp.getWriter().print(tofu.render(".renderFailed", params, null));
            return;
        }

        // Render response
        final SoyMapData params = new SoyMapData(
                "source", source,
                "moduleId", moduleId,
                "docHtml", docHtml
        );
        resp.setContentType(mimeType);
        resp.getWriter().print(tofu.render(".render", params, null));
    }

    private String render(String moduleId, String source) throws Exception {
        DocumentBuilder builder;
        Document sourceDoc;

        renderScope.enter();
        try {
            renderScope.seed(Key.get(String.class, Names.named("moduleId")), moduleId);
            builder = documentBuilderProvider.get();
            sourceDoc = builder.parse(new ByteArrayInputStream(source.getBytes()));
            return generatorProvider.get().generate(sourceDoc);
        } finally {
            renderScope.exit();
        }
    }
}
