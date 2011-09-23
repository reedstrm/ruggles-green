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
package org.cnx.migrator.context;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Contains the configuration parameters of the migration session.
 * <p>
 * Thread safe.
 * 
 * @author tal
 */
public class MigratorConfiguration {

    private static final int MIN_SHARD = 0;
    private static final int MAX_SHARD = 999;

    // e.g. "http://localhost:8888/atompub"
    // e.g. http://qa-cnx-repo.appspot.com/atompub
    @Option(name = "-repository_atompub_url", usage = "Repository atompub root URL")
    private String repositoryAtomPubUrl;

    // e.g. "/usr/local/cnx/data";
    @Option(name = "-data_root_dir", usage = "Input data root directory")
    private String dataRootDirectory;

    @Option(name = "-max_attempts", usage = "Max number of item upload attempt")
    private int maxAttempts = 10;

    // TODO(tal): setup the java formatter to wrap these lines correctly
    @Option(name = "-failure_delay_ms", usage = "Millis delay before retrying a failing attemp")
    private int failureDelayMillis = 10;

    @Option(name = "-transaction_delay_ms", usage = "Millis delay between submission to the same transaction lock")
    private int transactionDelayMillis = 1000;

    @Option(name = "-ramp_up_time_secs", usage = "Time in seconds to ramp up the upload throughput of each stage")
    private int rampUpTimeSecs = 10 * 60;

    @Option(name = "-migrate_all", usage = "Migrate all data (modules, collections, etc)")
    private boolean migrateAll = false;

    @Option(name = "-migrate_resources", usage = "Migrate resources")
    private boolean migrateResources = false;

    @Option(name = "-migrate_modules", usage = "Migrate modules")
    private boolean migrateModules = false;

    @Option(name = "-migrate_collections", usage = "Migrate collections")
    private boolean migrateCollections = false;

    @Option(name = "-resource_threads", usage = "Number of threads to use to migrate resources")
    private int resourceThreads = 500;

    @Option(name = "-module_threads", usage = "Number of threads to use to migrate modules")
    private int moduleThreads = 500;

    @Option(name = "-collection_threads", usage = "Number of threads to use to migrate collections")
    private int collectionThreads = 500;

    @Option(name = "-min_shard", usage = "Min shard number to migrate.")
    private int minShardToMigrate = 0;

    @Option(name = "-max_shard", usage = "Max shard number to migrate.")
    private int maxShardToMigrate = 999;

    public MigratorConfiguration(String args[]) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            // TODO(tal): provide a more graceful error message and usage text.
            parser.parseArgument(args);
            checkArgument(dataRootDirectory != null,
                    "Missing required command line arg: -data_root_dir");
            checkArgument(repositoryAtomPubUrl != null,
                    "Missing required command line arg: -repository_atompub_url");
            checkArgument(maxAttempts > 0, "-max_tries should be at least 1");
            // TODO(tal): add sanity checks for the rest of the args
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public String getRepositoryAtomPubUrl() {
        // return "http://localhost:8888/atompub";
        return repositoryAtomPubUrl;
    }

    public String getDataRootDirectory() {
        return dataRootDirectory;
    }

    public int getFailureDelayMillis() {
        return failureDelayMillis;
    }

    public int getTransactionDelayMillis() {
        return transactionDelayMillis;
    }

    public int getRampUpTimeSecs() {
        return rampUpTimeSecs;
    }

    protected void setRampUpTimeSecs(int rampUpTimeSecs) {
        this.rampUpTimeSecs = rampUpTimeSecs;
    }

    public boolean isMigrateResources() {
        return migrateAll || migrateResources;
    }

    public boolean isMigrateModules() {
        return migrateAll || migrateModules;
    }

    public boolean isMigrateCollections() {
        return migrateAll || migrateCollections;
    }

    public int getResourceThreads() {
        checkState(isMigrateResources(), "No need to migrate resources");
        return resourceThreads;
    }

    public int getResourceMinThreads() {
        checkState(isMigrateResources(), "No need to migrate resources");
        return Math.min(5, resourceThreads);
    }

    public int getModuleThreads() {
        checkState(isMigrateModules(), "No need to migrate modules");
        return moduleThreads;
    }

    public int getModuleMinThreads() {
        checkState(isMigrateModules(), "No need to migrate modules");
        return Math.min(5, moduleThreads);
    }

    public int getCollectionThreads() {
        checkState(isMigrateCollections(), "No need to migrate collections");
        return collectionThreads;
    }

    public int getCollectionMinThreads() {
        checkState(isMigrateCollections(), "No need to migrate collections");
        return Math.min(5, collectionThreads);
    }

    /** Get an upper bound of threads needed */
    public int getMaxThreads() {
        int maxThreads = 1;

        if (isMigrateResources() && maxThreads < resourceThreads) {
            maxThreads = resourceThreads;
        }

        if (isMigrateModules() && maxThreads < moduleThreads) {
            maxThreads = moduleThreads;
        }

        if (isMigrateCollections() && maxThreads < collectionThreads) {
            maxThreads = collectionThreads;
        }
        return maxThreads;
    }

    public int getMinShardToMigrate() {
        return minShardToMigrate;
    }

    public int getMaxShardToMigrate() {
        return maxShardToMigrate;
    }

    /** Test if the configurtion specifies migration of all shards. Used to generate warning. */
    public boolean isMigratingAllShards() {
        return (minShardToMigrate == MIN_SHARD) && (maxShardToMigrate == MAX_SHARD);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("  data_root_dir ............ [%s]\n", dataRootDirectory));
        sb.append(String.format("  repository_atompub_url ... [%s]\n", repositoryAtomPubUrl));
        sb.append('\n');
        sb.append(String.format("  migrate_all .............. [%s]\n", migrateAll));
        sb.append(String.format("  migrate_resources ........ [%s]\n", migrateResources));
        sb.append(String.format("  migrate_modules .......... [%s]\n", migrateModules));
        sb.append(String.format("  migrate_collections ...... [%s]\n", migrateCollections));
        sb.append('\n');
        if (isMigrateResources()) {
            sb.append(String.format("  resource_threads ......... [%s]\n", resourceThreads));
            sb.append(String.format("  resource_min_threads ..... [%s]\n", getResourceMinThreads()));
        }
        if (isMigrateModules()) {
            sb.append(String.format("  module_threads ........... [%s]\n", moduleThreads));
            sb.append(String.format("  module_min_threads ....... [%s]\n", getModuleMinThreads()));
        }
        if (isMigrateCollections()) {
            sb.append(String.format("  collection_threads ....... [%s]\n", collectionThreads));
            sb.append(String.format("  collection_min_threads ... [%s]\n", getCollectionMinThreads()));
        }
        sb.append(String.format("  max_threads .............. [%s]\n", getMaxThreads()));
        sb.append('\n');

        sb.append(String.format("  min_shard ................ [%d]%s\n", minShardToMigrate, (minShardToMigrate == MIN_SHARD) ? "" : " (Partial!)"));
        sb.append(String.format("  max_shard ................ [%s]%s\n", maxShardToMigrate, (maxShardToMigrate == MAX_SHARD) ? "" : " (Partial!)"));
        sb.append('\n');
        sb.append(String.format("  ramp_up_time_secs ........ [%d]\n", rampUpTimeSecs));
        sb.append(String.format("  max_attempts ............. [%s]\n", maxAttempts));
        sb.append(String.format("  transaction_delay_ms...... [%s]\n", transactionDelayMillis));
        sb.append(String.format("  failure_delay_ms.......... [%s]\n", failureDelayMillis));
        return sb.toString();
    }
}
