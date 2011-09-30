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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.sun.syndication.feed.atom.Category;
import com.sun.syndication.propono.atom.client.ClientWorkspace;
import com.sun.syndication.propono.atom.common.Categories;
import com.sun.syndication.propono.atom.common.Collection;
import com.sun.syndication.propono.utils.ProponoException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.CnxMediaTypes;
import org.cnx.common.repository.atompub.ServletUris;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CnxServiceDocumentServlet}
 * 
 * @author Arjun Satyapal
 */
public class CnxServiceDocumentServletTest extends CnxAtomPubBasetest {
    private CnxAtomPubClient cnxClient;

    public CnxServiceDocumentServletTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws MalformedURLException, ProponoException {
        cnxClient = new CnxAtomPubClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testCnxServiceDocument() {
        ClientWorkspace workspace = cnxClient.getWorkspace();
        assertEquals(3, workspace.getCollections().size());
        assertEquals(CnxAtomPubUtils.CNX_WORKSPACE_TITLE, workspace.getTitle());
        assertEquals(CnxMediaTypes.TEXT_XML_UTF8, workspace.getTitleType());

        // Now validate each collection individually.
        validateCollection(cnxClient.getCollectionResource(),
                ServletUris.Resource.RESOURCE_SERVLET, getConstants().getAPCResourceScheme());

        validateCollection(cnxClient.getCollectionModule(), ServletUris.Module.MODULE_SERVLET,
                getConstants().getAPCModuleScheme());

        validateCollection(cnxClient.getCollectionCnxCollection(),
                ServletUris.Collection.COLLECTION_SERVLET, getConstants().getAPCCollectionScheme());
    }

    private void
            validateCollection(Collection collection, String collectionTermAndLabel, URL scheme) {
        assertNotNull(collection);

        @SuppressWarnings("unchecked")
        List<Categories> listOfCategories = collection.getCategories();
        assertEquals(1, listOfCategories.size());
        Categories categories = listOfCategories.get(0);

        @SuppressWarnings("unchecked")
        List<Category> listOfCategory = categories.getCategories();
        assertEquals(1, listOfCategory.size());

        Category category = listOfCategory.get(0);
        assertEquals(collectionTermAndLabel, category.getTerm());
        assertEquals(collectionTermAndLabel, category.getLabel());

        // TODO(arjuns) : Change scheme to use rel.
        assertEquals(scheme.toString(), category.getScheme().toString());
    }
}
