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

import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.atompubclient.CnxClientUtils;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.IdWrapper;
import org.cnx.repository.atompub.VersionWrapper;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

/**
 * Test for {@link CnxAtomCollectionServlet}
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomCollectionServletTest extends CnxAtomPubBasetest {
    Logger logger = Logger.getLogger(CnxAtomCollectionServletTest.class.getName());
    private CnxAtomPubClient cnxClient;

    private final String COLLECTION_LOCATION = "/home/arjuns/cnxmodules/col10064_1.13_complete/";
    private final String ORIGINAL_COLLECTION_XML_LOCATION = COLLECTION_LOCATION + "collection.xml";

    public CnxAtomCollectionServletTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws MalformedURLException, ProponoException {
        cnxClient = new CnxAtomPubClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testCreateCollection() throws Exception {
        File collXml = new File(ORIGINAL_COLLECTION_XML_LOCATION);
        String collXmlAsString = Files.toString(collXml, Charsets.UTF_8);
        ClientEntry collectionEntry = cnxClient.createNewCollection();
        IdWrapper collectionId = CnxAtomPubConstants.getIdFromAtomPubId(collectionEntry.getId());

        // TODO(arjuns) : Add a regex test here.
        String expectedCollectionUrl =
                cnxClient.getConstants().getAtomPubRestUrl() + "/collection/"
                        + collectionId.getId() + "/1";
        assertEquals(expectedCollectionUrl, collectionEntry.getEditURI().toString());

        cnxClient.createNewCollectionVersion(collectionEntry, collXmlAsString);

        Link selfLink = CnxClientUtils.getSelfUri(collectionEntry);
        assertEquals(expectedCollectionUrl, selfLink.getHrefResolved());

        logger.info("New location for collection = \n" + expectedCollectionUrl);

        VersionWrapper latestVersion =
                new VersionWrapper(CnxAtomPubConstants.LATEST_VERSION_STRING);
        ClientEntry getEntry = cnxClient.getCollectionVersionEntry(collectionId, latestVersion);

        String downloadedCollXml =
                cnxClient.getConstants().getCollXmlDocFromAtomPubCollectionEntry(getEntry);

         assertEquals(collXmlAsString, downloadedCollXml);
    }

    @Test
    public void testCreateCollectionMultipleVersion() throws Exception {
        File collXml = new File(ORIGINAL_COLLECTION_XML_LOCATION);
        String collXmlAsString = Files.toString(collXml, Charsets.UTF_8);
        ClientEntry collectionEntry = cnxClient.createNewCollection();

        cnxClient.createNewCollectionVersion(collectionEntry, collXmlAsString);
        cnxClient.createNewCollectionVersion(collectionEntry, collXmlAsString);

        IdWrapper collectionId = CnxAtomPubConstants.getIdFromAtomPubId(collectionEntry.getId());
        ClientEntry latestEntry =
                cnxClient.getCollectionVersionEntry(collectionId, new VersionWrapper(
                        CnxAtomPubConstants.LATEST_VERSION_STRING));
        assertEquals(new VersionWrapper(2),
                CnxAtomPubConstants.getVersionFromAtomPubId(latestEntry.getId()));
    }

    // TODO(arjuns) : Add test for collectionMigration for forced ids.
}
