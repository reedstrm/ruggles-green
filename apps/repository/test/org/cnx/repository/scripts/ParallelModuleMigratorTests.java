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

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.cnx.atompubclient2.CnxClient;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.ModuleVersionWrapper;
import org.cnx.common.repository.atompub.objects.ModuleWrapper;
import org.cnx.repository.atompub.jerseyservlets.CnxAtomPubBasetest;
import org.cnx.repository.scripts.migrators.ParallelModuleMigrator;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ParallelModuleMigrator}
 * 
 * @author Arjun Satyapal
 */
public class ParallelModuleMigratorTests extends CnxAtomPubBasetest {
    private CnxClient cnxClient;
    private VersionWrapper FIRST_VERSION = new VersionWrapper(1);
    private VersionWrapper SECOND_VERSION = new VersionWrapper(2);

    private final String MODULE_ID = "m10085";
    private final String MODULE_LOCATION = "/home/arjuns/cnxmodules/col10064_1.13_complete/"
            + MODULE_ID;

    public ParallelModuleMigratorTests() throws Exception {
        super();
    }

    @Before
    public void initialize() throws IOException, URISyntaxException, JAXBException, CnxException {
        cnxClient = new CnxClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testModuleMigrator_preserveIds() throws Exception {
        IdWrapper moduleIdWrapper = new IdWrapper(MODULE_ID, IdWrapper.Type.MODULE);
        MigratorUtils.cleanUp(cnxClient, moduleIdWrapper);

        ModuleWrapper moduleWrapper = cnxClient.createModuleForMigration(moduleIdWrapper);

        ParallelModuleMigrator migrator1 =
                new ParallelModuleMigrator(cnxClient, MODULE_LOCATION, moduleWrapper,
                        FIRST_VERSION, true);
        migrator1.migrateModuleVersion();
        ModuleVersionWrapper moduleVersionWrapper =
                cnxClient.getModuleVersion(moduleIdWrapper, CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(FIRST_VERSION, moduleVersionWrapper.getVersion());

        // Now publishing second version.
        ParallelModuleMigrator migrator2 =
                new ParallelModuleMigrator(cnxClient, MODULE_LOCATION, moduleWrapper,
                        SECOND_VERSION, true);
        migrator2.migrateModuleVersion();
        moduleWrapper = migrator2.getModuleWrapper();

        // Validating version.
        moduleVersionWrapper =
                cnxClient.getModuleVersion(moduleIdWrapper, CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(SECOND_VERSION, moduleVersionWrapper.getVersion());
    }

    @Test
    public void testModuleMigrator_newIds() throws Exception {
        ParallelModuleMigrator migrator1 =
                new ParallelModuleMigrator(cnxClient, MODULE_LOCATION, null /* moduleWrapper */,
                        null /*version*/, false);
        migrator1.migrateModuleVersion();
        ModuleWrapper moduleWrapper = migrator1.getModuleWrapper();

        ModuleVersionWrapper moduleVersionWrapper =
                cnxClient.getModuleVersion(moduleWrapper.getId(),
                        CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(FIRST_VERSION, moduleVersionWrapper.getVersion());

        // Now publishing second version.
        ParallelModuleMigrator migrator2 =
                new ParallelModuleMigrator(cnxClient, MODULE_LOCATION, moduleWrapper,
                        SECOND_VERSION,
                        true);
        migrator2.migrateModuleVersion();
        moduleWrapper = migrator2.getModuleWrapper();

        // Validating version.
        moduleVersionWrapper =
                cnxClient.getModuleVersion(moduleWrapper.getId(),
                        CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(SECOND_VERSION, moduleVersionWrapper.getVersion());
    }
}
