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
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cnx.html.CnxmlNamespace;
import org.cnx.html.ModuleHTMLGenerator;
import org.cnx.html.RenderScope;
import org.w3c.dom.Document;
import org.cnx.util.DOMUtils;
import org.w3c.dom.Element;

@Singleton public class RenderModuleServlet extends HttpServlet {
    private static final String TEMPLATE_NAME = "org.cnx.web.module";
    private static final String PREFIX = "/module/";
    private static final String MIME_TYPE = "text/html; charset=utf-8";

    private final SoyTofu tofu;
    private final XmlFetcher fetcher;
    private final Provider<ModuleHTMLGenerator> generatorProvider;
    private final RenderScope renderScope;
    private final String cnxmlNamespace;

    @Inject public RenderModuleServlet(@WebViewTemplate SoyTofu tofu, XmlFetcher fetcher,
            Provider<ModuleHTMLGenerator> generatorProvider, RenderScope renderScope,
            @CnxmlNamespace String cnxmlNamespace) {
        this.tofu = tofu;
        this.fetcher = fetcher;
        this.generatorProvider = generatorProvider;
        this.renderScope = renderScope;
        this.cnxmlNamespace = cnxmlNamespace;
    }

    @Override public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final String[] components = parsePath(req.getServletPath());
        final String moduleId = components[0];
        final String version = components[1];

        // Fetch module
        XmlFetcher.Module module;
        try {
            module = fetcher.fetchModuleVersion(moduleId, version);
        } catch (Exception e) {
            // TODO: Log exception
            // TODO: 404 or 500
            return;
        }

        // Render content
        String title, contentHtml;
        try {
            title = getTitle(module.getModuleDocument());
            contentHtml = renderContent(moduleId, module);
        } catch (Exception e) {
            // TODO: Log exception
            // TODO: 500
            return;
        }

        resp.setContentType(MIME_TYPE);
        final SoyMapData params = new SoyMapData(
                "id", moduleId,
                "version", version,
                "title", title,
                "contentHtml", contentHtml
        );
        resp.getWriter().print(tofu.render(TEMPLATE_NAME, params, null));
    }

    /** Returns an array of [moduleId, version]. **/
    private String[] parsePath(final String path) {
        final String[] components = path.substring(PREFIX.length()).split("/");
        return new String[]{components[0], components[1]};
    }

    private String renderContent(String moduleId, XmlFetcher.Module module) throws Exception {
        renderScope.enter();
        try {
            renderScope.seed(Key.get(String.class, Names.named("moduleId")), moduleId);
            return generatorProvider.get().generate(module.getModuleDocument());
        } finally {
            renderScope.exit();
        }
    }

    private String getTitle(Document moduleDocument) throws Exception {
        Element titleElement = DOMUtils.findFirstChild(moduleDocument.getDocumentElement(),
                cnxmlNamespace, "title");
        if (titleElement == null) {
            throw new IllegalArgumentException("CNXML document has no title");
        }
        return titleElement.getTextContent();
    }
}