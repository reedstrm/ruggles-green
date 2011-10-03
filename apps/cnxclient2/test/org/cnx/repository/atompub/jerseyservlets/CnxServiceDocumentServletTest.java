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

import static org.cnx.repository.atompub.jerseyservlets.TestingUtils.validateTitle;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.cnx.atompubclient.CnxClient;
import org.cnx.common.repository.atompub.CnxAtomPubCollectionEnum;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.ServletUris;
import org.cnx.servicedocument.Category;
import org.cnx.servicedocument.Collection;
import org.cnx.servicedocument.Collection.Categories;
import org.cnx.servicedocument.Service;
import org.cnx.servicedocument.Workspace;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CnxServiceDocumentServlet}
 * 
 * @author Arjun Satyapal
 */
public class CnxServiceDocumentServletTest extends CnxAtomPubBasetest {
    private CnxClient cnxClient;

    public CnxServiceDocumentServletTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws IOException, JAXBException, URISyntaxException {
        cnxClient = new CnxClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testServiceDocument() throws Exception {
        Service serviceDocumentObject = cnxClient.getServiceDocumentObject();
        List<Workspace> workspaces = serviceDocumentObject.getWorkspace();
        assertEquals(1, workspaces.size());

        Workspace workspace = workspaces.get(0);
        validateTitle(workspace.getTitle(), ServletUris.ServiceDocument.CNX_WORKSPACE_TITLE);

        List<org.cnx.servicedocument.Collection> collections = workspace.getCollection();
        assertEquals(3, collections.size());

        // Now validating collections.
        // Ordering of collections is not required but assuming order helps in writing tests.
        // Assuming first collection is AtomPub Collection for Resources.
        validateAPCForResources(collections.get(0));
        validateAPCForModules(collections.get(1));
        validateAPCForCollections(collections.get(2));
    }

    //
    private void validateAPCForResources(Collection collection) {
        validateTitle(collection.getTitle(), CnxAtomPubCollectionEnum.APC_RESOURCES.getTitle());

        validateCategories(collection.getCategories(), ServletUris.Resource.RESOURCE_SERVLET,
                cnxClient.getConstants().getAPCResourceScheme());
    }

    private void validateAPCForModules(Collection collection) {
        validateTitle(collection.getTitle(), CnxAtomPubCollectionEnum.APC_MODULE.getTitle());

        validateCategories(collection.getCategories(), ServletUris.Module.MODULE_SERVLET, cnxClient
                .getConstants().getAPCModuleScheme());
    }

    private void validateAPCForCollections(Collection collection) {
        validateTitle(collection.getTitle(), CnxAtomPubCollectionEnum.APC_COLLECTION.getTitle());

        validateCategories(collection.getCategories(), ServletUris.Collection.COLLECTION_SERVLET,
                cnxClient.getConstants().getAPCCollectionScheme());
    }

    private void validateCategories(Categories categories, String term, URL scheme) {
        List<org.cnx.servicedocument.Category> listOfCategory = categories.getCategory();
        assertEquals(1, listOfCategory.size());
        assertEquals(CnxAtomPubUtils.YES, categories.getFixed());

        Category category = listOfCategory.get(0);
        assertEquals(term, category.getTerm());
        assertEquals(scheme.toString(), category.getScheme());
    }
}
