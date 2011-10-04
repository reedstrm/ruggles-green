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
package org.cnx.repository.scripts.programs;

import com.google.common.base.Preconditions;
import java.net.URL;
import java.util.logging.Logger;
import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.repository.scripts.migrators.ParallelCollectionMigrator;
import org.cnx.repository.scripts.migrators.ParallelModuleMigrator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Migrator for migrating Modules to CNX repository.
 * 
 * TODO(arjuns) : Add java flags.
 * 
 * @author Arjun Satyapal
 */
public class MigratorMain {
    private static Logger logger = Logger.getLogger(MigratorMain.class.getName());

    private CnxAtomPubClient cnxClient;

    @Option(name = "-migrateModule", usage = "do migration for module.")
    private boolean migrateModule;

    @Option(name = "-aerModuleId", usage = "ModuleId on AER.")
    private String aerModuleId;

    @Option(name = "-cnxModuleId", usage = "ModuleId on cnx.org.")
    private String cnxModuleId;

    @Option(name = "-migrateCollection", usage = "do migration for collection.")
    private boolean migrateCollection;

    @Option(name = "-aerCollectionId", usage = "CollectionId on AER.")
    private String aerCollectionId;

    @Option(name = "-cnxCollectionId", usage = "ModuleId on cnx.org.")
    private String cnxCollectionId;

    @Option(name = "-repositoryUrl", usage = "Repository URL")
    private String atomPubServiceUrl = "http://localhost:8888/atompub";

    @Option(name = "-localFolder", usage = "Location for module/collection")
    private String localFolder = null;

    @Option(name = "-preserveModuleIds", usage = "Preserve moduleIds for Collection.")
    private boolean preserveModuleIds = false;

    private final static String USAGE =
            "\n\t -migrateModule -[aerModuleId | cnxModuleId] <id> -localFolder <location> [-repositoryUrl <url>]"
                    + "\n\t-migrateCollection -[aerCollectionId | cnxCollectionId] <id> -localFolder <location> <-preserveModuleIds> [-repositoryUrl <url>]";

    public static void main(String[] args) throws Exception {

        StringBuilder builder = new StringBuilder();
        for (String currArg : args) {
            builder.append(currArg).append(" ");
        }

        logger.info("Args : " + builder.toString());

        new MigratorMain().doMain(args);
    }

    public void doMain(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);

            StringBuilder builder = new StringBuilder();
            builder.append("MigrateModule = ").append(migrateModule);

            if (migrateModule == migrateCollection) {
                printErrAndExit("Usage = " + USAGE);
            }

            Preconditions.checkNotNull(localFolder, "localfolder is null");
            Preconditions.checkNotNull(atomPubServiceUrl, "atomPubServiceUrl is null");

            URL atomPubUrl = new URL(atomPubServiceUrl);
            cnxClient = new CnxAtomPubClient(atomPubUrl);

            if (migrateModule) {
                migrateModule();
            } else if (migrateCollection) {
                migrateCollection();
            }

        } catch (CmdLineException e) {
            printErrAndExit("Usage = " + USAGE);
        }
        long endTime = System.currentTimeMillis();
        logger.info("Time to migrate = " + (endTime - startTime) / 1000);

    }

    private void migrateCollection() throws InterruptedException {
        IdWrapper cnxCollectionIdWrapper = null;
        IdWrapper aerCollectionIdWrapper = null;
        if (aerCollectionId != null) {
            Preconditions.checkArgument(cnxCollectionId == null);
            aerCollectionIdWrapper = new IdWrapper(aerCollectionId, IdWrapper.Type.COLLECTION);
        } else if (cnxCollectionId != null) {
            Preconditions.checkArgument(aerCollectionId == null);
            cnxCollectionIdWrapper = new IdWrapper(cnxCollectionId, IdWrapper.Type.COLLECTION);
        }

        ParallelCollectionMigrator migrator =
                new ParallelCollectionMigrator(cnxClient, localFolder, cnxCollectionIdWrapper,
                        aerCollectionIdWrapper, null /* version */, preserveModuleIds);

        Thread thread = new Thread(migrator);
        thread.start();
        thread.join();
        // TODO(arjuns) : Check for exit status for thread.
        // ClientEntry newclientEntry = migrator.getCollectionVersionEntry();
    }

    public void printErrAndExit(String errmMsg) {
        System.err.println(errmMsg);
        System.exit(1);
    }

    private void migrateModule() throws InterruptedException {
        IdWrapper aerModuleIdWrapper = null;
        IdWrapper cnxModuleIdWrapper = null;
        if (aerModuleId != null) {
            Preconditions.checkArgument(cnxModuleId == null);
            aerModuleIdWrapper = new IdWrapper(aerModuleId, IdWrapper.Type.MODULE);
        } else if (cnxModuleId != null) {
            Preconditions.checkArgument(aerModuleId == null);
            cnxModuleIdWrapper = new IdWrapper(cnxModuleId, IdWrapper.Type.MODULE);
        }

        ParallelModuleMigrator migrator =
                new ParallelModuleMigrator(cnxClient, localFolder, null /* collXmlModuleId */,
                        cnxModuleIdWrapper, aerModuleIdWrapper, null /* version */);

        Thread thread = new Thread(migrator);
        thread.start();
        thread.join();
        // TODO(arjuns) : Check for exit status for thread.
        // ClientEntry newclientEntry = migrator.getModuleVersionEntry();
    }

    public void validateCollectionDetails() {
        if (aerCollectionId != null) {
            Preconditions.checkArgument(cnxCollectionId == null);
        } else {
            Preconditions.checkArgument(cnxCollectionId != null);
        }
    }
}
