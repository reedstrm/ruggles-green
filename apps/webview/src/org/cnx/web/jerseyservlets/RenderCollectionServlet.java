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
import static org.cnx.web.CommonHack.fetchFromRepositoryAndReturn;
import static org.cnx.web.CommonHack.handleCnxInvalidUrlException;

import org.cnx.common.repository.ContentType;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.parsers.SAXParser;
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
import org.cnx.common.exceptions.CnxInvalidUrlException;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.ServletUris;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.mdml.Actor;
import org.cnx.mdml.Metadata;
import org.cnx.util.RenderScope;
import org.cnx.web.CommonHack;
import org.cnx.web.Utils;
import org.cnx.web.WebViewConfiguration;
import org.cnx.web.WebViewTemplate;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 * Servlet to Handle CNX Resources.
 * 
 * @author Arjun Satyapal
 */
@Path(ServletUris.Collection.COLLECTION_SERVLET)
public class RenderCollectionServlet {
    private final Logger logger = Logger.getLogger(RenderCollectionServlet.class.getName());
    /**
     * URI relative to :
     * {@link org.cnx.repository.atompub.ServletUris.Collection#COLLECTION_VERSION_PATH}
     * 
     * In order to fetch Module under context of a collection.
     * 
     * URL Pattern : {jersey-home}/collection/<collectionId>/<collectionVersion>/module/<moduleId>/<
     * moduleVersion>
     */
    static final String COLLECTION_VERSION_MODULE_VERSION_URI =
            ServletUris.Collection.COLLECTION_VERSION_PATH + ServletUris.Module.MODULE_SERVLET
                    + ServletUris.Module.MODULE_VERSION_PATH;

    /**
     * URI relative to : {@link #COLLECTION_VERSION_MODULE_VERSION_URI}.
     * 
     * Helper URI to fetch CNXML for a module under Collection Context.
     */
    static final String COLLECTION_VERSION_MODULE_VERSION_XML_URI =
            COLLECTION_VERSION_MODULE_VERSION_URI + "/" + ServletUris.XML_DOC;

    /**
     * URI relative to : {@link #COLLECTION_VERSION_MODULE_VERSION_URI}.
     * 
     * Helper URI to fetch ResourceMappingXml under Collection context.
     */
    static final String COLLECTION_VERSION_MODULE_VERSION_RESOURCE_MAPPING_URL =
            COLLECTION_VERSION_MODULE_VERSION_URI + "/" + ServletUris.RESOURCE_MAPPING_DOC;

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

    /**
     * When client does HTTP-GET on
     * {@link org.cnx.repository.atompub.ServletUris.Collection#COLLECTION_VERSION_PATH}, then this
     * method is triggered.
     * 
     * Purpose of this method is to fetch CollXml from Repository, then create a HTML and then serve
     * it back to client.
     * 
     * @param collectionId Id of desired Collection.
     * @param collectionVersionString Version of desiredModule.
     * @return Response which contains HTML representation of the desired module.
     * @throws ProponoException
     * @throws SAXException
     * @throws IOException
     */
    @GET
    @Produces(ContentType.TEXT_HTML_UTF8)
    @Path(ServletUris.Collection.COLLECTION_VERSION_PATH)
    public Response getCollectionVersion(
            @PathParam(ServletUris.COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(ServletUris.COLLECTION_VERSION_PATH_PARAM) String collectionVersionString)
            throws ProponoException, SAXException, IOException {
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
                CnxAtomPubUtils.getCollXmlDocFromAtomPubCollectionEntry(collectionVersionEntry);

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

    /**
     * When client does HTTP-GET on
     * {@link org.cnx.repository.atompub.ServletUris.Collection#COLLECTION_VERSION_COLLXML}, then
     * this method is triggered.
     * 
     * This is a helper method which is used for providing CollXml for desired Collection.
     * 
     * @param collectionId Id of desired Collection.
     * @param collectionVersionString Desired version of Collection.
     * @return CollXml.
     * @throws IOException
     */
    @GET
    @Produces(ContentType.TEXT_XML_UTF8)
    @Path(ServletUris.Collection.COLLECTION_VERSION_COLLXML)
    public Response getCollectionVersionXml(
            @PathParam(ServletUris.COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(ServletUris.COLLECTION_VERSION_PATH_PARAM) String collectionVersionString)
            throws IOException {
        // TODO(arjuns) : Handle exception.
        final IdWrapper idWrapper = new IdWrapper(collectionId, IdWrapper.Type.COLLECTION);
        final VersionWrapper versionWrapper = new VersionWrapper(collectionVersionString);

        URL url =
                cnxClient.getConstants().getCollectionVersionXmlAbsPath(idWrapper, versionWrapper);

        return fetchFromRepositoryAndReturn(url);
    }

    /**
     * When client does HTTP-GET on {@link #COLLECTION_VERSION_MODULE_VERSION_URI}, then this method
     * is triggered.
     * 
     * Purpose of this method is to render a Module in Collection's context.
     * 
     * @param collectionId Id of desired Collection.
     * @param collectionVersionString Version of desiredModule.
     * @param moduleId Id of desired Module.
     * @param moduleVersionString Version of desired Module.
     * @return Response which contains HTML representation of the desired module.
     * @throws ProponoException
     * @throws SAXException
     * @throws IOException
     * @throws JDOMException
     */
    @GET
    @Produces(ContentType.TEXT_HTML_UTF8)
    @Path(COLLECTION_VERSION_MODULE_VERSION_URI)
    public Response getModuleVersionUnderCollectionVersion(
            @PathParam(ServletUris.COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(ServletUris.COLLECTION_VERSION_PATH_PARAM) String collectionVersionString,
            @PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(ServletUris.MODULE_VERSION_PATH_PARAM) String moduleVersionString)
            throws ProponoException, SAXException, IOException, JDOMException {
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
                CnxAtomPubUtils.getCollXmlDocFromAtomPubCollectionEntry(collectionVersionEntry);

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
        return CommonHack.CONTENT_NAME_SPACE + ServletUris.Collection.COLLECTION_SERVLET + "/"
                + collectionId + "/" + collectionVersion.toString();
    }

    /**
     * When client does HTTP-GET on {@link #COLLECTION_VERSION_MODULE_VERSION_XML_URI}, then this
     * method is triggered.
     * 
     * This is a helper method which is used for providing XML for a Module. This method in turn
     * delegates the responsibility to {@link RenderModuleServlet#getModuleVersion};
     * 
     * @param context ServletContext.
     * @param moduleId Id of desired Module.
     * @param moduleVersionString Version of desired Module.
     * @return Response containing CNXML for desired Module.
     * @throws IOException
     */
    @GET
    @Produces(ContentType.TEXT_XML_UTF8)
    @Path(COLLECTION_VERSION_MODULE_VERSION_XML_URI)
    public Response getModuleVersionXmlUnderCollectionVersion(@Context ServletContext context,
            @PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(ServletUris.MODULE_VERSION_PATH_PARAM) String moduleVersionString)
            throws IOException {
        RenderModuleServlet moduleServlet = new RenderModuleServlet(context);

        return moduleServlet.getModuleVersionResourcesXml(moduleId, moduleVersionString);
    }

    /**
     * When client does HTTP-GET on {@link #COLLECTION_VERSION_MODULE_VERSION_RESOURCE_MAPPING_URL},
     * then this method is triggered.
     * 
     * This is a helper method which is used for providing ResrouceMappingXml for a Module. This
     * method in turn delegates the responsibility to
     * {@link RenderModuleServlet#getModuleVersionResourcesXml};
     * 
     * @param context ServletContext.
     * @param moduleId Id of desired Module.
     * @param moduleVersionString Version of desired Module.
     * @return Response containing CNXML for desired Module.
     * @throws IOException
     */
    @GET
    @Produces(ContentType.TEXT_XML_UTF8)
    @Path(COLLECTION_VERSION_MODULE_VERSION_RESOURCE_MAPPING_URL)
    public Response getModuleVersionResourcesXmlUnderCollectionVersion(
            @Context ServletContext context,
            @PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(ServletUris.MODULE_VERSION_PATH_PARAM) String moduleVersionString)
            throws IOException {
        RenderModuleServlet moduleServlet = new RenderModuleServlet(context);

        return moduleServlet.getModuleVersionResourcesXml(moduleId, moduleVersionString);
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
