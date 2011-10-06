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

import java.io.File;
import java.net.URL;

import org.cnx.atompubclient2.CnxClient;
import org.cnx.migrator.context.MigratorConfiguration;
import org.cnx.migrator.context.MigratorContext;
import org.cnx.migrator.io.DataRootDirectory;
import org.cnx.migrator.io.Directory;
import org.cnx.migrator.migrators.CollectionMigrator;
import org.cnx.migrator.migrators.ItemMigrator;
import org.cnx.migrator.migrators.ModuleMigrator;
import org.cnx.migrator.migrators.ResourceMigrator;
import org.cnx.migrator.util.Log;
import org.cnx.migrator.util.MigratorUtil;
import org.cnx.migrator.util.Timer;
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
    /** Context that is used across the migration session. */
    private final MigratorContext context;

    /** Root directory of input data to migrate. */
    private final DataRootDirectory root;

    /** Migration work units to process */
    private final WorkQueue workQueue;

    public Migrator(MigratorConfiguration config) {
        Log.message("\nCONFIG:\n%s", config);
        // Pause scrolling to allow reading the message
        MigratorUtil.sleep(2000);

        try {
            final URL atomPubUrl = new URL(config.getRepositoryAtomPubUrl());
            final CnxClient cnxClient = new CnxClient(atomPubUrl);
            this.context = new MigratorContext(config, cnxClient);
            this.root = new DataRootDirectory(new File(config.getDataRootDirectory()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Initialize work queue and workers. We setup the queue size arbitrarily to 500;
        this.workQueue = new WorkQueue(500, config.getMaxThreads());
    }

    /** Migrate all data subject to the migrator configuration */
    public void migrateAll() {
        final Timer timer = new Timer();
        final MigratorConfiguration config = context.getConfig();
        final int rampUpTimeMillies = config.getRampUpTimeSecs() * 1000;

        if (config.isMigrateResources()) {
            workQueue.reset(new TimeRamp(config.getResourceMinThreads(), config
                    .getResourceThreads(), rampUpTimeMillies));
            migrateAllResources();
        } else {
            context.addSummaryMessage("Resource migration not requested. SKIPPING");
        }

        if (config.isMigrateModules()) {
            workQueue.reset(new TimeRamp(config.getModuleMinThreads(), config.getModuleThreads(),
                    rampUpTimeMillies));
            migrateAllModules();
        } else {
            context.addSummaryMessage("Modules migration not requested. SKIPPING");
        }

        if (config.isMigrateCollections()) {
            workQueue.reset(new TimeRamp(config.getCollectionMinThreads(), config
                    .getCollectionThreads(), rampUpTimeMillies));
            migrateAllCollections();
        } else {
            context.addSummaryMessage("Collections migration not requested. SKIPPING");
        }

        context.addSummaryMessage("Migration completed in %s", timer);

        Log.message("\n%s", context);
    }

    /** Migrate all resources */
    public void migrateAllResources() {
        final Timer timer = new Timer();
        int resourceCount = 0;
        final MigratorConfiguration config = context.getConfig();
        final Directory resourcesDirectory = root.getResourcesRoot();
        Log.message("Reosources root directory: %s", resourcesDirectory);
        // Iterate resource shards
        for (Directory shard : resourcesDirectory.getSubDirectories()) {
            if (!config.getShardFilterPattern().matcher(shard.getName()).matches()) {
                Log.message("Resources shard excluded by shard filter: %s", shard);
                context.incrementCounter("RESOURCE_SHARDS_IGNORED", 1);
                continue;
            }
            Log.message("Processing resources in shard: %s", shard);
            context.incrementCounter("RESOURCE_SHARDS_PROCESSED", 1);
            // Iterated resources in current shard
            for (Directory resourceDirectory : shard.getSubDirectories()) {
                final ItemMigrator migrator =
                        new ResourceMigrator(context, resourceDirectory);
                queueItemMigrator(migrator);
                resourceCount++;
            }
        }
        waitForWorkQueueCompletion("Resources");
        context.addSummaryMessage("Migrated %d resources in %s", resourceCount, timer);
    }

    /** Migrate all modules */
    public void migrateAllModules() {
        final Timer timer = new Timer();
        int moduleCount = 0;
        final MigratorConfiguration config = context.getConfig();
        final Directory modulesDirectory = root.getModulesRoot();
        Log.message("Modules root directory: %s", modulesDirectory);
        // Iterate module shards
        for (Directory shard : modulesDirectory.getSubDirectories()) {
            if (!config.getShardFilterPattern().matcher(shard.getName()).matches()) {
                Log.message("Module shard excluded by shard filter: %s", shard);
                context.incrementCounter("SHARD_SHARDS_IGNORED", 1);
                continue;
            }
            Log.message("Processing modules in shard: %s", shard);
            context.incrementCounter("MODULE_SHARDS_PROCESSED", 1);
            // Iterated modules in current shard
            for (Directory moduleDirectory : shard.getSubDirectories()) {
                final ItemMigrator migrator =
                        new ModuleMigrator(context, moduleDirectory);
                queueItemMigrator(migrator);
                moduleCount++;
            }
        }
        waitForWorkQueueCompletion("Modules");
        context.addSummaryMessage("Migrated %d modules in %s", moduleCount, timer);
    }

    /** Migrate all collections */
    public void migrateAllCollections() {
        final Timer timer = new Timer();
        int collectionCount = 0;
        final MigratorConfiguration config = context.getConfig();
        final Directory collectionsDirectory = root.getCollectionsRoot();
        Log.message("Collections root directory: %s", collectionsDirectory);
        // Iterate collection shards
        for (Directory shard : collectionsDirectory.getSubDirectories()) {
            if (!config.getShardFilterPattern().matcher(shard.getName()).matches()) {
                Log.message("Collection shard excluded by shard filter: %s", shard);
                context.incrementCounter("COLLECTION_SHARDS_IGNORED", 1);
                continue;
            }
            Log.message("Processing collections in shard: %s", shard);
            context.incrementCounter("COLLECTION_SHARDS_PROCESSED", 1);
            // Iterated collections in current shard
            for (Directory collectionDirectory : shard.getSubDirectories()) {
                final ItemMigrator migrator =
                        new CollectionMigrator(context, collectionDirectory);
                queueItemMigrator(migrator);
                collectionCount++;
            }
        }
        waitForWorkQueueCompletion("Collections");
        context.addSummaryMessage("Migrated %d collection in %s", collectionCount, timer);
    }

    /** Add a work unit to the work queue */
    private void queueItemMigrator(ItemMigrator itemMigrator) {
        while (!workQueue.tryToAddItem(itemMigrator)) {
            Log.message("Work queue is full, waiting...");
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
