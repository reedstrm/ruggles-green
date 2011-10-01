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
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.common.exceptions.CnxConflictException;
import org.cnx.common.exceptions.CnxInvalidUrlException;
import org.cnx.common.repository.atompub.CnxAtomPubLinkRelations;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.repository.scripts.migrators.ParallelModuleMigrator;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CnxAtomModuleServlet}
 * 
 * @author Arjun Satyapal
 */
@Deprecated
public class CnxAtomModuleServletTest extends CnxAtomPubBasetest {
    private Logger logger = Logger.getLogger(CnxAtomModuleServletTest.class.getName());
    private CnxAtomPubClient cnxClient;
    private final String MODULE_LOCATION = "/home/arjuns/testdata/modules/m1000001";

    public CnxAtomModuleServletTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws ProponoException, IOException {
        cnxClient = new CnxAtomPubClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testMigrateCompleteModule() throws Exception {
        List<File> listOfResourcesToUpload =
                ParallelModuleMigrator.getListOfResourcesToBeUploaded(MODULE_LOCATION);

        List<ClientEntry> listOfEntryForUploadedResources = Lists.newArrayList();
//        for (File currFile : listOfResourcesToUpload) {
//            break;
//            ClientEntry resourceEntry = cnxClient.createNewResource();
//            logger.info("Attempting to upload : " + currFile.getAbsolutePath());
//            cnxClient.uploadFileToBlobStore(resourceEntry, currFile);
//            listOfEntryForUploadedResources.add(resourceEntry);
//            logger.info("Successuflly uploaded [" + currFile.getName() + "] as resourceId["
//                    + resourceEntry.getId() + "], and can be found here ["
//                    + CnxAtomPubLinkRelations.getUploadUri(resourceEntry).getHrefResolved() + "].");
//            // TODO(arjuns) : Add validations here.
//
//        }

        String resourceMappingDocXml =
                cnxClient.getResourceMappingFromResourceEntries(listOfEntryForUploadedResources);

        File cnxml = new File(MODULE_LOCATION + "/index_auto_generated.cnxml");
        String cnxmlAsString = Files.toString(cnxml, Charsets.UTF_8);

        ClientEntry moduleEntry = cnxClient.createNewModule();
        // TODO(arjuns): Add more validations here.

        cnxClient.createNewModuleVersion(moduleEntry, cnxmlAsString, resourceMappingDocXml);
        // TODO(arjuns) : Add more validations here.

        // TODO(arjuns) : refactor this.
        IdWrapper moduleId = CnxAtomPubUtils.getIdFromAtomPubId(moduleEntry.getId());
        VersionWrapper version = CnxAtomPubUtils.getVersionFromAtomPubId(moduleEntry.getId());
        ClientEntry getEntry = cnxClient.getModuleVersionEntry(moduleId, version);

        String downloadedCnxmlDoc = cnxClient.getCnxml(getEntry);
        assertEquals(cnxmlAsString, downloadedCnxmlDoc);

        String downloadedResourceMappingDoc = cnxClient.getResourceMappingXml(getEntry);
        assertEquals(resourceMappingDocXml, downloadedResourceMappingDoc);
        // TODO(arjuns) : Add test for links.
    }

    @Test
    public void testCreateModuleMultipleVersion() throws Exception {
        List<ClientEntry> listOfEntryForUploadedResources = Lists.newArrayList();
        String resourceMappingXml =
                cnxClient.getResourceMappingFromResourceEntries(listOfEntryForUploadedResources);

        File cnxml = new File(MODULE_LOCATION + "/index_auto_generated.cnxml");
        String cnxmlAsString = Files.toString(cnxml, Charsets.UTF_8);

        ClientEntry moduleEntry = cnxClient.createNewModule();

        cnxClient.createNewModuleVersion(moduleEntry, cnxmlAsString, resourceMappingXml);

        cnxClient.createNewModuleVersion(moduleEntry, cnxmlAsString, resourceMappingXml);

        IdWrapper moduleId = CnxAtomPubUtils.getIdFromAtomPubId(moduleEntry.getId());
        ClientEntry latestEntry =
                cnxClient.getModuleVersionEntry(moduleId, new VersionWrapper(
                        CnxAtomPubUtils.LATEST_VERSION_STRING));
        assertEquals(new VersionWrapper(2),
                CnxAtomPubUtils.getVersionFromAtomPubId(latestEntry.getId()));

        String downloadedCnxml = cnxClient.getCnxml(latestEntry);
        assertEquals(cnxmlAsString, downloadedCnxml);

        String downloadedResourceMappingXml = cnxClient.getResourceMappingXml(latestEntry);
        assertEquals(resourceMappingXml, downloadedResourceMappingXml);
    }

    /*
     * Purpose of this test is to test the state after creating a moduleId but not publishing any
     * version.
     */
    @Test
    public void testGetModuleVersion_withoutPublishingAnyVersion() throws Exception {
        ClientEntry moduleEntry = cnxClient.createNewModule();

        IdWrapper moduleId = CnxAtomPubUtils.getIdFromAtomPubId(moduleEntry.getId());

        List<VersionWrapper> listOfInvalidVersions =
                Lists.newArrayList(new VersionWrapper(CnxAtomPubUtils.LATEST_VERSION_STRING),
                        new VersionWrapper(0), new VersionWrapper(1));

        for (VersionWrapper currentVersion : listOfInvalidVersions) {
            try {
                cnxClient.getModuleVersionEntry(moduleId, currentVersion);
                fail("should have failed.");
            } catch (CnxInvalidUrlException e) {
                // expected.
            }
        }
    }

    @Test
    public void testGetModuleVersion_0() throws Exception {
        List<ClientEntry> listOfEntryForUploadedResources = Lists.newArrayList();
        String resourceMappingXml =
                cnxClient.getResourceMappingFromResourceEntries(listOfEntryForUploadedResources);

        File cnxml = new File(MODULE_LOCATION + "/index_auto_generated.cnxml");
        String cnxmlAsString = Files.toString(cnxml, Charsets.UTF_8);

        ClientEntry moduleEntry = cnxClient.createNewModule();

        cnxClient.createNewModuleVersion(moduleEntry, cnxmlAsString, resourceMappingXml);

        IdWrapper moduleId = CnxAtomPubUtils.getIdFromAtomPubId(moduleEntry.getId());
        VersionWrapper version = new VersionWrapper(0);

        try {
            cnxClient.getModuleVersionEntry(moduleId, version);
            fail("should have failed.");
        } catch (CnxInvalidUrlException e) {
            // expected.
        }

        version = new VersionWrapper(1);
        try {
            ClientEntry entry = cnxClient.getModuleVersionEntry(moduleId, version);
            IdWrapper downloadedId = CnxAtomPubUtils.getIdFromAtomPubId(entry.getId());
            VersionWrapper downloadedVersion =
                    CnxAtomPubUtils.getVersionFromAtomPubId(entry.getId());

            assertEquals(moduleId, downloadedId);
            assertEquals(version, downloadedVersion);
        } catch (Exception e) {
            fail("should not have failed." + Throwables.getStackTraceAsString(e));
        }
    }

    @Test
    public void test_cretateNewResourceForMigration() throws Exception {
        // TODO(arjuns) : ensure that resource does not exist earlier.Current hack.
        try {
            IdWrapper id = new IdWrapper("m0010", IdWrapper.Type.MODULE);
            cnxClient.createNewModuleForMigration(id);
            cnxClient.createNewModuleForMigration(id);
            fail("should have failed.");
        } catch (CnxConflictException e) {
            // expected.
        }
    }
}

    /*
     * Test with new cnxClient.
     */
//    @Test
//    public void test_createNewModule() throws Exception {
//        ModuleWrapper module = client2.createNewModule();
//        doTestForCreateResource(module, false /* isMigaration */);
//    }

    // TODO(arjuns) : Uncomment this test once we have integrated repo with unittests.
    // @Test
    // public void test_createNewModuleForMigration() throws IllegalArgumentException, IOException,
    // JDOMException,
    // FeedException, URISyntaxException {
    // IdWrapper restrictedId = new IdWrapper("m0001", IdWrapper.Type.MODULE);
    // ModuleWrapper module = client2.createNewModuleForMigration(restrictedId);
    // assertEquals(restrictedId, module.getId());
    // doTestForCreateResource(module, true /* isMigaration */);
    // }
//
//    private void doTestForCreateResource(ModuleWrapper module, boolean isMigration)
//            throws Exception {
//        TestingUtils.validateAtomPubResource(module, isMigration, IdWrapper.Type.MODULE,
//                CnxAtomPubUtils.DEFAULT_VERSION);
//
//        List<File> listOfResourcesToUpload =
//                ParallelModuleMigrator.getListOfResourcesToBeUploaded(MODULE_LOCATION);
//
//        Builder<String, IdWrapper> mapPrettyNameToResourceIdBuilder =
//                new ImmutableMap.Builder<String, IdWrapper>();
//
//        for (File currFile : listOfResourcesToUpload) {
//            ResourceWrapper resourceWrapper = client2.createNewResource();
//            logger.info("Attempting to upload : " + currFile.getAbsolutePath());
//            client2.uploadResource(resourceWrapper.getUploadUri(), null/* mediaType */,
//                    null/* fileName */, currFile);
//            mapPrettyNameToResourceIdBuilder.put(currFile.getName(), resourceWrapper.getId());
//
//            logger.info("Successuflly uploaded [" + currFile.getName() + "] as resourceId["
//                    + resourceWrapper.getId() + "], and can be found here ["
//                    + resourceWrapper.getSelfUri() + "].");
//            // TODO(arjuns) : Add validations here.
//            break;
//        }
//
//        String resourceMappingXml =
//                CnxAtomPubUtils.getResourceMappingXmlFromResources(mapPrettyNameToResourceIdBuilder
//                        .build());
//
//        File cnxmlFile = new File(MODULE_LOCATION + "/index_auto_generated.cnxml");
//        String cnxml = Files.toString(cnxmlFile, Charsets.UTF_8);
//
//        client2.createNewModuleVersion(module.getId(), CnxAtomPubUtils.DEFAULT_EDIT_VERSION,
//                cnxml, resourceMappingXml);
//        // TODO(arjuns) : Add more validations here.
//        //
//        // // TODO(arjuns) : refactor this.
//        // IdWrapper moduleId = CnxAtomPubUtils.getIdFromAtomPubId(moduleEntry.getId());
//        // VersionWrapper version = CnxAtomPubUtils.getVersionFromAtomPubId(moduleEntry.getId());
//        // ClientEntry getEntry = cnxClient.getModuleVersionEntry(moduleId, version);
//        //
//        // String downloadedCnxmlDoc = cnxClient.getCnxml(getEntry);
//        // assertEquals(cnxmlAsString, downloadedCnxmlDoc);
//        //
//        // String downloadedResourceMappingDoc = cnxClient.getResourceMappingXml(getEntry);
//        // assertEquals(resourceMappingDocXml, downloadedResourceMappingDoc);
//        // // TODO(arjuns) : Add test for links.
//    }
//
//}
