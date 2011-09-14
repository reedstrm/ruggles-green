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
import static org.junit.Assert.fail;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Logger;
import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.atompubclient.CnxClientUtils;
import org.cnx.exceptions.CnxInvalidUrlException;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.IdWrapper;
import org.cnx.repository.atompub.VersionWrapper;
import org.junit.Before;
import org.junit.Test;

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


    /*
     * Purpose of this test is to test the state after creating a moduleId but not publishing any
     * version.
     */
    @Test
    public void testGetCollectionVersion_withoutPublishingAnyVersion() throws Exception {
        ClientEntry collectionEntry = cnxClient.createNewCollection();

        IdWrapper collectionId = CnxAtomPubConstants.getIdFromAtomPubId(collectionEntry.getId());

        List<VersionWrapper> listOfInvalidVersions =
                Lists.newArrayList(new VersionWrapper(CnxAtomPubConstants.LATEST_VERSION_STRING),
                        new VersionWrapper(0), new VersionWrapper(1));

        for (VersionWrapper currentVersion : listOfInvalidVersions) {
            try {
                cnxClient.getCollectionVersionEntry(collectionId, currentVersion);
                fail("should have failed.");
            } catch (CnxInvalidUrlException e) {
                // expected.
            }
        }
    }

    @Test
    public void testGetCollectionVersion_0() throws Exception {
        File collXml = new File(ORIGINAL_COLLECTION_XML_LOCATION);
        String collXmlAsString = Files.toString(collXml, Charsets.UTF_8);
        
        ClientEntry collectionEntry = cnxClient.createNewCollection();
        IdWrapper collectionId = CnxAtomPubConstants.getIdFromAtomPubId(collectionEntry.getId());

        cnxClient.createNewCollectionVersion(collectionEntry, collXmlAsString);

        VersionWrapper version = new VersionWrapper(0);

        try {
            cnxClient.getModuleVersionEntry(collectionId, version);
            fail("should have failed.");
        } catch (CnxInvalidUrlException e) {
            // expected.
        }
        
        version = new VersionWrapper(1);
        try {
            ClientEntry entry = cnxClient.getModuleVersionEntry(collectionId, version);
            IdWrapper downloadedId = CnxAtomPubConstants.getIdFromAtomPubId(entry.getId());
            VersionWrapper downloadedVersion =
                    CnxAtomPubConstants.getVersionFromAtomPubId(entry.getId());
            
            assertEquals(collectionId, downloadedId);
            assertEquals(version, downloadedVersion);
        } catch (Exception e) {
            fail("should not have failed." + Throwables.getStackTraceAsString(e));
        }
    }
    
    // TODO(arjuns) : Add test for collectionMigration for forced ids.
}
