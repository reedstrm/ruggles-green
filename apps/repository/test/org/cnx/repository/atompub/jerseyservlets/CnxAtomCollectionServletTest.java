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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.sun.syndication.io.FeedException;
import org.jdom.JDOMException;

import org.cnx.common.exceptions.CnxConflictException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.common.exceptions.CnxBadRequestException;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.exceptions.CnxPreconditionFailedException;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionVersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionWrapper;
import org.cnx.repository.scripts.MigratorUtils;
import org.cnx.repository.scripts.migrators.ParallelCollectionMigrator;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CnxAtomModuleServlet}
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomCollectionServletTest extends CnxAtomPubBasetest {
    private CnxClient cnxClient;
    private VersionWrapper FIRST_VERSION = new VersionWrapper(1);
    @SuppressWarnings("unused")
    private VersionWrapper SECOND_VERSION = new VersionWrapper(2);
    @SuppressWarnings("unused")
    private VersionWrapper THIRD_VERSION = new VersionWrapper(3);
    private IdWrapper COLLECTION_ID_WRAPPER = new IdWrapper("col10064", IdWrapper.Type.COLLECTION);
    private final String COLLECTION_LOCATION = "/home/arjuns/cnxmodules/testdata/col10064/";

    public CnxAtomCollectionServletTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws IOException, URISyntaxException, JAXBException, CnxException {
        cnxClient = new CnxClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void test_createCollection() throws Exception {
        CollectionWrapper collection = cnxClient.createCollection();
        doTestForCreateCollection(collection, false /* isMigaration */);
    }

    @Test
    public void test_createCollectionForMigration() throws Exception {
        MigratorUtils.cleanUp(cnxClient, COLLECTION_ID_WRAPPER);
        CollectionWrapper collection =
                cnxClient.createCollectionForMigration(COLLECTION_ID_WRAPPER);
        doTestForCreateCollection(collection, true /* isMigaration */);
    }

    private void doTestForCreateCollection(final CollectionWrapper collectionWrapper,
            boolean isMigration)
            throws Exception {
        TestingUtils.validateAtomPubResource(collectionWrapper, isMigration,
                IdWrapper.Type.COLLECTION,
                CnxAtomPubUtils.DEFAULT_VERSION);

        ParallelCollectionMigrator collectionMigrator =
                migrateCollection(collectionWrapper, FIRST_VERSION, COLLECTION_LOCATION,
                        isMigration);

        CollectionWrapper collectionWrapper1 = collectionMigrator.getCollectionWrapper();

        assertEquals(FIRST_VERSION, collectionWrapper1.getVersion());

        // First downloading using version=1.
        CollectionVersionWrapper collectionVersion_1 =
                cnxClient.getCollectionVersion(collectionWrapper1.getId(), FIRST_VERSION);
        assertEquals(collectionMigrator.getCollectionXml(), collectionVersion_1.getCollectionXml());

        // Now downloading ModuleVersion and validating it.
        CollectionVersionWrapper collectionVersionLatest =
                cnxClient.getCollectionVersion(collectionWrapper1.getId(),
                        CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(collectionMigrator.getCollectionXml(),
                collectionVersionLatest.getCollectionXml());
    }

    private ParallelCollectionMigrator migrateCollection(CollectionWrapper collectionWrapper,
            @Nullable VersionWrapper newVersion,
            String moduleLocation, boolean isMigration) {
        ParallelCollectionMigrator collectionMigrator =
                new ParallelCollectionMigrator(cnxClient, moduleLocation, collectionWrapper,
                        newVersion, isMigration);
        collectionMigrator.migrateCollection();
        return collectionMigrator;
    }

    @Test
    public void test_createCollectionForMigration_MultipleTimes() throws Exception {
        MigratorUtils.cleanUp(cnxClient, COLLECTION_ID_WRAPPER);
        // When used for migration, we can create same module many times.
        cnxClient.createCollectionForMigration(COLLECTION_ID_WRAPPER);
        cnxClient.createCollectionForMigration(COLLECTION_ID_WRAPPER);

        // This should pass successfully.
        // TODO(arjuns) : ONce module info is implemented, update this test.
    }

    @Test
    public void test_createModuleVersion_withGaps() throws Exception {
        CollectionWrapper collectionWrapper =                 cnxClient.createCollection();

        dotTest_createModuleVersion_withGaps(collectionWrapper, true /* isMigration */);
    }
    
    @Test
    public void test_createModuleVersionForMigration_WithGaps() throws Exception {
        MigratorUtils.cleanUp(cnxClient, COLLECTION_ID_WRAPPER);
        CollectionWrapper collectionWrapper =
                cnxClient.createCollectionForMigration(COLLECTION_ID_WRAPPER);

        dotTest_createModuleVersion_withGaps(collectionWrapper, true /* isMigration */);
    }

    private void dotTest_createModuleVersion_withGaps(CollectionWrapper collectionWrapper,
            boolean isMigration) throws IOException, JDOMException,
            FeedException, URISyntaxException, CnxException, Exception {
        publishVersionForCollection(collectionWrapper, FIRST_VERSION);

        // Now publishing THIRD_VERSION
        if (isMigration) {
            publishVersionForCollection(collectionWrapper, THIRD_VERSION);
        } else {
            try {
                publishVersionForCollection(collectionWrapper, THIRD_VERSION);
                fail("should have failed.");
            } catch (CnxConflictException e) {
                // expected
                return;
            }
        }
        
        // Now trying to publish SECOND_VERSION. This should result in conflict.
        try {
            publishVersionForCollection(collectionWrapper, SECOND_VERSION);
            fail("should have failed.");
        } catch (CnxConflictException e) {
            // expected.
        }
    }

    /*
     * Purpose of this test is to test the state after creating a moduleId but not publishing any
     * version.
     */
    @Test
    public void testGetModule_LatestVersion_withoutPublishingAnyVersion() throws Exception {
        CollectionWrapper collectionWrapper = cnxClient.createCollection();

        try {
            cnxClient.getCollectionVersion(collectionWrapper.getId(),
                    CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
            fail("should have failed.");
        } catch (CnxPreconditionFailedException e) {
            // expected.
        }

        publishVersionForCollection(collectionWrapper, FIRST_VERSION);

        // This should pass fine.
        cnxClient.getCollectionVersion(collectionWrapper.getId(),
                CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
    }

    @Test
    public void testGetCollectionVersion_0() throws Exception {
        CollectionWrapper collectionWrapper = cnxClient.createCollection();

        // First without publishing any version.
        try {
            cnxClient.getCollectionVersion(collectionWrapper.getId(),
                    CnxAtomPubUtils.DEFAULT_VERSION);
            fail("should have failed.");
        } catch (CnxBadRequestException e) {
            // expected.
        }

        // Now publishing one version.
        publishVersionForCollection(collectionWrapper, FIRST_VERSION);

        // Still the output should remain same.
        try {
            cnxClient.getCollectionVersion(collectionWrapper.getId(),
                    CnxAtomPubUtils.DEFAULT_VERSION);
            fail("should have failed.");
        } catch (CnxBadRequestException e) {
            // expected.
        }
    }

    //
    private CollectionWrapper publishVersionForCollection(CollectionWrapper collectionWrapper,
            VersionWrapper version) throws Exception {
        File collxmlFile = new File(COLLECTION_LOCATION + "/collection.xml");
        String collxmlAsString = Files.toString(collxmlFile, Charsets.UTF_8);

        // Publishing FIRST_VERSION.
        return cnxClient.createCollectionVersionForMigration(collectionWrapper.getId(), version,
                collxmlAsString);
    }
}
