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

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.cnxml.LinkResolver;
import org.cnx.common.collxml.Collection;
import org.cnx.common.collxml.CollectionHTMLGenerator;
import org.cnx.common.collxml.CollxmlNamespace;
import org.cnx.common.collxml.ModuleLink;
import org.cnx.mdml.Actor;
import org.cnx.mdml.Metadata;
import org.cnx.util.RenderScope;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.net.URI;

@Singleton public class RenderCollectionServlet extends HttpServlet {
    private static final String TEMPLATE_NAME = "org.cnx.web.collection";
    private static final String PREFIX = "/light/collection/";
    private static final String MIME_TYPE = "text/html; charset=utf-8";

    private static final Logger log = Logger.getLogger(RenderCollectionServlet.class.getName());

    private final SoyTofu tofu;
    private final XmlFetcher fetcher;
    private final Provider<CollectionHTMLGenerator> generatorProvider;
    private final LinkResolver linkResolver;
    private final RenderScope renderScope;
    private final String collxmlNamespace;

    @Inject public RenderCollectionServlet(@WebViewTemplate SoyTofu tofu, XmlFetcher fetcher,
            Provider<CollectionHTMLGenerator> generatorProvider, RenderScope renderScope,
            LinkResolver linkResolver, @CollxmlNamespace String collxmlNamespace) {
        this.tofu = tofu;
        this.fetcher = fetcher;
        this.generatorProvider = generatorProvider;
        this.renderScope = renderScope;
        this.linkResolver = linkResolver;
        this.collxmlNamespace = collxmlNamespace;
    }

    @Override public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final String[] components = parsePath(req.getServletPath());
        final String collectionId = components[0];
        final String version = components[1];

        // Fetch collection
        Collection coll;
        try {
            coll = fetcher.fetchCollectionVersion(collectionId, version);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error while fetching", e);
            // TODO(light): 404 or 500
            return;
        }

        // Get metadata
        String title = "", abstractText = null;
        List<Actor> authors = null;
        final Metadata metadata = coll.getMetadata();
        if (metadata != null) {
            try {
                title = metadata.getTitle();
                abstractText = metadata.getAbstract();
                authors = metadata.getAuthors();
            } catch (Exception e) {
                log.log(Level.WARNING, "Error while getting metadata", e);
                // TODO(light): 500
                return;
            }
        }

        // Render content
        String contentHtml;
        try {
            contentHtml = renderContent(coll);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error while rendering", e);
            // TODO(light): 500
            return;
        }

        // Get start link
        URI firstModuleUri = null;
        renderScope.enter();
        try {
            renderScope.seed(Collection.class, coll);
            if (!coll.getModuleLinks().isEmpty()) {
                final ModuleLink link = coll.getModuleLinks().get(0);
                firstModuleUri = linkResolver.resolveDocument(
                        link.getModuleId(), link.getModuleVersion());
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error while resolving first module link", e);
            // TODO(light): 500
            return;
        } finally {
            renderScope.exit();
        }

        resp.setContentType(MIME_TYPE);
        final SoyMapData params = new SoyMapData(
                "collection", new SoyMapData(
                        "id", collectionId,
                        "version", version,
                        "title", title,
                        "abstract", abstractText,
                        "authors", Utils.convertActorListToSoyData(authors),
                        "contentHtml", contentHtml
                ),
                "firstModuleUri", (firstModuleUri != null ? firstModuleUri.toString() : null)
        );
        resp.getWriter().print(tofu.render(TEMPLATE_NAME, params, null));
    }

    /** Returns an array of [moduleId, version]. **/
    private String[] parsePath(final String path) {
        final String[] components = path.substring(PREFIX.length()).split("/");
        return new String[]{components[0], components[1]};
    }

    private String renderContent(Collection coll) throws Exception {
        renderScope.enter();
        try {
            renderScope.seed(Collection.class, coll);
            return generatorProvider.get().generate(coll);
        } finally {
            renderScope.exit();
        }
    }
}
