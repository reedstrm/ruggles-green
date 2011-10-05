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

import static org.cnx.repository.atompub.jerseyservlets.TestingUtils.validateAtomPubResource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.objects.CollectionWrapper;
import org.cnx.repository.scripts.ParallelCollectionMigrator;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CnxCollectionServlet}.
 * 
 * @author Arjun Satyapal
 */
public class CnxCollectionServletTest extends CnxAtomPubBasetest {
    private CnxClient cnxClient;
    private final String COLLECTION_LOCATION = "testdata/col10064";

    @Before
    public void initialize() throws IOException, JAXBException, URISyntaxException, CnxException {
        cnxClient = new CnxClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void test_createCollection() throws Exception {
        CollectionWrapper collection = cnxClient.createCollection();
        doTestForCreateCollection(collection, false /* isMigaration */);
    }

    // TODO(arjuns) : Enable this test once we can have things done locally.
    // @Test
    // public void test_new_createResourceForMigration() throws Exception {
    // ResourceWrapper resource = client2.createNewResourceForMigration(new IdWrapper("r0001",
    // IdWrapper.Type.RESOURCE));
    // validateResource(resource, true /* isMigaration */);
    // }

    private void doTestForCreateCollection(CollectionWrapper collection, boolean isMigration)
            throws Exception {
        validateAtomPubResource(collection, isMigration, IdWrapper.Type.COLLECTION,
                CnxAtomPubUtils.DEFAULT_VERSION);
        String collectionXml = getCollectionXml(COLLECTION_LOCATION, isMigration);
        // String collectionXml = getCollectionXml(COLLECTION_LOCATION);
        // // String resourceMappingXml = getResourceMappingXml(MODULE_LOCATION);
        // CollectionWrapper collectionWrapper =
        // cnxClient.createCollectionVersion(collection.getEditUri(), collectionXml);
        // assertEquals(CnxAtomPubUtils.DEFAULT_EDIT_VERSION, collectionWrapper.getVersion());
        // //
        // // First downloading using version=1.
        // CollectionVersionWrapper collectionVersion_1 =
        // cnxClient.getCollectionVersion(collection.getId(),
        // CnxAtomPubUtils.DEFAULT_EDIT_VERSION);
        // assertEquals(collectionXml, collectionVersion_1.getCollectionXml());
        //
        // // Now downloading using latest.
        // CollectionVersionWrapper moduleVersionLatest =
        // cnxClient.getCollectionVersion(collection.getId(),
        // CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        // assertEquals(collectionXml, moduleVersionLatest.getCollectionXml());
    }

    private String getCollectionXml(String collectionLocation, boolean isMigration)
            throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String fileSystemCollectionLocation =
                TestingUtils.getFileSystemPath(classLoader, collectionLocation);
        File file = new File(fileSystemCollectionLocation);
        IdWrapper collectionId = new IdWrapper(file.getName(), IdWrapper.Type.COLLECTION);

        ParallelCollectionMigrator collectionMigrator =
                new ParallelCollectionMigrator(cnxClient,
                        fileSystemCollectionLocation, collectionId,
                        CnxAtomPubUtils.LATEST_VERSION_WRAPPER, isMigration);

        collectionMigrator.migrateCollection();

        return collectionMigrator.getCollectionXml();
    }
}
