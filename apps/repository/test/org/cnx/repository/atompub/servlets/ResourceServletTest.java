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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.servlets.migrators.ResourceMigrator;
import org.junit.Before;
import org.junit.Test;

import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

/**
 * Test for {@link CnxAtomResourceServlet}
 *
 * @author Arjun Satyapal
 */
public class ResourceServletTest extends CnxAtomPubBasetest {
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
        ClientEntry createResourceEntry = cnxClient.createUploadUrl("test-resource");
        assertNotNull(createResourceEntry);
        assertNotNull(createResourceEntry.getId());
        String resourceId = createResourceEntry.getId();
        String expectedResourceUrl = getConstants().getResourceAbsPath(resourceId).toString();
        assertEquals(expectedResourceUrl, cnxClient.getLinkForResource(createResourceEntry)
            .getHrefResolved());

        /*
         * There should be two links in following order :<br> 1. Link for Blobstore.<br> 2. Link for
         * Resource.<br>
         */
        @SuppressWarnings("unchecked")
        List<Link> listOfLinks = createResourceEntry.getOtherLinks();
        assertEquals(2, listOfLinks.size());

        Link selfLink = listOfLinks.get(0);
        assertEquals(CnxAtomPubConstants.REL_TAG_FOR_SELF_URL, selfLink.getRel());
        assertEquals(expectedResourceUrl, selfLink.getHref());

        Link blobStoreLink = listOfLinks.get(1);
        assertEquals(CnxAtomPubConstants.REL_TAG_FOR_BLOBSTORE_URL, blobStoreLink.getRel());
        assertNotNull(blobStoreLink.getHref());

        // Now upload blob to AppEngine.
        cnxClient.uploadFileToBlobStore(blobStoreLink.getHrefResolved(), file);

        // TODO(arjuns) : Add test for get once it works.
        // TODO(arjuns) : Add link in entry for get.
    }

    // TODO(arjuns) : Move this to separate test.
    @Test
    public void testResourceMigrator() throws HttpException, ProponoException, IOException {
        ResourceMigrator resourceMigrator = new ResourceMigrator(cnxClient);

        ClientEntry createResourceEntry = resourceMigrator.migrateResource(file.getAbsolutePath());
        createResourceEntry.getEditURI();
        assertNotNull(createResourceEntry);
        assertNotNull(createResourceEntry.getId());
        String resourceId = createResourceEntry.getId();
        String expectedResourceUrl = getConstants().getResourceAbsPath(resourceId).toString();
        assertEquals(expectedResourceUrl, cnxClient.getLinkForResource(createResourceEntry)
            .getHrefResolved());
    }
}
