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
package org.cnx.migrator.workers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.migrator.MigratorConfiguration;
import org.cnx.migrator.io.Directory;
import org.cnx.migrator.queue.WorkQueue;
import org.cnx.migrator.util.MigratorUtil;

import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 * A migrator worker thread.
 * 
 * @author tal
 */
public class MigratorWorkerThread extends Thread {

    /** For debuging only */
    private final String workerId;

    /** The work queue that this worker serves. */
    private final WorkQueue<MigratorWorkUnit> workQueue;

    public MigratorWorkerThread(String workerId, WorkQueue<MigratorWorkUnit> workQueue) {
        this.workerId = checkNotNull(workerId);
        this.workQueue = checkNotNull(workQueue);

        // Not main thread.
        setDaemon(true);
    }

    @Override
    public void run() {
        for (;;) {
            MigratorWorkUnit workUnit = workQueue.getNextItemToWork();

            // Handle resource migration work items
            if (workUnit instanceof ResourceMigrationWorkUnit) {
                migrateOneResource((ResourceMigrationWorkUnit) workUnit);
                workQueue.itemCompleted(workUnit);
                continue;
            }

            // TODO(tal): add here handling of collections and modules

            throw new RuntimeException("Unknown work unit type: " + workUnit.getClass());
        }
    }

    /**
     * Handle a single resource migration work unit.
     */
    public void migrateOneResource(ResourceMigrationWorkUnit workUnit) {

        final Directory resourceDirectory = workUnit.getResourceDirectory();
        final CnxAtomPubClient cnxClient = workUnit.getCnxClient();
        final MigratorConfiguration config = workUnit.getConfig();

        // Read properties
        final Properties properties =
                resourceDirectory.readPropertiesFile("resource_properties.txt");

        if (config.isVerbose()) {
            message("      Resource directory: %s", resourceDirectory);

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                message("  [%s] -> [%s]", entry.getKey(), entry.getValue());
            }
        }

        // Upload resource file
        final File resourceFile = resourceDirectory.subFile("resource_data");
        for (int tryNumber = 1; tryNumber <= config.getMaxTries(); tryNumber++) {
            try {
                if (config.isVerbose() || tryNumber > 1) {
                    message("Attemp [%d] to upload resource: %s", tryNumber,
                            resourceFile.getAbsolutePath());
                }
                // TODO(tal): add forced resource id
                // TODO(tal): add forced context type
                final ClientEntry entry =
                        cnxClient.uploadFileToBlobStore(resourceFile.getName(), resourceFile);
                String resourceUrl = cnxClient.getLinkForResource(entry).getHrefResolved();
                if (config.isVerbose()) {
                    message("Successfully uploaded (try %d): %s to %s", tryNumber,
                            resourceFile.getAbsolutePath(), resourceUrl);
                } else {
                    message("Resource uploaded: %s", resourceDirectory.getDir().getName());
                }

                // TODO(tal): verify resource by comparing its size and md5
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
            // wait a little before next try
            MigratorUtil.sleep(1000);
        }

        throw new RuntimeException("Failed to upload resource file after " + config.getMaxTries()
                + " attempts: " + resourceFile.getAbsolutePath());
    }

    private void message(String format, Object... args) {
        System.out.println("[" + workerId + "]: " + String.format(format, args));
    }
}
