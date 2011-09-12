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
package org.cnx.migrator;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.net.URL;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.migrator.io.DataRootDirectory;
import org.cnx.migrator.io.Directory;
import org.cnx.migrator.io.DirectoryShard;
import org.cnx.migrator.io.ShardedDirectory;
import org.cnx.migrator.queue.WorkQueue;
import org.cnx.migrator.queue.WorkQueueStats;
import org.cnx.migrator.util.MigratorUtil;
import org.cnx.migrator.workers.MigratorWorkUnit;
import org.cnx.migrator.workers.MigratorWorkerThread;
import org.cnx.migrator.workers.ResourceMigrationWorkUnit;

/**
 * CNX data migrator to new app engine repository.
 * 
 * The migrator copied data exported from the old CNX repository to a directory structure and
 * uploads it to a new repository. The new repository is assumed to be empty upon start.
 * 
 * @author tal
 */
public class Migrator {
    /** Configuration that controls the operation of the migrator */
    private final MigratorConfiguration config;

    /** Root directory of input data to migrate */
    private final DataRootDirectory root;

    /** Client for accessing the new repository via its atompub API. Thread safe. */
    private final CnxAtomPubClient cnxClient;

    private final WorkQueue<MigratorWorkUnit> workQueue;

    public Migrator(MigratorConfiguration config) {
        this.config = checkNotNull(config);

        System.out.println("\nCONFIG:\n" + config);
        MigratorUtil.sleep(1000);

        try {
            // TODO(tal): configure from command line
            URL atomPubUrl = new URL(config.getRepositoryAtomPubUrl());
            this.cnxClient = new CnxAtomPubClient(atomPubUrl);
            // TODO(tal): configure from command line
            this.root = new DataRootDirectory(new File(config.getDataRootDirectory()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Initialize work queue and workers
        this.workQueue = new WorkQueue<MigratorWorkUnit>(1000);
        for (int i = 0; i < config.getWorkerThreadCount(); i++) {
            new MigratorWorkerThread(String.format("Worker %03d", i), workQueue).start();
        }
    }

    /** Migrate all data, subject to the migrator configuration */
    public void migrateAll() {
        migrateAllResources();

        // TODO(tal): add here also migration of collections and modules.
    }

    /** Migrate all resources */
    public void migrateAllResources() {
        final ShardedDirectory resources = root.getResourcesRoot();
        System.out.printf("  Reosources directory: %s\n", resources);
        // Iterate resource shards
        for (DirectoryShard shard : resources.getShards()) {
            System.out.printf("    Shard directory: %s\n", shard);
            // Iterated resources in current shard
            for (Directory resourceDirectory : shard.getSubDirectories()) {
                final ResourceMigrationWorkUnit workUnit =
                        new ResourceMigrationWorkUnit(config, cnxClient, resourceDirectory);
                addWorkUnit(workUnit);
            }
        }
        waitForCompletion("Resources");
    }

    /** Add a work unit to the work queue */
    private void addWorkUnit(MigratorWorkUnit workUnit) {
        while (!workQueue.tryToAddItem(workUnit)) {
            System.out.println(workQueue.getStats());
            MigratorUtil.sleep(5000);
        }
    }

    /** Wait until all work unit in the work queue are completed */
    private void waitForCompletion(String messagePrefix) {
        for (;;) {
            WorkQueueStats stats = workQueue.getStats();
            if (stats.getNonCompletedItemCount() == 0) {
                break;
            }
            System.out.println(messagePrefix + ": Waiting for " + stats.getNonCompletedItemCount()
                    + " to complete");
            MigratorUtil.sleep(2000);
        }
        System.out.println(messagePrefix + ": " + workQueue.getStats());
    }
}
