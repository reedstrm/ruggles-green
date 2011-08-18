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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpException;
import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.servlets.CnxAtomPubBasetest;
import org.cnx.repository.atompub.servlets.migrators.ModuleMigrator;
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
    private final String ORIGINAL_MODULE_ID = "m10057";
    private final String MODULE_LOCATION = "/home/arjuns/cnxmodules/col10064_1.12_complete/"
        + ORIGINAL_MODULE_ID;

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
        List<File> listOfResourcesToUpload =
            ModuleMigrator.getListOfResourcesToBeUploaded(MODULE_LOCATION);

        List<ClientEntry> listOfEntryForUploadedResources = Lists.newArrayList();
        for (File currFile : listOfResourcesToUpload) {
            logger.info("Attempting to upload : " + currFile.getAbsolutePath());
            ClientEntry resourceEntry =
                cnxClient.uploadFileToBlobStore(currFile.getName(), currFile);
            listOfEntryForUploadedResources.add(resourceEntry);
            logger.info("Successuflly uploaded [" + currFile.getName() + "] as resourceId["
                + resourceEntry.getId() + "], and can be found here [" + resourceEntry.getEditURI()
                + "].");
            // TODO(arjuns) : Add validations here.
        }

        String resourceMappingDocXml =
            cnxClient.getResourceMappingFromResourceEntries(listOfEntryForUploadedResources);

        File cnxml = new File(MODULE_LOCATION + "/index_auto_generated.cnxml");
        String cnxmlAsString = Files.toString(cnxml, Charsets.UTF_8);

        ClientEntry createModuleEntry = cnxClient.createNewModule();
        // TODO(arjuns): Add more validations here.

        ClientEntry moduleNewVersionEntry =
            cnxClient.createNewModuleVersion(createModuleEntry, cnxmlAsString,
                resourceMappingDocXml);
        // TODO(arjuns) : Add more validations here.

        // TODO(arjuns) : refactor this.
        String moduleId = CnxAtomPubConstants.getIdFromAtomPubId(moduleNewVersionEntry.getId());
        String version = CnxAtomPubConstants.getVersionFromAtomPubId(moduleNewVersionEntry.getId());
        Entry getEntry = cnxClient.getModuleVersionEntry(moduleId, version);

        logger.info("New location for [" + ORIGINAL_MODULE_ID + "] is "
            + moduleNewVersionEntry.getEditURI());

        Content content = (Content) getEntry.getContents().get(0);
        String encodedModuleEntry = content.getValue();
        String moduleEntryXml =
            cnxClient.getConstants().decodeFrom64BitEncodedString(encodedModuleEntry);

        String downloadedCnxmlDoc = cnxClient.getCnxml(getEntry);
        assertEquals(cnxmlAsString, downloadedCnxmlDoc);

        String downloadedResourceMappingDoc = cnxClient.getResourceMappingXml(getEntry);
        assertEquals(resourceMappingDocXml, downloadedResourceMappingDoc);
    }

    // TODO(arjuns) : Move this test to other file.
    @Test
    public void testModuleMigrator() throws IOException, ProponoException, JAXBException, JDOMException {
        File cnxml = new File(MODULE_LOCATION + "/index_auto_generated.cnxml");
        String cnxmlAsString = Files.toString(cnxml, Charsets.UTF_8);

        ModuleMigrator migrator = new ModuleMigrator(cnxClient);
        Entry moduleEntry = migrator.migrateModule(ORIGINAL_MODULE_ID, MODULE_LOCATION);
        assertNotNull(moduleEntry);
        cnxClient.getConstants();
        assertEquals("1", CnxAtomPubConstants.getVersionFromAtomPubId(moduleEntry.getId()));
    }
}
