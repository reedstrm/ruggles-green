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
import static org.junit.Assert.assertNotNull;

import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;
import java.io.File;
import java.net.MalformedURLException;
import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.atompubclient.CnxClientUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.repository.atompub.jerseyservlets.CnxAtomPubBasetest;
import org.cnx.repository.scripts.migrators.ParallelResourceMigrator;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ParallelResourceMigrator}
 * 
 * @author Arjun Satyapal
 */
public class ParallelResourceMigratorTest extends CnxAtomPubBasetest {
    private CnxAtomPubClient cnxClient;

    // TODO(arjuns) : Create file dynamically.
    private final File file = new File("/home/arjuns/test_file.txt");

    public ParallelResourceMigratorTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws MalformedURLException, ProponoException {
        cnxClient = new CnxAtomPubClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testResourceMigrator() {
        ParallelResourceMigrator resourceMigrator =
                new ParallelResourceMigrator(cnxClient, file.getAbsolutePath());

        ClientEntry createResourceEntry = resourceMigrator.migrateResource();
        createResourceEntry.getEditURI();
        assertNotNull(createResourceEntry);
        assertNotNull(createResourceEntry.getId());
        String resourceId = createResourceEntry.getId();
        String expectedResourceUrl =
                getConstants().getResourceAbsPath(
                        new IdWrapper(resourceId, IdWrapper.Type.RESOURCE)).toString();
        assertEquals(expectedResourceUrl, CnxClientUtils.getSelfUri(createResourceEntry)
                .getHrefResolved());
    }
}
