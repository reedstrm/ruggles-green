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

import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

import org.cnx.repository.atompub.client.CnxAtomPubClient;
import org.cnx.repository.atompub.utils.CnxAtomPubConstants;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Test for {@link CnxAtomResourceServlet}
 *
 * @author Arjun Satyapal
 */
public class ResourceServletTest extends CnxAtomPubBaseTest {
    private CnxAtomPubClient cnxClient;

    // TODO(arjuns) : Create file dynamically.
    private final File file = new File("/home/arjuns/test_file.txt");
    public ResourceServletTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws MalformedURLException, ProponoException {
        cnxClient = new CnxAtomPubClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testResource() throws Exception {
        ClientEntry createResourceEntry = cnxClient.createUploadUrl();
        assertNotNull(createResourceEntry);
        assertNotNull(createResourceEntry.getId());
        String resourceId = createResourceEntry.getId();
        String expectedResourceUrl = getConstants().getResourceAbsPath(resourceId).toString();
        assertEquals(expectedResourceUrl, createResourceEntry.getEditURI());

        /*
         * There should be two links in following order :
         * 1. Link for Blobstore.
         * 2. Link for Resource.
         */
        @SuppressWarnings("unchecked")
        List<Link> listOfLinks = createResourceEntry.getOtherLinks();
        assertEquals(2, listOfLinks.size());

        Link blobStoreLink = listOfLinks.get(0);
        assertEquals(CnxAtomPubConstants.REL_TAG_FOR_BLOBSTORE_URL, blobStoreLink.getRel());
        assertNotNull(blobStoreLink.getHref());

        Link resourceLink = listOfLinks.get(1);
        assertEquals(CnxAtomPubConstants.LINK_RELATION_EDIT_TAG, resourceLink.getRel());
        assertEquals(expectedResourceUrl, resourceLink.getHref());

        // Now upload blob to AppEngine.
        ClientEntry uploadResourceEntry = cnxClient.uploadFileToBlobStore(file);

        // TODO(arjuns) : Add test for get once it works.
        System.out.println(createResourceEntry);
    }
}
