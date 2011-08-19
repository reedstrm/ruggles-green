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
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cnx.cnxml.CnxmlNamespace;
import org.cnx.cnxml.Module;
import org.cnx.cnxml.ModuleHTMLGenerator;
import org.cnx.mdml.Actor;
import org.cnx.util.RenderScope;

@Singleton public class RenderModuleServlet extends HttpServlet {
    private static final String TEMPLATE_NAME = "org.cnx.web.module";
    private static final String PREFIX = "/light/module/";
    private static final String MIME_TYPE = "text/html; charset=utf-8";

    private static final Logger log = Logger.getLogger(RenderModuleServlet.class.getName());

    protected final SoyTofu tofu;
    protected final XmlFetcher fetcher;
    protected final Provider<ModuleHTMLGenerator> generatorProvider;
    protected final RenderScope renderScope;
    protected final String cnxmlNamespace;

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
        Module module;
        try {
            module = fetcher.fetchModuleVersion(moduleId, version);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error while fetching", e);
            // TODO(light): 404 or 500
            return;
        }

        // Render content
        String title, contentHtml;
        List<Actor> authors;
        renderScope.enter();
        try {
            renderScope.seed(Module.class, module);
            title = module.getTitle();
            if (module.getMetadata() != null) {
                authors = module.getMetadata().getAuthors();
            } else {
                authors = Collections.<Actor>emptyList();
            }
            contentHtml = renderModuleContent(module);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error while rendering", e);
            // TODO(light): 500
            return;
        } finally {
            renderScope.exit();
        }

        resp.setContentType(MIME_TYPE);
        final SoyMapData params = new SoyMapData(
                "module", new SoyMapData(
                        "id", moduleId,
                        "version", version,
                        "title", title,
                        "authors", Utils.convertActorListToSoyData(authors),
                        "contentHtml", contentHtml
                )
        );
        resp.getWriter().print(tofu.render(TEMPLATE_NAME, params, null));
    }

    /** Returns an array of [moduleId, version]. **/
    private String[] parsePath(final String path) {
        final String[] components = path.substring(PREFIX.length()).split("/");
        return new String[]{components[0], components[1]};
    }

    protected String renderModuleContent(Module module) throws Exception {
        return generatorProvider.get().generate(module);
    }
}
