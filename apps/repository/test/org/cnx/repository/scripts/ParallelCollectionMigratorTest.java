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
package org.cnx.repository.scripts;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionVersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionWrapper;
import org.cnx.repository.atompub.jerseyservlets.CnxAtomPubBasetest;
import org.cnx.repository.scripts.migrators.ParallelCollectionMigrator;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ParallelCollectionMigrator}
 * 
 * @author Arjun Satyapal
 */
public class ParallelCollectionMigratorTest extends CnxAtomPubBasetest {
    private CnxClient cnxClient;
    private VersionWrapper FIRST_VERSION = new VersionWrapper(1);
    private VersionWrapper SECOND_VERSION = new VersionWrapper(2);

    private final String COLLECTION_ID = "col10064";
    private final String COLLECTION_LOCATION = "/home/arjuns/cnxmodules/testdata/col10064/";

    public ParallelCollectionMigratorTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws IOException, URISyntaxException, JAXBException, CnxException {
        cnxClient = new CnxClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testCollectionMigrator_preserveIds() throws Exception {
        IdWrapper collectionIdWrapper = new IdWrapper(COLLECTION_ID, IdWrapper.Type.COLLECTION);
        MigratorUtils.cleanUp(cnxClient, collectionIdWrapper);
        List<File> listOfModules =
                ParallelCollectionMigrator.getListOfModulesToBeUploaded(COLLECTION_LOCATION);
        for (File currFile : listOfModules) {
            IdWrapper tempWrapper = new IdWrapper(currFile.getName(), IdWrapper.Type.MODULE);
            MigratorUtils.cleanUp(cnxClient, tempWrapper);

        }

        CollectionWrapper collectionWrapper =
                cnxClient.createCollectionForMigration(collectionIdWrapper);

        ParallelCollectionMigrator migrator1 =
                new ParallelCollectionMigrator(cnxClient, COLLECTION_LOCATION, collectionWrapper,
                        FIRST_VERSION, true);
        migrator1.migrateCollection();

        CollectionVersionWrapper collectionVersionWrapper =
                cnxClient.getCollectionVersion(collectionIdWrapper,
                        CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(FIRST_VERSION, collectionVersionWrapper.getVersion());

        // Now publishing second version.
        ParallelCollectionMigrator migrator2 =
                new ParallelCollectionMigrator(cnxClient, COLLECTION_LOCATION, collectionWrapper,
                        SECOND_VERSION, true);
        migrator2.migrateCollection();

        // Validating version.
        collectionVersionWrapper =
                cnxClient.getCollectionVersion(collectionIdWrapper,
                        CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(SECOND_VERSION, collectionVersionWrapper.getVersion());
    }

    @Test
    public void testModuleMigrator_newIds() throws Exception {
        ParallelCollectionMigrator migrator1 =
                new ParallelCollectionMigrator(cnxClient, COLLECTION_LOCATION,
                        null /* collectionWrapper */, null /* version */, false);
        migrator1.migrateCollection();
        CollectionWrapper collectionWrapper = migrator1.getCollectionWrapper();

        CollectionVersionWrapper moduleVersionWrapper =
                cnxClient.getCollectionVersion(collectionWrapper.getId(),
                        CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(FIRST_VERSION, moduleVersionWrapper.getVersion());

        // Now publishing second version.
        ParallelCollectionMigrator migrator2 =
                new ParallelCollectionMigrator(cnxClient, COLLECTION_LOCATION, collectionWrapper,
                        SECOND_VERSION, true);
        migrator2.migrateCollection();
        collectionWrapper = migrator2.getCollectionWrapper();

        // Validating version.
        moduleVersionWrapper =
                cnxClient.getCollectionVersion(collectionWrapper.getId(),
                        CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(SECOND_VERSION, moduleVersionWrapper.getVersion());
    }
}
