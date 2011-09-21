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
import org.cnx.migrator.config.MigratorConfiguration;
import org.cnx.migrator.io.DataRootDirectory;
import org.cnx.migrator.io.Directory;
import org.cnx.migrator.io.ShardedDirectory;
import org.cnx.migrator.migrators.CollectionMigrator;
import org.cnx.migrator.migrators.ItemMigrator;
import org.cnx.migrator.migrators.ModuleMigrator;
import org.cnx.migrator.migrators.ResourceMigrator;
import org.cnx.migrator.util.Log;
import org.cnx.migrator.util.MigratorUtil;
import org.cnx.migrator.workqueue.TimeRamp;
import org.cnx.migrator.workqueue.WorkQueue;
import org.cnx.migrator.workqueue.WorkQueueStats;

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

    private final WorkQueue workQueue;

    public Migrator(MigratorConfiguration config) {
        this.config = checkNotNull(config);

        Log.message("\nCONFIG:\n%s", config);
        // Pause scrolling to allow reading the message
        MigratorUtil.sleep(1000);

        try {
            final URL atomPubUrl = new URL(config.getRepositoryAtomPubUrl());
            this.cnxClient = new CnxAtomPubClient(atomPubUrl);
            this.root = new DataRootDirectory(new File(config.getDataRootDirectory()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Initialize work queue and workers. We setup the queue size arbitrarily to 500;
        this.workQueue = new WorkQueue(500, config.getMaxThreads());
    }

    /** Migrate all data subject to the migrator configuration */
    public void migrateAll() {
        final int rampUpTimeMillies = config.getRampUpTimeSecs() * 1000;

        if (config.isMigrateResources()) {
            workQueue.reset(new TimeRamp(config.getResourceMinThreads(), config
                    .getResourceThreads(), rampUpTimeMillies));
            migrateAllResources();
        }

        if (config.isMigrateModules()) {
            workQueue.reset(new TimeRamp(config.getModuleMinThreads(), config.getModuleThreads(),
                    rampUpTimeMillies));
            migrateAllModules();
        }

        if (config.isMigrateCollections()) {
            workQueue.reset(new TimeRamp(config.getCollectionMinThreads(), config
                    .getCollectionThreads(), rampUpTimeMillies));
            migrateAllCollections();
        }

        Log.message("\nCONFIG:\n%s", config);
    }

    /** Migrate all resources */
    public void migrateAllResources() {
        final ShardedDirectory resources = root.getResourcesRoot();
        Log.message("  Reosources directory: %s", resources);
        // Iterate resource shards
        for (Directory shard : resources.getShards()) {
            Log.message("    Shard directory: %s", shard);
            // Iterated resources in current shard
            for (Directory resourceDirectory : shard.getSubDirectories()) {
                final ItemMigrator migrator =
                        new ResourceMigrator(config, cnxClient, resourceDirectory);
                queueItemMigrator(migrator);
            }
        }
        waitForWorkQueueCompletion("Resources");
    }

    /** Migrate all collections */
    public void migrateAllCollections() {
        final ShardedDirectory collections = root.getCollectionsRoot();
        Log.message("  Collections directory: %s", collections);
        // Iterate collection shards
        for (Directory shard : collections.getShards()) {
            Log.message("    Shard directory: %s", shard);
            // Iterated collections in current shard
            for (Directory collectionDirectory : shard.getSubDirectories()) {
                final ItemMigrator migrator =
                        new CollectionMigrator(config, cnxClient, collectionDirectory);
                queueItemMigrator(migrator);
            }
        }
        waitForWorkQueueCompletion("Collections");
    }

    /** Migrate all modules */
    public void migrateAllModules() {
        final ShardedDirectory modules = root.getModulesRoot();
        Log.message("  Modules directory: %s", modules);
        // Iterate module shards
        for (Directory shard : modules.getShards()) {
            Log.message("    Shard directory: %s", shard);
            // Iterated modules in current shard
            for (Directory moduleDirectory : shard.getSubDirectories()) {
                final ItemMigrator migrator =
                        new ModuleMigrator(config, cnxClient, moduleDirectory);
                queueItemMigrator(migrator);
            }
        }
        waitForWorkQueueCompletion("Modules");
    }

    /** Add a work unit to the work queue */
    private void queueItemMigrator(ItemMigrator itemMigrator) {
        while (!workQueue.tryToAddItem(itemMigrator)) {
            Log.message("%s", workQueue.getStats());
            MigratorUtil.sleep(5000);
        }
    }

    /** Wait until all work unit in the work queue are completed */
    private void waitForWorkQueueCompletion(String messagePrefix) {
        for (;;) {
            WorkQueueStats stats = workQueue.getStats();
            if (stats.getNonCompletedItemCount() == 0) {
                break;
            }
            Log.message("%s: waiting for %s to complete", messagePrefix,
                    stats.getNonCompletedItemCount());
            MigratorUtil.sleep(2000);
        }
        Log.message("%s: %s", messagePrefix, workQueue.getStats());
    }
}
