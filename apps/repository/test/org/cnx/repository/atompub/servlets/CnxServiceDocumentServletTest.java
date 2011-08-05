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
package org.cnx.repository.atompub.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.utils.CustomMediaTypes;
import org.junit.Before;
import org.junit.Test;

import com.sun.syndication.feed.atom.Category;
import com.sun.syndication.propono.atom.client.ClientWorkspace;
import com.sun.syndication.propono.atom.common.Categories;
import com.sun.syndication.propono.atom.common.Collection;
import com.sun.syndication.propono.utils.ProponoException;


/**
 * Test for {@link CnxServiceDocumentServlet}
 *
 * @author Arjun Satyapal
 */
public class CnxServiceDocumentServletTest extends CnxAtomPubBaseTest {
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
        ClientWorkspace workspace = cnxClient.getWorkSpace();
        assertEquals(3, workspace.getCollections().size());
        assertEquals(CnxAtomPubConstants.CNX_WORKSPACE_TITLE, workspace.getTitle());
        assertEquals(CustomMediaTypes.TEXT, workspace.getTitleType());

        // Now validate each collection individually.
        validateCollection(cnxClient.getCollectionResource(),
            CnxAtomPubConstants.COLLECTION_RESOURCE_REL_PATH, getConstants()
                .getCollectionResourceScheme());

        validateCollection(cnxClient.getCollectionModule(),
            CnxAtomPubConstants.COLLECTION_MODULE_REL_PATH, getConstants()
                .getCollectionModuleScheme());

        validateCollection(cnxClient.getCollectionCnxCollection(),
            CnxAtomPubConstants.COLLECTION_CNX_COLLECTION_REL_PATH, getConstants()
                .getCollectionCnxCollectionScheme());
    }

    private void
                    validateCollection(Collection collection, String collectionTermAndLabel,
                        URL scheme) {
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
        assertEquals(scheme.toString(), category.getScheme());
    }
}