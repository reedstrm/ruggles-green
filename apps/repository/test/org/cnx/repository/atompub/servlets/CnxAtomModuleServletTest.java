/*
 * Copyright 2011 Google Inc.
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
package org.cnx.repository.atompub.servlets;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpException;
import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.repository.atompub.servlets.migrators.ModuleMigrator;
import org.cnx.repository.atompub.servlets.migrators.ResourceMigrator;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

/**
 * Test for {@link CnxAtomModuleServlet}
 *
 * @author Arjun Satyapal
 */
public class CnxAtomModuleServletTest extends CnxAtomPubBasetest {
    Logger logger = Logger.getLogger(CnxAtomModuleServletTest.class.getName());
    private CnxAtomPubClient cnxClient;
    private final String ORIGINAL_MODULE_ID = "m10085";
    private final String COLLECTION_LOCATION = "/home/arjuns/cnxmodules/col10064_1.12_complete/";

    public CnxAtomModuleServletTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws MalformedURLException, ProponoException {
        cnxClient = new CnxAtomPubClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testMigrateCompleteModule() throws HttpException, ProponoException, IOException,
            JAXBException, JDOMException {
        String tempLocation = "/home/arjuns/mymodule";
        List<File> listOfResourcesToUpload =
            ModuleMigrator.getListOfResourcesToBeUploaded(tempLocation);

        List<ClientEntry> listOfEntryForUploadedResources = Lists.newArrayList();
        for (File currFile : listOfResourcesToUpload) {
            logger.info("Attempting to upload : " + currFile.getAbsolutePath());
            ClientEntry resourceEntry =
                cnxClient.uploadFileToBlobStore(ResourceMigrator
                    .getResourceNameForResourceMappingDoc(currFile.getName()), currFile);
            listOfEntryForUploadedResources.add(resourceEntry);
            logger.info("Successuflly uploaded [" + currFile.getName() + "] as resourceId["
                + resourceEntry.getId() + "], and can be found here [" + resourceEntry.getEditURI()
                + "].");
            // TODO(arjuns) : Add validations here.
        }

        String resourceMappingDocXml =
            cnxClient.getResourceMappingFromResourceEntries(listOfEntryForUploadedResources);

        File cnxml = new File(tempLocation + "/index_auto_generated.cnxml");
        String cnxmlAsString = Files.toString(cnxml, Charsets.UTF_8);

        ClientEntry createModuleEntry = cnxClient.createNewModule();
        // TODO(arjuns): Add more validations here.

        ClientEntry moduleNewVersionEntry =
            cnxClient.createNewModuleVersion(createModuleEntry, cnxmlAsString,
                resourceMappingDocXml);
        // TODO(arjuns) : Add more validations here.

        // TODO(arjuns) : refactor this.
        String moduleId = CnxAtomPubConstants.getIdFromAtomPubId(moduleNewVersionEntry.getId());
        VersionWrapper version = CnxAtomPubConstants.getVersionFromAtomPubId(moduleNewVersionEntry.getId());
        ClientEntry getEntry = cnxClient.getModuleVersionEntry(moduleId, version);

        logger.info("New location for [" + ORIGINAL_MODULE_ID + "] is \n"
            + moduleNewVersionEntry.getEditURI());

        Content content = (Content) getEntry.getContents().get(0);
        String encodedModuleEntry = content.getValue();
        String moduleEntryXml =
            cnxClient.getConstants().decodeFrom64BitEncodedString(encodedModuleEntry);

        String downloadedCnxmlDoc = cnxClient.getCnxml(getEntry);
        assertEquals(cnxmlAsString, downloadedCnxmlDoc);

        String downloadedResourceMappingDoc = cnxClient.getResourceMappingXml(getEntry);
        assertEquals(resourceMappingDocXml, downloadedResourceMappingDoc);
        // TODO(arjuns) : Add test for links.
    }

//    // TODO(arjuns) : Move this test to other file.
    @Test
    public void testModuleMigrator() throws Exception {
        File cnxml = new File(COLLECTION_LOCATION + ORIGINAL_MODULE_ID + "/index_auto_generated.cnxml");
        String cnxmlAsString = Files.toString(cnxml, Charsets.UTF_8);

        ModuleMigrator migrator = new ModuleMigrator(cnxClient);
        Entry moduleEntry = migrator.migrateModule(ORIGINAL_MODULE_ID, COLLECTION_LOCATION + ORIGINAL_MODULE_ID);
        assertNotNull(moduleEntry);

        VersionWrapper expectedVersion = new VersionWrapper(1);
        assertEquals(expectedVersion, CnxAtomPubConstants.getVersionFromAtomPubId(moduleEntry.getId()));
    }

    @Test
    public void testMultipleModules() throws Exception {
        // Create first version.
        String moduleLocation = COLLECTION_LOCATION + ORIGINAL_MODULE_ID;
        File cnxml = new File(moduleLocation + "/index_auto_generated.cnxml");
        String cnxmlAsString = Files.toString(cnxml, Charsets.UTF_8);

        ModuleMigrator migrator = new ModuleMigrator(cnxClient);
        Entry moduleEntry = migrator.migrateModule(ORIGINAL_MODULE_ID, moduleLocation);

        String newModuleId = CnxAtomPubConstants.getIdFromAtomPubId(moduleEntry.getId());
        VersionWrapper version = CnxAtomPubConstants.NEW_MODULE_DEFAULT_VERSION;

        // Now publishing second version.

        migrator.migrateVersion(newModuleId, version, moduleLocation);

        URL moduleLatestUrl =
            cnxClient.getConstants().getModuleVersionAbsPath(newModuleId,
                new VersionWrapper(CnxAtomPubConstants.LATEST_VERSION_STRING));
        ClientEntry clientEntry = cnxClient.getService().getEntry(moduleLatestUrl.toString());
        VersionWrapper expectedVersion = new VersionWrapper(2);
        VersionWrapper actualVersion = CnxAtomPubConstants.getVersionFromAtomPubId(clientEntry.getId());

        logger.info("New location : " + moduleLatestUrl);
        assertEquals(expectedVersion, actualVersion);
    }

    // TODO(arjuns) : Move this to some other place.
    @Test
    public void testUpdateRichsModules() throws Exception {
        String localFileSystemModuleId = "m2102/";
        String moduleLocalLocation = COLLECTION_LOCATION + localFileSystemModuleId;

        String cnxRepoModuleId = "m34287";
        URL moduleLatestUrl =
                cnxClient.getConstants().getModuleVersionAbsPath(cnxRepoModuleId,
                    new VersionWrapper(CnxAtomPubConstants.LATEST_VERSION_STRING));
        ClientEntry clientEntry = cnxClient.getService().getEntry(moduleLatestUrl.toString());
        VersionWrapper currentVersion = CnxAtomPubConstants.getVersionFromAtomPubId(clientEntry.getId());
        System.out.println("Current Version = " + currentVersion);
        ModuleMigrator migrator = new ModuleMigrator(cnxClient);
        migrator.migrateVersion(cnxRepoModuleId, currentVersion, moduleLocalLocation);
    }
}
