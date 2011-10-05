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
import static org.junit.Assert.assertEquals;

import org.cnx.atompubclient2.CnxClient;

import com.google.cloud.sql.jdbc.internal.Charsets;
import com.google.common.io.CharStreams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.objects.ModuleVersionWrapper;
import org.cnx.common.repository.atompub.objects.ModuleWrapper;
import org.cnx.repository.scripts.ParallelModuleMigrator;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CnxModuleServlet}.
 * 
 * @author Arjun Satyapal
 */
public class CnxModuleServletTest extends CnxAtomPubBasetest {
    private CnxClient cnxClient;
    private final String MODULE_LOCATION = "testdata/m10085";

    @Before
    public void initialize() throws Exception {
        cnxClient = new CnxClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void test_createModule() throws Exception {
        ModuleWrapper module = cnxClient.createModule();
        doTestForCreateModule(module, false /* isMigaration */);
    }

    // TODO(arjuns) : Enable this test once we can have things done locally.
    // @Test
    // public void test_new_createResourceForMigration() throws Exception {
    // ResourceWrapper resource = client2.createNewResourceForMigration(new IdWrapper("r0001",
    // IdWrapper.Type.RESOURCE));
    // validateResource(resource, true /* isMigaration */);
    // }

    private void doTestForCreateModule(ModuleWrapper module, boolean isMigration)
            throws Exception {
        validateAtomPubResource(module, isMigration, IdWrapper.Type.MODULE,
                CnxAtomPubUtils.DEFAULT_VERSION);

        String cnxml = getCnxml(MODULE_LOCATION);
        String resourceMappingXml = getResourceMappingXml(MODULE_LOCATION, isMigration);
        ModuleWrapper moduleWrapper =
                cnxClient.createModuleVersion(module.getEditUri(), cnxml, resourceMappingXml);

        assertEquals(CnxAtomPubUtils.DEFAULT_EDIT_VERSION, moduleWrapper.getVersion());

        // First downloading using version=1.
        ModuleVersionWrapper moduleVersion_1 =
                cnxClient.getModuleVersion(module.getId(), CnxAtomPubUtils.DEFAULT_EDIT_VERSION);
        assertEquals(cnxml, moduleVersion_1.getCnxml());
        assertEquals(resourceMappingXml, moduleVersion_1.getResourceMappingXml());

        // Now downloading ModuleVersion and validating it.
        ModuleVersionWrapper moduleVersionLatest =
                cnxClient.getModuleVersion(module.getId(), CnxAtomPubUtils.LATEST_VERSION_WRAPPER);
        assertEquals(cnxml, moduleVersionLatest.getCnxml());
        assertEquals(resourceMappingXml, moduleVersionLatest.getResourceMappingXml());
    }

    private String getCnxml(String moduleLocation) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream is =
                classLoader.getResourceAsStream(moduleLocation + "/index_auto_generated.cnxml");
        return CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
    }

    private String getResourceMappingXml(String moduleLocation, boolean isMigration)
            throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();

        String fileSystemModuleLocation =
                TestingUtils.getFileSystemPath(classLoader, moduleLocation);
        File file = new File(fileSystemModuleLocation);
        IdWrapper moduleId = new IdWrapper(file.getName(), IdWrapper.Type.MODULE);

        ParallelModuleMigrator moduleMigrator =
                new ParallelModuleMigrator(cnxClient, fileSystemModuleLocation, moduleId,
                        CnxAtomPubUtils.LATEST_VERSION_WRAPPER, isMigration);
        moduleMigrator.migrateModuleVersion();
        return moduleMigrator.getResourceMappingXml();
    }
}
