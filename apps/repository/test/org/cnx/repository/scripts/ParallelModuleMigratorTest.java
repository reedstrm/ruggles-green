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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.IdWrapper;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.repository.atompub.jerseyservlets.CnxAtomPubBasetest;
import org.cnx.repository.scripts.migrators.ParallelModuleMigrator;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;

/**
 * Test for {@link ParallelModuleMigrator}
 * 
 * @author Arjun Satyapal
 */
public class ParallelModuleMigratorTest extends CnxAtomPubBasetest {
    private CnxAtomPubClient cnxClient;

    private final String MODULE_LOCATION = "/home/arjuns/cnxmodules/col10064_1.13_complete/m10085";

    public ParallelModuleMigratorTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws MalformedURLException, ProponoException {
        cnxClient = new CnxAtomPubClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testModuleMigrator() throws Exception {
        ParallelModuleMigrator migrator =
            new ParallelModuleMigrator(cnxClient, MODULE_LOCATION, null /* collXmlModuleId */,
                    null/* cnxModuleId */, null /* aerModuleId */, null /* version */);
        Entry moduleEntry = migrator.migrateModuleVersion();
        assertNotNull(moduleEntry);

        VersionWrapper expectedVersion = new VersionWrapper(1);
        assertEquals(expectedVersion,
                CnxAtomPubConstants.getVersionFromAtomPubId(moduleEntry.getId()));
    }

    @Test
    public void testMultipleModuleVersions() throws Exception {
        // Create first version.
        ParallelModuleMigrator migrator =
            new ParallelModuleMigrator(cnxClient, MODULE_LOCATION, null/* collXmlModuleId */,
                    null /* cnxModuleId */, null/* aerModuleId */, null /* version */);
        Entry moduleEntry = migrator.migrateModuleVersion();

        IdWrapper aerModuleId = CnxAtomPubConstants.getIdFromAtomPubId(moduleEntry.getId());

        VersionWrapper firstVersion = CnxAtomPubConstants.NEW_MODULE_DEFAULT_VERSION;
        // Now publishing second version.
        ParallelModuleMigrator migrator2 =
            new ParallelModuleMigrator(cnxClient, MODULE_LOCATION, null /* collXmlModuleId */,
                    null/* cnxModuleId */, aerModuleId, firstVersion);

        migrator2.migrateModuleVersion();

        // Validating version.
        ClientEntry clientEntry =
            cnxClient.getModuleVersionEntry(aerModuleId, LATEST_VERSION_WRAPPER);
        VersionWrapper expectedVersion = new VersionWrapper(2);
        VersionWrapper actualVersion =
            CnxAtomPubConstants.getVersionFromAtomPubId(clientEntry.getId());

        assertEquals(expectedVersion, actualVersion);
    }
    // TODO(arjuns): Add more tests when modules are migrated in context of collection.
    // TODO(arjuns) : Add tests for forcedId.
}
