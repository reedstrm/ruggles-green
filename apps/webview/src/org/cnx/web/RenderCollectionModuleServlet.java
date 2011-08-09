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

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cnx.cnxml.CnxmlNamespace;
import org.cnx.cnxml.LinkResolver;
import org.cnx.cnxml.Module;
import org.cnx.cnxml.ModuleHTMLGenerator;
import org.cnx.common.collxml.Collection;
import org.cnx.util.DOMUtils;
import org.cnx.util.RenderScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  This servlet renders a module in the context of a collection.
 */
@Singleton public class RenderCollectionModuleServlet extends RenderModuleServlet {
    private static final String TEMPLATE_NAME = "org.cnx.web.collectionModule";
    private static final String PREFIX = "/light/collection/";
    private static final String MIME_TYPE = "text/html; charset=utf-8";

    private static final Logger log =
            Logger.getLogger(RenderCollectionModuleServlet.class.getName());

    private final Provider<LinkResolver> linkResolverProvider;

    @Inject public RenderCollectionModuleServlet(@WebViewTemplate SoyTofu tofu, XmlFetcher fetcher,
            Provider<ModuleHTMLGenerator> generatorProvider,
            Provider<LinkResolver> linkResolverProvider, RenderScope renderScope,
            @CnxmlNamespace String cnxmlNamespace) {
        super(tofu, fetcher, generatorProvider, renderScope, cnxmlNamespace);
        this.linkResolverProvider = linkResolverProvider;
    }

    @Override public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final String[] components = parsePath(req.getServletPath());
        final String collectionId = components[0];
        final String collectionVersion = components[1];
        final String moduleId = components[2];
        final String moduleVersion = components[3];

        final MemcacheService cache = MemcacheServiceFactory.getMemcacheService();

        // Fetch collection
        Collection collection;
        try {
            collection = fetcher.fetchCollectionVersion(collectionId, collectionVersion);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error while fetching collection", e);
            // TODO(light): 404 or 500
            return;
        }

        // Ensure module is part of the collection
        final Collection.ModuleLink currentModuleLink = collection.getModuleLink(moduleId);
        if (currentModuleLink == null) {
            log.log(Level.INFO, "Collection " + collectionId +
                    " does not contain module " + moduleId);
            // TODO(light): 404
            return;
        }

        // Fetch module
        Module module;
        try {
            module = fetcher.fetchModuleVersion(moduleId, moduleVersion);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error while fetching module", e);
            // TODO(light): 404 or 500
            return;
        }

        final Collection.ModuleLink[] links = collection.getPreviousNext(moduleId);
        URI previousModuleUri = null, nextModuleUri = null;
        String collectionTitle = null, moduleTitle;
        String moduleContentHtml;
        final String moduleContentHtmlCacheKey = "moduleContentHtml "
                + collectionId + " " + moduleId;

        renderScope.enter();
        try {
            renderScope.seed(Collection.class, collection);
            renderScope.seed(Module.class, module);

            moduleContentHtml = (String)cache.get(moduleContentHtmlCacheKey);
            if (moduleContentHtml == null) {
                log.info("moduleContentHtml missed cache");
                moduleContentHtml = renderModuleContent(module);
                cache.put(moduleContentHtmlCacheKey, moduleContentHtml);
            } else {
                log.info("moduleContentHtml hit cache");
            }

            // Get collection title
            if (collection.getMetadata() != null) {
                collectionTitle = collection.getMetadata().getTitle();
            }

            // Get module title
            if (currentModuleLink.getMetadata() != null) {
                moduleTitle = currentModuleLink.getMetadata().getTitle();
            } else {
                moduleTitle = module.getTitle();
            }

            // Get collection previous/next links
            final LinkResolver linkResolver = linkResolverProvider.get();
            if (links[0] != null) {
                previousModuleUri = getModuleLinkUri(linkResolver, links[0]);
            }
            if (links[1] != null) {
                nextModuleUri = getModuleLinkUri(linkResolver, links[1]);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error while rendering", e);
            // TODO(light): 500
            return;
        } finally {
            renderScope.exit();
        }

        resp.setContentType(MIME_TYPE);
        final SoyMapData params = new SoyMapData(
                "collection", new SoyMapData(
                        "id", collectionId,
                        "version", collectionVersion,
                        "title", collectionTitle
                ),
                "module", new SoyMapData(
                        "id", moduleId,
                        "version", moduleVersion,
                        "title", moduleTitle,
                        "contentHtml", moduleContentHtml
                ),
                "previousModule", convertModuleLinkToSoyData(links[0], previousModuleUri),
                "nextModule", convertModuleLinkToSoyData(links[1], nextModuleUri)
        );
        resp.getWriter().print(tofu.render(TEMPLATE_NAME, params, null));
    }

    private static final URI getModuleLinkUri(LinkResolver linkResolver,
            Collection.ModuleLink link) throws Exception {
        return linkResolver.resolveDocument(link.getModuleId(), link.getModuleVersion());
    }

    private static final SoyData convertModuleLinkToSoyData(@Nullable Collection.ModuleLink link,
            @Nullable URI uri) {
        if (link == null) {
            return SoyData.createFromExistingData(null);
        }
        String title = null;
        try {
            title = link.getMetadata().getTitle();
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not obtain title for module link", e);
        }
        return new SoyMapData(
                "id", link.getModuleId(),
                "version", link.getModuleVersion(),
                "title", title,
                "uri", (uri != null ? uri.toString() : null)
        );
    }

    /** Returns an array of [collectionId, collectionVersion, moduleId, moduleVersion]. **/
    private String[] parsePath(final String path) {
        // Incoming path is collectionId/collectionVersion/ "module" /moduleId/moduleVersion/
        final String[] components = path.substring(PREFIX.length()).split("/");
        return new String[]{components[0], components[1], components[3], components[4]};
    }
}
