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
package org.cnx.repository.scripts.migrators;

import com.google.common.base.Throwables;

import com.sun.syndication.propono.atom.client.ClientEntry;

import org.cnx.atompubclient.CnxAtomPubClient;

import java.io.File;
import java.util.logging.Logger;

/**
 * Migrator for a resource.
 * 
 * @author Arjun Satyapal
 */
public class ParallelResourceMigrator implements Runnable {
    private final Logger logger = Logger.getLogger(ParallelResourceMigrator.class.getName());
    private final CnxAtomPubClient cnxClient;
    private final String resourceLocation;
    private ClientEntry resourceEntry;
    private boolean success = false;

    public String getResourceLocation() {
        return resourceLocation;
    }

    public ClientEntry getResourceEntry() {
        return resourceEntry;
    }

    public boolean isSuccess() {
        return success;
    }

    public ParallelResourceMigrator(CnxAtomPubClient cnxClient, String resourceLocation) {
        this.cnxClient = cnxClient;
        this.resourceLocation = resourceLocation;
    }

    // TODO(arjuns) : Replace probably with InputStream.
    public ClientEntry migrateResource() {
        File file = new File(resourceLocation);

        for (int i = 0; i < 10; i++) {
            try {
                logger.info("Trying to upload : " + resourceLocation);
                resourceEntry = cnxClient.uploadFileToBlobStore(file.getName(), file);
                success = true;
                String resourceUrl = cnxClient.getLinkForResource(resourceEntry).getHrefResolved();
                logger.info("Successfully uploaded : " + resourceLocation + " to : " + resourceUrl);
                return resourceEntry;
            } catch (Exception e) {
                // TODO(arjuns): Auto-generated catch block
                logger.severe(Throwables.getStackTraceAsString(e));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.severe(Throwables.getStackTraceAsString(e));
            }
        }

        logger.severe("Failed to upload Resource after 10 attempts : " + resourceLocation);
        System.exit(1);
        return null;
    }

    @Override
    public void run() {
        migrateResource();
    }
}
