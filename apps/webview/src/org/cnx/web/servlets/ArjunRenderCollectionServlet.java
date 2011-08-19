/*
 * Copyright 2011 Google Inc.
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

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.cnxml.LinkResolver;
import org.cnx.cnxml.Module;
import org.cnx.cnxml.ModuleHTMLGenerator;
import org.cnx.common.collxml.Collection;
import org.cnx.common.collxml.CollectionHTMLGenerator;
import org.cnx.mdml.Actor;
import org.cnx.mdml.Metadata;
import org.cnx.repository.atompub.CnxMediaTypes;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.util.RenderScope;
import org.cnx.web.Utils;
import org.cnx.web.WebViewTemplate;
import org.cnx.web.XmlFetcher;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 * Servlet to Handle CNX Resources.
 *
 * TODO(arjuns) : Rename this file.
 *
 * @author Arjun Satyapal
 */
// TODO(arjuns) : Fix this hardcoding.
@Path("/collection")
public class ArjunRenderCollectionServlet {
    static Logger logger = Logger.getLogger(ArjunRenderCollectionServlet.class.getName());

    private final String COLLECTION_ID_PATH_PARAM = "collectionId";

    // TODO(arjuns) : move these to common.
    private final String COLLECTION_VERSION_PATH_PARAM = "collectionVersion";

    /**
     * This will fetch the specific version. Possible value for version = [{version}, {latest}]
     *
     * URL Pattern = /collection/<collectionId>/<collectionVersion>
     */
    private final String COLLECTION_VERSION_URL_PATTERN = "/{" + COLLECTION_ID_PATH_PARAM + "}/{"
        + COLLECTION_VERSION_PATH_PARAM + "}";

    /**
     * URL Pattern wrt /collection
     * /<collectionId>/<collectionVersion>/module/<moduleId>/<moduleVersion>
     */
    private final String COLLECTION_MODULE_URL_PATTERN = COLLECTION_VERSION_URL_PATTERN + "/module"
        + ArjunRenderModuleServlet.MODULE_VERSION_URL_PATTERN;

    // TODO(arjuns) : Move this to a better place.
    private CnxAtomPubClient cnxClient;

    public ArjunRenderCollectionServlet() {
        URL url = null;
        try {
            url = new URL(CommonHack.REPO_ATOM_PUB_URL);
            cnxClient = new CnxAtomPubClient(url);
        } catch (Exception e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    // http://localhost:8080/content/collection/c30039/1
    @GET
    @Produces(CnxMediaTypes.TEXT_HTML_UTF8)
    @Path(COLLECTION_VERSION_URL_PATTERN)
    public Response getCollectionVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String collectionVersionString)
            throws Exception {
        // TODO(arjuns) : Handle exception.
        StringBuilder builder = new StringBuilder();

        if (!VersionWrapper.isValidVersion(collectionVersionString)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        final VersionWrapper collectionVersion = new VersionWrapper(collectionVersionString);

        // TODO(arjuns) : Add a URL for accessing resources with HTTP redirect.
        Entry collectionVersionEntry =
            cnxClient.getCollectionVersionEntry(collectionId, collectionVersion);
        String collXml =
            cnxClient.getConstants()
                .getCollXmlDocFromAtomPubCollectionEntry(collectionVersionEntry);

        Injector injector = Guice.createInjector(new ArjunGuiceConfig());

        XmlFetcher fetcher = injector.getInstance(XmlFetcher.class);
        Collection collection = fetcher.getCollection(collectionId, collXml);
        // Get metadata
        String title = "";
        List<Actor> authors = null;
        final Metadata metadata = collection.getMetadata();
        if (metadata != null) {
            try {
                title = metadata.getTitle();
                authors = metadata.getAuthors();
            } catch (Exception e) {
                // TODO(arjuns) : handle exception.
                return Response.serverError().build();
            }
        }

        RenderScope renderScope = injector.getInstance(RenderScope.class);
        // Render content
        String contentHtml;
        // List<Actor> authors;
        renderScope.enter();
        try {
            renderScope.seed(Collection.class, collection);
            CollectionHTMLGenerator generator = injector.getInstance(CollectionHTMLGenerator.class);
            contentHtml = generator.generate(collection);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            renderScope.exit();
        }

        SoyTofu tofu = injector.getInstance(Key.get(SoyTofu.class, WebViewTemplate.class));
        final SoyMapData params =
            new SoyMapData("collection", new SoyMapData("id", collectionId, "version",
                collectionVersion.toString(), "title", title, "authors", Utils
                    .convertActorListToSoyData(authors), "contentHtml", contentHtml));

        String generatedCollectionHtml =
            tofu.render(CommonHack.COLLECTION_TEMPLATE_NAME, params, null);
        builder.append(generatedCollectionHtml);

        ResponseBuilder myresponse = Response.ok();
        myresponse.entity(builder.toString());

        return myresponse.build();
    }

    // TODO(arjuns) : Add returns.
    @GET
    @Produces(CnxMediaTypes.TEXT_HTML_UTF8)
    @Path(COLLECTION_MODULE_URL_PATTERN)
    public
            Response
            getModuleVersionUnderCollectionVersion(
                    @Context HttpServletRequest req,
                    @Context HttpServletResponse res,
                    @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
                    @PathParam(COLLECTION_VERSION_PATH_PARAM) String collectionVersionString,
                    @PathParam(ArjunRenderModuleServlet.MODULE_ID_PATH_PARAM) String moduleId,
                    @PathParam(ArjunRenderModuleServlet.MODULE_VERSION_PATH_PARAM) String moduleVersionString)
                    throws Exception {
        // TODO(arjuns) : handle exceptions.
        StringBuilder builder = new StringBuilder();

        if (!VersionWrapper.isValidVersion(collectionVersionString)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        final VersionWrapper collectionVersion = new VersionWrapper(collectionVersionString);

        if (!VersionWrapper.isValidVersion(moduleVersionString)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        final VersionWrapper moduleVersion = new VersionWrapper(moduleVersionString);

        // TODO(arjuns) : Add a URL for accessing resources with HTTP redirect.
        Entry collectionVersionEntry =
            cnxClient.getCollectionVersionEntry(collectionId, collectionVersion);
        String collXml =
            cnxClient.getConstants()
                .getCollXmlDocFromAtomPubCollectionEntry(collectionVersionEntry);

        Injector injector = Guice.createInjector(new ArjunGuiceConfig());
        XmlFetcher fetcher = injector.getInstance(XmlFetcher.class);
        Collection collection = fetcher.getCollection(collectionId, collXml);

        // Ensure module is part of the collection
        final Collection.ModuleLink currentModuleLink = collection.getModuleLink(moduleId);
        if (currentModuleLink == null) {
            logger.log(Level.INFO, "Collection " + collectionId + " does not contain module "
                + moduleId);
            // TODO(arjuns): Fix this.
            return Response.serverError().build();
        }

        ClientEntry moduleVersionEntry =
            cnxClient.getModuleVersionEntry(moduleId, moduleVersion);
        String cnxml = cnxClient.getCnxml(moduleVersionEntry);
        String resourceMappingXml = cnxClient.getResourceMappingXml(moduleVersionEntry);
        Module module = fetcher.getModule(moduleId, cnxml, resourceMappingXml);

        final Collection.ModuleLink[] links = collection.getPreviousNext(moduleId);
        URI previousModuleUri = null, nextModuleUri = null;
        String collectionTitle = null, moduleTitle;
        String moduleContentHtml = null;
        List<Actor> moduleAuthors;
        final String moduleContentHtmlCacheKey =
            "moduleContentHtml " + collectionId + " " + moduleId;

        RenderScope renderScope = injector.getInstance(RenderScope.class);

        renderScope.enter();
        try {
            renderScope.seed(Collection.class, collection);
            renderScope.seed(Module.class, module);

            if (moduleContentHtml == null) {
                logger.info("moduleContentHtml missed cache");
                ModuleHTMLGenerator generator = injector.getInstance(ModuleHTMLGenerator.class);
                moduleContentHtml = generator.generate(module);
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

            // Get module authors
            if (module.getMetadata() != null) {
                moduleAuthors = module.getMetadata().getAuthors();
            } else {
                moduleAuthors = Collections.<Actor> emptyList();
            }

            // Get collection previous/next links
            @SuppressWarnings("unchecked")
            Provider<LinkResolver> linkResolverProvider = injector.getProvider(LinkResolver.class);

            final LinkResolver linkResolver = linkResolverProvider.get();
            if (links[0] != null) {
                previousModuleUri = getModuleLinkUri(linkResolver, links[0]);
            }
            if (links[1] != null) {
                nextModuleUri = getModuleLinkUri(linkResolver, links[1]);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while rendering", e);
            // TODO(arjuns): handle exception.
            return Response.serverError().build();
        } finally {
            renderScope.exit();
        }

        SoyTofu tofu = injector.getInstance(Key.get(SoyTofu.class, WebViewTemplate.class));
        final SoyMapData params =
            new SoyMapData("collection", new SoyMapData("id", collectionId, "version",
                collectionVersion.toString(), "title", collectionTitle), "module", new SoyMapData("id",
                moduleId, "version", moduleVersion.toString(), "title", moduleTitle, "authors", Utils
                    .convertActorListToSoyData(moduleAuthors), "contentHtml", moduleContentHtml),
                "previousModule", convertModuleLinkToSoyData(links[0], previousModuleUri),
                "nextModule", convertModuleLinkToSoyData(links[1], nextModuleUri));

        String renderedModuleHtml = tofu.render(CommonHack.COLLECTION_MODULE_TEMPLATE_NAME, params, null);
        builder.append(renderedModuleHtml);
        ResponseBuilder myresponse = Response.ok();
        myresponse.entity(builder.toString());

        return myresponse.build();
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
            logger.log(Level.WARNING, "Could not obtain title for module link", e);
        }
        return new SoyMapData("id", link.getModuleId(), "version", link.getModuleVersion(),
            "title", title, "uri", (uri != null ? uri.toString() : null));
    }

    private static final URI
            getModuleLinkUri(LinkResolver linkResolver, Collection.ModuleLink link)
                    throws Exception {
        return linkResolver.resolveDocument(link.getModuleId(), link.getModuleVersion());
    }
}