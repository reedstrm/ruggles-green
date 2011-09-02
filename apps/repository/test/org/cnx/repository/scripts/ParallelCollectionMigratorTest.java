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

import static org.cnx.repository.atompub.CnxAtomPubConstants.LATEST_VERSION_WRAPPER;
import static org.cnx.repository.atompub.CnxAtomPubConstants.NEW_CNX_COLLECTION_DEFAULT_VERSION;
import static org.cnx.repository.atompub.CnxAtomPubConstants.getVersionFromAtomPubId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.repository.atompub.jerseyservlets.CnxAtomPubBasetest;
import org.cnx.repository.scripts.migrators.ParallelCollectionMigrator;
import org.cnx.repository.scripts.migrators.ParallelModuleMigrator;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;

/**
 * Test for {@link ParallelModuleMigrator}
 * 
 * @author Arjun Satyapal
 */
public class ParallelCollectionMigratorTest extends CnxAtomPubBasetest {
    private CnxAtomPubClient cnxClient;

    private final String COLLECTION_LOCATION =
            "/home/arjuns/cnxmodules/col10064_1.12_complete.bak/";

    public ParallelCollectionMigratorTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws MalformedURLException, ProponoException {
        cnxClient = new CnxAtomPubClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testCreateNewCollection() throws Exception {
        ParallelCollectionMigrator migrator =
                new ParallelCollectionMigrator(cnxClient, COLLECTION_LOCATION,
                        null /* cnxCollectionId */, null /* aerCollectionId */, null /* version */,
                        false /* preserveIds */);

        Entry collectionEntry = migrator.migrateCollectionVersion();
        assertNotNull(collectionEntry);

        assertEquals(NEW_CNX_COLLECTION_DEFAULT_VERSION,
                getVersionFromAtomPubId(collectionEntry.getId()));
    }

    @Test
    public void testMultipleCollectionVersions() throws Exception {
        // Create first version.
        ParallelCollectionMigrator migrator =
                new ParallelCollectionMigrator(cnxClient, COLLECTION_LOCATION,
                        null /* cnxCollectionId */, null /* aerCollectionId */, null /* version */,
                        false /* preserveIds */);

        Entry collectionEntry = migrator.migrateCollectionVersion();

        String aerCollectionId = CnxAtomPubConstants.getIdFromAtomPubId(collectionEntry.getId());

        // TODO(arjuns) : Rename to NEW_COLLECTION_DEFAULT_VERSION
        VersionWrapper firstVersion = CnxAtomPubConstants.NEW_CNX_COLLECTION_DEFAULT_VERSION;
        // Now publishing second version.
        ParallelCollectionMigrator migrator2 =
                new ParallelCollectionMigrator(cnxClient, COLLECTION_LOCATION,
                        null /* cnxCollectionId */, aerCollectionId, firstVersion, 
                        false /* preserveIds */);

        migrator2.migrateCollectionVersion();

        // Validating version.
        ClientEntry clientEntry =
                cnxClient.getCollectionVersionEntry(aerCollectionId, LATEST_VERSION_WRAPPER);
        VersionWrapper expectedVersion = new VersionWrapper(2);
        VersionWrapper actualVersion =
                CnxAtomPubConstants.getVersionFromAtomPubId(clientEntry.getId());

        assertEquals(expectedVersion, actualVersion);
    }
    // TODO(arjuns): Add more tests when modules are migrated in context of collection.
    // TODO(arjuns) : Add tests for forcedId.
}
