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

import org.cnx.repository.atompub.jerseyservlets.TestingUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.bind.JAXBException;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.objects.ResourceWrapper;
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
    private CnxClient cnxClient;

    // TODO(arjuns) : Create file dynamically.
    private final File file = new File("/home/arjuns/test_file.txt");

    public ParallelResourceMigratorTest() throws Exception {
        super();
    }

    @Before
    public void initialize() throws IOException, URISyntaxException, JAXBException, CnxException {
        cnxClient = new CnxClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void testResourceMigrator_reservedId() throws Exception {
        IdWrapper resourceIdWrapper = new IdWrapper("r1234", IdWrapper.Type.RESOURCE);
        MigratorUtils.cleanUp(cnxClient, resourceIdWrapper);
        ResourceWrapper resourceWrapper = cnxClient.createResourceForMigration(resourceIdWrapper);

        ParallelResourceMigrator resourceMigrator =
                new ParallelResourceMigrator(cnxClient, file, resourceWrapper, true /* isMigration */);

        resourceMigrator.migrateResource();
        String expectedResourceUrl =
                getConstants().getResourceAbsPath(resourceWrapper.getId()).toString();
        assertEquals(expectedResourceUrl, resourceWrapper.getSelfUri().toString());
    }

    @Test
    public void testResourceMigrator_newId() throws Exception {
        ParallelResourceMigrator resourceMigrator =
                new ParallelResourceMigrator(cnxClient, file, null /* resourceWrapper */,
                        false /* isMigration */);

        ResourceWrapper resourceWrapper = resourceMigrator.migrateResource();
        assertNotNull(resourceWrapper);
        TestingUtils.validateAtomPubResource(resourceWrapper, false, IdWrapper.Type.RESOURCE, null);

        String expectedResourceUrl =
                getConstants().getResourceAbsPath(resourceWrapper.getId()).toString();
        assertEquals(expectedResourceUrl, resourceWrapper.getSelfUri().toString());
    }
}
