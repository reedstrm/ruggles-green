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
package org.cnx.repository.atompub.jerseyservlets;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import org.cnx.common.repository.atompub.CnxAtomPubCollectionEnum;
import org.cnx.common.repository.atompub.CnxAtomPubConstants;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.CnxMediaTypes;
import org.cnx.common.repository.atompub.ServletUris;
import org.cnx.repository.atompub.utils.ServerUtil;
import org.cnx.servicedocument.AtomTextConstruct;
import org.cnx.servicedocument.Category;
import org.cnx.servicedocument.Collection;
import org.cnx.servicedocument.Collection.Categories;
import org.cnx.servicedocument.ObjectFactory;
import org.cnx.servicedocument.Service;
import org.cnx.servicedocument.Workspace;

/**
 * REST Resource for fetching ServiceDocument.
 * 
 * @author Arjun Satyapal
 */
@Path(ServletUris.ServiceDocument.SERVICE_DOCUMENT_SERVLET2)
public class CnxServiceDocumentServlet2 {
    @GET
    @Produces(CnxMediaTypes.TEXT_XML)
    @Path(ServletUris.ServiceDocument.SERVICE_DOCUMENT_PATH)
    public Response getNewServiceDocument(@Context HttpServletRequest req) throws JAXBException,
            XMLStreamException, FactoryConfigurationError {
        URL atomPubUrl = null;
        try {
            atomPubUrl =
                    new URL(ServerUtil.computeHostUrl(req) + "/"
                            + CnxAtomPubUtils.ATOMPUB_URL_PREFIX);
        } catch (MalformedURLException e) {
            // TODO(arjuns) : Handle exception properly.
            throw new RuntimeException(e);
        }

        CnxAtomPubConstants constants = new CnxAtomPubConstants(atomPubUrl);

        // TODO(arjuns) : Handle exception.
        // Creating ObjectFactor to create objects for Service Document.
        ObjectFactory objectFactory = new ObjectFactory();

        // Creating Service Document Object.
        Service service = objectFactory.createService();

        // Creating Workspace and adding it to Service Document.
        Workspace workspace = objectFactory.createWorkspace();

        // TODO(arjuns) : move this to jaxb utils.
        workspace
                .setTitle(getTitle(objectFactory, ServletUris.ServiceDocument.CNX_WORKSPACE_TITLE));
        service.getWorkspace().add(workspace);

        List<Collection> listOfCollections = workspace.getCollection();
        listOfCollections.add(getAPCForResources(objectFactory, constants));
        listOfCollections.add(getAPCForModules(objectFactory, constants));
        listOfCollections.add(getAPCForCollections(objectFactory, constants));

        String string = CnxAtomPubUtils.jaxbObjectToString(Service.class, service);
        return Response.ok().entity(string).build();
    }

    private Collection
            getAPCForResources(ObjectFactory objectFactory, CnxAtomPubConstants constants) {
        return getAtomPubCollection(objectFactory, constants.getAPCResourcesAbsPath(),
                constants.getAPCResourceScheme(),
                CnxAtomPubCollectionEnum.APC_RESOURCES.getTitle(),
                ServletUris.Resource.RESOURCE_SERVLET);
    }

    private Collection getAPCForModules(ObjectFactory objectFactory, CnxAtomPubConstants constants) {
        return getAtomPubCollection(objectFactory, constants.getAPCModulesAbsPath(),
                constants.getAPCModuleScheme(), CnxAtomPubCollectionEnum.APC_MODULE.getTitle(),
                ServletUris.Module.MODULE_SERVLET);
    }

    private Collection getAPCForCollections(ObjectFactory objectFactory,
            CnxAtomPubConstants constants) {
        return getAtomPubCollection(objectFactory, constants.getAPCModulesAbsPath(),
                constants.getAPCCollectionScheme(),
                CnxAtomPubCollectionEnum.APC_COLLECTION.getTitle(),
                ServletUris.Collection.COLLECTION_SERVLET);
    }

    private Collection getAtomPubCollection(ObjectFactory objectFactory, URL atomPubCollectionPath,
            URL scheme, String collectionTitle, String categoryTerm) {
        Collection cnxResourceCollection = objectFactory.createCollection();
        cnxResourceCollection.setHref(atomPubCollectionPath.toString());
        cnxResourceCollection.setTitle(getTitle(objectFactory, collectionTitle));

        Category category = objectFactory.createCategory();
        category.setTerm(categoryTerm);
        category.setScheme(scheme.toString());

        Categories categories = objectFactory.createCollectionCategories();
        categories.getCategory().add(category);
        categories.setFixed(CnxAtomPubUtils.YES);
        cnxResourceCollection.setCategories(categories);

        return cnxResourceCollection;
    }

    private AtomTextConstruct getTitle(ObjectFactory objectFactory, String title) {
        AtomTextConstruct atomTitle = objectFactory.createAtomTextConstruct();
        atomTitle.getContent().add(title);

        return atomTitle;
    }
}
