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
package org.cnx.migrator.migrators;

import static com.google.common.base.Preconditions.checkArgument;
import static org.cnx.migrator.util.MigratorUtil.checkAtombuyEntryId;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.migrator.config.MigratorConfiguration;
import org.cnx.migrator.io.Directory;
import org.cnx.migrator.util.Log;
import org.cnx.migrator.util.MigratorUtil;
import org.cnx.repository.atompub.IdWrapper;

import com.google.common.collect.ImmutableList;
import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 * A migrator for a module item, including all of its versions
 * 
 * @author tal
 */
public class ModuleMigrator extends ItemMigrator {

    /** The root directory of the module to migrate. */
    private final Directory moduleDirectory;

    /** Module id in the repository. E.g. "m0012". */
    private final String cnxModuleId;

    /**
     * Construct a single module migrator.
     * 
     * The constructor should not do any significant amount of work. This is done later by the
     * {@link #doWork()} method.
     * 
     * @param config the configuration of this migration session.
     * @param cnxClient repository client to use.
     * @param moduleDirectory root data directory of the module to migrate. Its base name represent
     *            the numeric value of the module id (e.g. "000012").
     */
    public ModuleMigrator(MigratorConfiguration config, CnxAtomPubClient cnxClient,
            Directory moduleDirectory) {
        super(config, cnxClient);
        this.moduleDirectory = moduleDirectory;
        this.cnxModuleId = moduleDirectoryToId(moduleDirectory);
    }

    /** This is called by a worker thread to do the migration */
    @Override
    public void doWork() {

        ClientEntry atompubEntry = createCollection();

        // NOTE(tal): Version directories have sequential numeric names starting from 1 and
        // their lexicographic order preserves the numeric order using zero padding.
        final ImmutableList<Directory> versionDirectories = moduleDirectory.getSubDirectories();

        for (int versionNum = 1; versionNum <= versionDirectories.size(); versionNum++) {
            MigratorUtil.sleep(getConfig().getTransactionDelayMillis());

            final Directory versionDirectory = versionDirectories.get(versionNum - 1); // zero based
            final int directoryAsNum = Integer.parseInt(versionDirectory.getName());
            checkArgument(directoryAsNum == versionNum, "Version directory name mismatch: %s",
                    versionDirectory);
            // NOTE(tal): modifies atompubEntry to point to next version.
            migrateNextModuleVersion(atompubEntry, versionNum, versionDirectory);
        }
    }

    /** Map module data directory to CNX module id in the repository. */
    private static String moduleDirectoryToId(Directory moduleDirectory) {
        final String directoryName = moduleDirectory.getName(); // e.g. "0000012"
        final int directoryNumber = Integer.valueOf(directoryName); // e.g. 12
        // MOTE(tal): This matches the repository collection key to id mapping
        return String.format("m%04d", directoryNumber); // e.g. "m0012"
    }

    /**
     * Create the module entity in the repository. Module versions are migrated later.
     * 
     * @returns the atompub entry to use to upload the first module version.
     */
    private ClientEntry createCollection() {
        Log.message("***** Going to create module: %s", cnxModuleId);
        int attempt;
        for (attempt = 1;; attempt++) {
            final ClientEntry atompubEntry;
            IdWrapper cnxModuleIdWrapper = new IdWrapper(cnxModuleId, IdWrapper.Type.MODULE);
            try {
                atompubEntry = getCnxClient().createNewModuleForMigration(cnxModuleIdWrapper);
                checkAtombuyEntryId(cnxModuleId, 1, atompubEntry);
                message("Added module: %s", atompubEntry.getId());
                return atompubEntry;
            } catch (Exception e) {
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException(String.format("Failed after %d attempts: %s",
                            attempt, cnxModuleId), e);
                }
                Log.message("**** Attempt %d failed to write module %s. Will retry", attempt,
                        cnxModuleId);
                MigratorUtil.sleep(getConfig().getFailureDelayMillis());
            }
        }
    }

    /**
     * Migrate next version of this module.
     * 
     * The method assumes that the module has been created and that exactly all the version prior to
     * this version have already been migrated.
     * 
     * @param atompubEntry the atompub entry to use for posting this module version. Upon return,
     *            this entry is modified so it can be used to upload the next version of this
     *            module.
     * @param versionNum version number (1 based) of the next version to migrate.
     * @param versionDirectory root directory of this module version data.
     */
    private void migrateNextModuleVersion(ClientEntry atompubEntry, int versionNum,
            Directory versionDirectory) {

        int attempt;
        for (attempt = 1;; attempt++) {
            try {
                // TODO(tal): *** implement module version mapping from major.minor to number
                // in the xml file
                final String cnxml = versionDirectory.readXmlFile("cnxml.xml");

                // TODO(tal): upload resource map from property file
                getCnxClient().createNewModuleVersion(atompubEntry, cnxml, "Resource Map: TBD");
                checkAtombuyEntryId(cnxModuleId, versionNum, atompubEntry);
                // NOTE(tal): here atompubEntry.getEditURI points to a URL to post the next version.

                Log.message("Migrated module version %s/%s", cnxModuleId, versionNum);
                return;
            } catch (Exception e) {
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException(String.format("Failed after %d attempts: %s/%s",
                            attempt, cnxModuleId, versionNum), e);
                }
                Log.message("**** Attempt %d failed to write module version %s/%s. Will retry",
                        attempt, cnxModuleId, versionNum);
                MigratorUtil.sleep(getConfig().getFailureDelayMillis());
                // NOTE(tal): If got an exception, atompubEntry is guaranteed to not be changed.
            }
        }
    }
}
