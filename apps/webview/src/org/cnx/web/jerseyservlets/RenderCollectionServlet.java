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
package org.cnx.web.jerseyservlets;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.cnx.repository.atompub.CnxAtomPubConstants.END_URL_XML;
import static org.cnx.web.CommonHack.COLLECTION_ID_PATH_PARAM;
import static org.cnx.web.CommonHack.COLLECTION_VERSION_PATH_PARAM;
import static org.cnx.web.CommonHack.MODULE;
import static org.cnx.web.CommonHack.MODULE_ID_PATH_PARAM;
import static org.cnx.web.CommonHack.MODULE_VERSION_PATH_PARAM;
import static org.cnx.web.CommonHack.fetchFromRepositoryAndReturn;
import static org.cnx.web.CommonHack.handleCnxInvalidUrlException;
import static org.cnx.web.jerseyservlets.RenderModuleServlet.MODULE_VERSION_RESOURCES_URL_PATTERN;
import static org.cnx.web.jerseyservlets.RenderModuleServlet.MODULE_VERSION_URL_PATTERN;
import static org.cnx.web.jerseyservlets.RenderModuleServlet.MODULE_VERSION_XML_URL_PATTERN;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.propono.atom.client.ClientEntry;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.cnxml.LinkResolver;
import org.cnx.cnxml.Module;
import org.cnx.cnxml.ModuleFactory;
import org.cnx.cnxml.ModuleHtmlGenerator;
import org.cnx.common.collxml.Collection;
import org.cnx.common.collxml.CollectionFactory;
import org.cnx.common.collxml.CollectionHtmlGenerator;
import org.cnx.common.collxml.CollectionItem;
import org.cnx.common.collxml.ModuleLink;
import org.cnx.common.collxml.Subcollection;
import org.cnx.exceptions.CnxInvalidUrlException;
import org.cnx.mdml.Actor;
import org.cnx.mdml.Metadata;
import org.cnx.repository.atompub.CnxMediaTypes;
import org.cnx.repository.atompub.IdWrapper;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.util.RenderScope;
import org.cnx.web.CommonHack;
import org.cnx.web.Utils;
import org.cnx.web.WebViewConfiguration;
import org.cnx.web.WebViewTemplate;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.parsers.SAXParser;

/**
 * Servlet to Handle CNX Resources.
 * 
 * TODO(arjuns) : Create separate package for jersey servlets vs httpservlets.
 * 
 * @author Arjun Satyapal
 */
@Path(CommonHack.COLLECTION)
public class RenderCollectionServlet {
    private static final Logger logger = Logger.getLogger(RenderCollectionServlet.class.getName());

    /**
     * This will fetch the specific version. Possible value for version = [{version}, {latest}]
     * 
     * URL Pattern = /collection/<collectionId>/<collectionVersion>
     */
    private final String COLLECTION_VERSION_URL_PATTERN = "/{" + COLLECTION_ID_PATH_PARAM + "}/{"
            + COLLECTION_VERSION_PATH_PARAM + "}";

    /**
     * This will fetch the CollXml for specific collection-version.
     * 
     * URL Pattern = /collection/<collectionId>/<collectionVersion>/xml
     */
    private final String COLLECTION_VERSION_XML_URL_PATTERN = "/{" + COLLECTION_ID_PATH_PARAM
            + "}/{" + COLLECTION_VERSION_PATH_PARAM + "}" + END_URL_XML;

    /**
     * URL Pattern wrt /collection
     * /<collectionId>/<collectionVersion>/module/<moduleId>/<moduleVersion>
     */
    private final String COLLECTION_MODULE_URL_PATTERN = COLLECTION_VERSION_URL_PATTERN + MODULE
            + MODULE_VERSION_URL_PATTERN;

    /**
     * URL Pattern wrt /collection
     * /<collectionId>/<collectionVersion>/module/<moduleId>/<moduleVersion>/xml
     */
    private final String COLLECTION_MODULE_XML_URL_PATTERN = COLLECTION_VERSION_URL_PATTERN
            + MODULE + MODULE_VERSION_XML_URL_PATTERN;

    /**
     * URL Pattern wrt /collection
     * /<collectionId>/<collectionVersion>/module/<moduleId>/<moduleVersion>/resources
     */
    private final String COLLECTION_MODULE_RESOURCES_URL_PATTERN = COLLECTION_VERSION_URL_PATTERN
            + MODULE + MODULE_VERSION_RESOURCES_URL_PATTERN;

    // Soy parameters
    private static final String AUTHORS_PARAM = "authors";
    private static final String COLLECTION_ITEM_DEPTH_PARAM = "depth";
    private static final String COLLECTION_ITEM_INDEX_PARAM = "index";
    private static final String COLLECTION_ITEM_TYPE_PARAM = "type";
    private static final String COLLECTION_PARAM = "collection";
    private static final String CONTENT_HTML_PARAM = "contentHtml";
    private static final String ID_PARAM = "id";
    private static final String MODULE_LINK_TYPE = "module";
    private static final String MODULE_PARAM = "module";
    private static final String NEXT_MODULE_PARAM = "nextModule";
    private static final String PREVIOUS_MODULE_PARAM = "previousModule";
    private static final String SUBCOLLECTION_TYPE = "subcollection";
    private static final String TITLE_PARAM = "title";
    private static final String URI_PARAM = "uri";
    private static final String VERSION_PARAM = "version";

    private final Injector injector;
    private final WebViewConfiguration configuration;
    private final Provider<LinkResolver> linkResolverProvider;
    private final SAXParser saxParser;
    // TODO(arjuns) : Move this to a better place.
    private final CnxAtomPubClient cnxClient;

    public RenderCollectionServlet(@Context ServletContext context) {
        URL url = null;
        try {
            injector = (Injector) context.getAttribute(Injector.class.getName());
            linkResolverProvider = injector.getProvider(LinkResolver.class);
            configuration = injector.getInstance(WebViewConfiguration.class);
            saxParser = injector.getInstance(SAXParser.class);
            url = new URL(configuration.getRepositoryAtomPubUrl());
            cnxClient = new CnxAtomPubClient(url);
        } catch (Exception e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    @GET
    @Produces(CnxMediaTypes.TEXT_HTML_UTF8)
    @Path(COLLECTION_VERSION_URL_PATTERN)
    public Response getCollectionVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String collectionVersionString)
            throws Exception {
        // TODO(arjuns) : Handle exception.
        final IdWrapper idWrapper = new IdWrapper(collectionId, IdWrapper.Type.COLLECTION);
        final VersionWrapper versionWrapper = new VersionWrapper(collectionVersionString);

        StringBuilder builder = new StringBuilder();

        // TODO(arjuns) : Add a URL for accessing resources with HTTP redirect.
        Entry collectionVersionEntry = null;

        try {
            collectionVersionEntry = cnxClient.getCollectionVersionEntry(idWrapper, versionWrapper);
        } catch (CnxInvalidUrlException e) {
            handleCnxInvalidUrlException(idWrapper, e);
        }

        String collXml =
                cnxClient.getConstants().getCollXmlDocFromAtomPubCollectionEntry(
                        collectionVersionEntry);

        final Collection collection =
                injector.getInstance(CollectionFactory.class).create(collectionId,
                        collectionVersionString, CommonHack.parseXmlString(saxParser, collXml));
        // Get metadata
        String title = "", abstractText = null;
        List<Actor> authors = null;
        final Metadata metadata = collection.getMetadata();
        if (metadata != null) {
            try {
                title = metadata.getTitle();
                abstractText = metadata.getAbstract();
                authors = metadata.getAuthors();
            } catch (Exception e) {
                // TODO(arjuns) : handle exception.
                return Response.serverError().build();
            }
        }

        RenderScope renderScope = injector.getInstance(RenderScope.class);
        // Render content
        String contentHtml;
        renderScope.enter();
        try {
            renderScope.seed(Collection.class, collection);
            CollectionHtmlGenerator generator = injector.getInstance(CollectionHtmlGenerator.class);
            contentHtml = generator.generate(collection);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            renderScope.exit();
        }

        // Get start link
        URI firstModuleUri = null;
        renderScope.enter();
        try {
            renderScope.seed(Collection.class, collection);
            if (!collection.getModuleLinks().isEmpty()) {
                final ModuleLink link = collection.getModuleLinks().get(0);
                final LinkResolver linkResolver = injector.getInstance(LinkResolver.class);
                firstModuleUri =
                        linkResolver.resolveDocument(link.getModuleId(), link.getModuleVersion());
            }
        } catch (Exception e) {
            // TODO(light): handle exception.
            return Response.serverError().build();
        } finally {
            renderScope.exit();
        }

        SoyTofu tofu = injector.getInstance(Key.get(SoyTofu.class, WebViewTemplate.class));

        SoyMapData collectionSoyMapData =
                new SoyMapData("id", collectionId, "version", versionWrapper.toString(), "title",
                        title, "abstract", abstractText, "authors",
                        Utils.convertActorListToSoyData(authors), "contentHtml", contentHtml);
        String uriString = (firstModuleUri != null ? firstModuleUri.toString() : null);
        final SoyMapData params =
                new SoyMapData("collection", collectionSoyMapData, "firstModuleUri", uriString);

        String generatedCollectionHtml =
                tofu.render(CommonHack.COLLECTION_TEMPLATE_NAME, params, null);
        builder.append(generatedCollectionHtml);

        ResponseBuilder myresponse = Response.ok();
        myresponse.entity(builder.toString());

        return myresponse.build();
    }

    @GET
    @Produces(CnxMediaTypes.TEXT_XML_UTF8)
    @Path(COLLECTION_VERSION_XML_URL_PATTERN)
    public Response getCollectionVersionXml(
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String collectionVersionString)
            throws Exception {
        // TODO(arjuns) : Handle exception.
        final IdWrapper idWrapper = new IdWrapper(collectionId, IdWrapper.Type.COLLECTION);
        final VersionWrapper versionWrapper = new VersionWrapper(collectionVersionString);

        URL url =
                cnxClient.getConstants().getCollectionVersionXmlAbsPath(idWrapper, versionWrapper);

        return fetchFromRepositoryAndReturn(url);
    }

    // TODO(arjuns) : Add returns.
    @GET
    @Produces(CnxMediaTypes.TEXT_HTML_UTF8)
    @Path(COLLECTION_MODULE_URL_PATTERN)
    public Response getModuleVersionUnderCollectionVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String collectionVersionString,
            @PathParam(CommonHack.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(CommonHack.MODULE_VERSION_PATH_PARAM) String moduleVersionString)
            throws Exception {
        // TODO(arjuns) : handle exceptions.
        final IdWrapper collectionIdWrapper =
                new IdWrapper(collectionId, IdWrapper.Type.COLLECTION);
        final VersionWrapper collectionVersion = new VersionWrapper(collectionVersionString);

        final IdWrapper moduleIdWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
        final VersionWrapper moduleVersion = new VersionWrapper(moduleVersionString);

        // TODO(arjuns) : Add a URL for accessing resources with HTTP redirect.
        Entry collectionVersionEntry = null;
        try {
            collectionVersionEntry =
                    cnxClient.getCollectionVersionEntry(collectionIdWrapper, collectionVersion);
        } catch (CnxInvalidUrlException e) {
            handleCnxInvalidUrlException(collectionIdWrapper, e);
        }

        String collXml =
                cnxClient.getConstants().getCollXmlDocFromAtomPubCollectionEntry(
                        collectionVersionEntry);

        final Collection collection =
                injector.getInstance(CollectionFactory.class).create(collectionId,
                        collectionVersionString, CommonHack.parseXmlString(saxParser, collXml));

        // Ensure module is part of the collection
        final ModuleLink currentModuleLink = collection.getModuleLink(moduleId);
        if (currentModuleLink == null) {
            logger.log(Level.INFO, "Collection " + collectionId + " does not contain module "
                    + moduleId);
            // TODO(arjuns): Fix this.
            return Response.serverError().build();
        }

        ClientEntry moduleVersionEntry = null;
        try {
            moduleVersionEntry = cnxClient.getModuleVersionEntry(moduleIdWrapper, moduleVersion);
        } catch (CnxInvalidUrlException e) {
            handleCnxInvalidUrlException(moduleIdWrapper, e);
        }

        String cnxml = cnxClient.getCnxml(moduleVersionEntry);
        String resourceMappingXml = cnxClient.getResourceMappingXml(moduleVersionEntry);

        final Module module =
                injector.getInstance(ModuleFactory.class).create(moduleId, moduleVersionString,
                        CommonHack.parseXmlString(saxParser, cnxml),
                        CommonHack.getResourcesFromResourceMappingDoc(resourceMappingXml));

        final ModuleLink[] links = collection.getPreviousNext(moduleId);
        SoyData prevLink, nextLink;
        String collectionTitle = null, moduleTitle;
        String moduleContentHtml = null;
        List<Actor> moduleAuthors;

        RenderScope renderScope = injector.getInstance(RenderScope.class);

        renderScope.enter();
        try {
            renderScope.seed(Collection.class, collection);
            renderScope.seed(Module.class, module);

            final ModuleHtmlGenerator generator = injector.getInstance(ModuleHtmlGenerator.class);
            moduleContentHtml = generator.generate(module);

            // Get collection title
            if (collection.getMetadata() != null) {
                collectionTitle = collection.getMetadata().getTitle();
            }

            // Get next/previous links
            prevLink = convertCollectionItemToSoyData(links[0]);
            nextLink = convertCollectionItemToSoyData(links[1]);

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
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while rendering", e);
            // TODO(arjuns): handle exception.
            return Response.serverError().build();
        } finally {
            renderScope.exit();
        }

        final SoyTofu tofu = injector.getInstance(Key.get(SoyTofu.class, WebViewTemplate.class));
        final SoyMapData collectionSoyMapData =
                new SoyMapData(ID_PARAM, collectionId, VERSION_PARAM, collectionVersion.toString(),
                        URI_PARAM, getCollectionUri(collectionId, collectionVersion), TITLE_PARAM,
                        collectionTitle);

        final SoyMapData moduleSoyMapData =
                new SoyMapData(ID_PARAM, moduleId, VERSION_PARAM, moduleVersion.toString(),
                        TITLE_PARAM, moduleTitle, AUTHORS_PARAM,
                        Utils.convertActorListToSoyData(moduleAuthors), CONTENT_HTML_PARAM,
                        moduleContentHtml);

        final SoyMapData params =
                new SoyMapData(COLLECTION_PARAM, collectionSoyMapData, MODULE_PARAM,
                        moduleSoyMapData, PREVIOUS_MODULE_PARAM, prevLink, NEXT_MODULE_PARAM,
                        nextLink);

        final String renderedModuleHtml =
                tofu.render(CommonHack.COLLECTION_MODULE_TEMPLATE_NAME, params, null);
        return Response.ok().entity(renderedModuleHtml).build();
    }

    private String getCollectionUri(String collectionId, VersionWrapper collectionVersion) {
        return CommonHack.CONTENT_NAME_SPACE + CommonHack.COLLECTION + "/" + collectionId + "/"
                + collectionVersion.toString();
    }

    @GET
    @Produces(CnxMediaTypes.TEXT_XML_UTF8)
    @Path(COLLECTION_MODULE_XML_URL_PATTERN)
    public Response getModuleVersionXmlUnderCollectionVersion(
            @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String moduleVersionString) throws Exception {
        IdWrapper idWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
        VersionWrapper versionWrapper = new VersionWrapper(moduleVersionString);
        URL url = cnxClient.getConstants().getModuleVersionXmlAbsPath(idWrapper, versionWrapper);

        return fetchFromRepositoryAndReturn(url);
    }

    @GET
    @Produces(CnxMediaTypes.TEXT_XML_UTF8)
    @Path(COLLECTION_MODULE_RESOURCES_URL_PATTERN)
    public Response getModuleVersionResourcesXmlUnderCollectionVersion(
            @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String moduleVersionString) throws Exception {
        IdWrapper idWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
        VersionWrapper versionWrapper = new VersionWrapper(moduleVersionString);
        URL url =
                cnxClient.getConstants().getModuleVersionResourceMappingAbsPath(idWrapper,
                        versionWrapper);

        return fetchFromRepositoryAndReturn(url);
    }

    private SoyData convertCollectionItemToSoyData(@Nullable CollectionItem item) {
        if (item == null) {
            return SoyData.createFromExistingData(null);
        }
        String title = null;
        try {
            title = item.getMetadata().getTitle();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not obtain title for collection item", e);
        }

        final SoyMapData map =
                new SoyMapData(TITLE_PARAM, title, COLLECTION_ITEM_DEPTH_PARAM, item.getDepth(),
                        COLLECTION_ITEM_INDEX_PARAM, item.getIndex());

        if (item instanceof ModuleLink) {
            map.put(COLLECTION_ITEM_TYPE_PARAM, MODULE_LINK_TYPE);
            convertModuleLinkToSoyData(map, (ModuleLink) item);
        } else if (item instanceof Subcollection) {
            map.put(COLLECTION_ITEM_TYPE_PARAM, SUBCOLLECTION_TYPE);
        }

        return map;
    }

    private void convertModuleLinkToSoyData(SoyMapData data, ModuleLink link) {
        checkNotNull(data);
        checkNotNull(link);
        URI uri = null;
        try {
            uri = getModuleLinkUri(linkResolverProvider.get(), link);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not obtain URI for module link", e);
        }
        data.put(ID_PARAM, link.getModuleId());
        data.put(VERSION_PARAM, link.getModuleVersion());
        data.put(URI_PARAM, (uri != null ? uri.toString() : null));
    }

    private static final URI getModuleLinkUri(LinkResolver linkResolver, ModuleLink link)
            throws Exception {
        return linkResolver.resolveDocument(link.getModuleId(), link.getModuleVersion());
    }
}
