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
package org.cnx.repository.atompub.servlets;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;

import org.cnx.repository.atompub.client.CnxAtomPubClient;
import org.cnx.repository.atompub.utils.CnxAtomPubConstants;
import org.junit.Before;
import org.junit.Test;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

/**
 * Test for {@link CnxAtomModuleServlet}
 *
 * @author Arjun Satyapal
 */
public class ModuleServletTest extends CnxAtomPubBaseTest {
    private CnxAtomPubClient cnxClient;

    // TODO(arjuns) : Create file dynamically.
    private final File file = new File("/home/arjuns/test_file.txt");

    public ModuleServletTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws MalformedURLException, ProponoException {
        cnxClient = new CnxAtomPubClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testModule() throws Exception {
        ClientEntry createResourceEntry = cnxClient.uploadFileToBlobStore(file);

        ClientEntry createModuleEntry = cnxClient.createNewModule();

        String cnxmlDoc = "CNXML Doc.";
        String resourceMappingDoc = "Resource Mapping Doc.";

        ClientEntry moduleNewVersionEntry =
            cnxClient.createNewModuleVersion(createModuleEntry, cnxmlDoc, resourceMappingDoc);

        cnxClient.getConstants();
        // TODO(arjuns) : refactor this.
        String moduleId = CnxAtomPubConstants.getIdFromAtomPubId(moduleNewVersionEntry.getId());
        String version = CnxAtomPubConstants.getVersionFromAtomPubId(moduleNewVersionEntry.getId());

        Entry getEntry = cnxClient.getModuleVersion(moduleId, version);

        // TODO(arjuns) : Fix this.
        String getCnxmlDoc =
            CnxAtomPubConstants.getCnxmlDocFromContent((Content) getEntry.getContents().get(0));
        String getResourceMappingDoc =
            CnxAtomPubConstants.getResourceMappingDocFromContent((Content) getEntry.getContents()
                .get(0));

        assertEquals(cnxmlDoc, getCnxmlDoc);
        assertEquals(resourceMappingDoc, getResourceMappingDoc);
    }
}
