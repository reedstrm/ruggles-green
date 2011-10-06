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

import org.cnx.repository.scripts.MigratorUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.common.exceptions.CnxBadRequestException;
import org.cnx.common.exceptions.CnxConflictException;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.exceptions.CnxPreconditionFailedException;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.ModuleVersionWrapper;
import org.cnx.common.repository.atompub.objects.ModuleWrapper;
import org.cnx.repository.scripts.migrators.ParallelModuleMigrator;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CnxAtomModuleServlet}
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomModuleServletTests extends CnxAtomPubBasetest {
    private CnxClient cnxClient;
    private VersionWrapper FIRST_VERSION = new VersionWrapper(1);
    private VersionWrapper SECOND_VERSION = new VersionWrapper(2);
    private VersionWrapper THIRD_VERSION = new VersionWrapper(3);
    private IdWrapper MODULE_ID_WRAPPER = new IdWrapper("m10085", IdWrapper.Type.MODULE);
    private final String MODULE_LOCATION = "/home/arjuns/cnxmodules/col10064_1.13_complete/"
            + MODULE_ID_WRAPPER.getId();

    public CnxAtomModuleServletTests() throws Exception {
        super();
    }

    @Before
    public void initialize() throws IOException, URISyntaxException, JAXBException, CnxException {
        cnxClient = new CnxClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void test_createModule() throws Exception {
        ModuleWrapper module = cnxClient.createModule();
        doTestForCreateModule(module, false /* isMigaration */);
    }

    @Test
    public void test_createModuleForMigration() throws Exception {
        MigratorUtils.cleanUp(cnxClient, MODULE_ID_WRAPPER);
        ModuleWrapper module = cnxClient.createModuleForMigration(MODULE_ID_WRAPPER);
        doTestForCreateModule(module, true /* isMigaration */);
    }

    private void doTestForCreateModule(final ModuleWrapper moduleWrapper, boolean isMigration)
            throws Exception {
        TestingUtils.validateAtomPubResource(moduleWrapper, isMigration, IdWrapper.Type.MODULE,
                CnxAtomPubUtils.DEFAULT_VERSION);

        ParallelModuleMigrator moduleMigrator =
                migrateModule(moduleWrapper, FIRST_VERSION, MODULE_LOCATION, isMigration);

        ModuleWrapper moduleWrapper1 = moduleMigrator.getModuleWrapper();

        assertEquals(FIRST_VERSION, moduleWrapper1.getVersion());

        // First downloading using version=1.
        ModuleVersionWrapper moduleVersion_1 =
                cnxClient.getModuleVersion(moduleWrapper1.getId(), FIRST_VERSION);
        assertEquals(moduleMigrator.getCnxml(), moduleVersion_1.getCnxml());
        assertEquals(moduleMigrator.getResourceMappingXml(),
                moduleVersion_1.getResourceMappingXml());

        // Now downloading ModuleVersion and validating it.
        ModuleVersionWrapper moduleVersionLatest =
                cnxClient.getModuleVersion(moduleWrapper1.getId(),
                        CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(moduleMigrator.getCnxml(), moduleVersionLatest.getCnxml());
        assertEquals(moduleMigrator.getResourceMappingXml(),
                moduleVersionLatest.getResourceMappingXml());
    }

    private ParallelModuleMigrator migrateModule(ModuleWrapper moduleWrapper,
            @Nullable VersionWrapper newVersion,
            String moduleLocation, boolean isMigration) {
        ParallelModuleMigrator moduleMigrator =
                new ParallelModuleMigrator(cnxClient, moduleLocation, moduleWrapper,
                        newVersion, isMigration);
        moduleMigrator.migrateModuleVersion();
        return moduleMigrator;
    }

    @Test
    public void test_createModuleForMigration_MultipleTimes() throws Exception {
        MigratorUtils.cleanUp(cnxClient, MODULE_ID_WRAPPER);
        // When used for migration, we can create same module many times.
        cnxClient.createModuleForMigration(MODULE_ID_WRAPPER);
        cnxClient.createModuleForMigration(MODULE_ID_WRAPPER);

        // This should pass successfully.
        // TODO(arjuns) : ONce module info is implemented, update this test.
    }

    @Test
    public void test_createModuleVersion_WithGaps() throws Exception {
        ModuleWrapper moduleWrapper = cnxClient.createModule();
        doTestFor_createModuleVersion_withGaps(moduleWrapper, false /*isMigration*/);
    }

    private void doTestFor_createModuleVersion_withGaps(ModuleWrapper moduleWrapper,
            boolean isMigration)
            throws Exception {
        publishVersionForModule(moduleWrapper, FIRST_VERSION, isMigration);

        // Now publishing THIRD_VERSION
        if (isMigration) {
            publishVersionForModule(moduleWrapper, THIRD_VERSION, isMigration);
            // This should pass successfully for migration.
        } else {
            try {
                publishVersionForModule(moduleWrapper, THIRD_VERSION, isMigration);
                fail("should have failed");
            } catch (CnxConflictException e) {
                // expected.
                return;
            }
        }
        
        // Now trying to publish SECOND_VERSION. This should result in conflict.
        try {
            publishVersionForModule(moduleWrapper, SECOND_VERSION, isMigration);
            fail("should have failed.");
        } catch (CnxConflictException e) {
            // expected.
        }
    }

    @Test
    public void test_createModuleVersionForMigration_WithGaps() throws Exception {
        MigratorUtils.cleanUp(cnxClient, MODULE_ID_WRAPPER);
        ModuleWrapper moduleWrapper = cnxClient.createModuleForMigration(MODULE_ID_WRAPPER);
        
        doTestFor_createModuleVersion_withGaps(moduleWrapper, true /*isMigration*/);
    }

    /*
     * Purpose of this test is to test the state after creating a moduleId but not publishing any
     * version.
     */
    @Test
    public void testGetModule_LatestVersion_withoutPublishingAnyVersion() throws Exception {
        ModuleWrapper moduleWrapper = cnxClient.createModule();

        try {
            cnxClient.getModuleVersion(moduleWrapper.getId(),
                    CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
            fail("should have failed.");
        } catch (CnxPreconditionFailedException e) {
            // expected.
        }

        publishVersionForModule(moduleWrapper, FIRST_VERSION, false /*isMigration*/);

        // This should pass fine.
        cnxClient.getModuleVersion(moduleWrapper.getId(), CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
    }

    @Test
    public void testGetModuleVersion_0() throws Exception {
        ModuleWrapper moduleWrapper = cnxClient.createModule();

        // First without publishing any version.
        try {
            cnxClient.getModuleVersion(moduleWrapper.getId(),
                    CnxAtomPubUtils.DEFAULT_VERSION);
            fail("should have failed.");
        } catch (CnxBadRequestException e) {
            // expected.
        }

        // Now publishing one version.
        publishVersionForModule(moduleWrapper, FIRST_VERSION, false /*isMigration*/);

        // Still the output should remain same.
        try {
            cnxClient.getModuleVersion(moduleWrapper.getId(),
                    CnxAtomPubUtils.DEFAULT_VERSION);
            fail("should have failed.");
        } catch (CnxBadRequestException e) {
            // expected.
        }
    }

    private ModuleWrapper publishVersionForModule(ModuleWrapper moduleWrapper,
            VersionWrapper version, boolean isMigration) throws Exception {
        Map<String, IdWrapper> map = Maps.newConcurrentMap();
        String resourceMappingXml = cnxClient.getResourceMappingXml(map);

        File cnxmlFile = new File(MODULE_LOCATION + "/index_auto_generated.cnxml");
        String cnxmlAsString = Files.toString(cnxmlFile, Charsets.UTF_8);

        // Publishing FIRST_VERSION.
        if (isMigration) {
            return cnxClient.createModuleVersionForMigration(moduleWrapper.getId(), version,
                    cnxmlAsString, resourceMappingXml);
        } else {
            return cnxClient.createModuleVersion(moduleWrapper.getId(), version, cnxmlAsString,
                    resourceMappingXml);
        }
    }
}
