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

import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.migrator.context.MigratorContext;
import org.cnx.migrator.io.Directory;
import org.cnx.migrator.util.Log;
import org.cnx.migrator.util.MigratorUtil;

import com.google.common.collect.ImmutableList;
import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 * A migrator for a collection item, including all of its versions
 * 
 * @author tal
 */
public class CollectionMigrator extends ItemMigrator {

    /** The root directory of the collection to migrate. */
    private final Directory collectionDirectory;

    /** Collection id in the repository. E.g. "col0012". */
    private final String cnxCollectionId;

    /**
     * Construct a single collection migrator.
     * 
     * The constructor should not do any significant amount of work. This is done later by the
     * {@link #doWork()} method.
     * 
     * @param context the context of this migration session.
     * @param collectionDirectory root data directory of the collection to migrate. Its base name
     *            represent the numeric value of the collection id (e.g. "000012").
     */
    public CollectionMigrator(MigratorContext context, Directory collectionDirectory) {
        super(context);
        this.collectionDirectory = collectionDirectory;
        this.cnxCollectionId = collectionDirectoryToId(collectionDirectory);
    }

    /** This is called by a worker thread to do the migration */
    @Override
    public void doWork() {

        ClientEntry atompubEntry = createCollection();

        int nextVersionNum = 1;

        // NOTE(tal): Version directories have sequential numeric names starting from 1 and
        // their lexicographic order preserves the numeric order using zero padding.
        final ImmutableList<Directory> versionDirectories = collectionDirectory.getSubDirectories();
        // TODO(tal): decide what we want to do with these collections, if any
        checkArgument(versionDirectories.size() > 0, "Collection has no versions: %s", collectionDirectory);

        for (Directory versionDirectory : versionDirectories) {
            final int directoryVersionNum = Integer.parseInt(versionDirectory.getName());
            checkArgument(directoryVersionNum >= nextVersionNum, "%s", versionDirectory);

            // If needed, create gap versions
            while (directoryVersionNum > nextVersionNum) {
                getContext().incrementCounter("COLLECTION_VERSION_TAKEDOWNS", 1);
                MigratorUtil.sleep(getConfig().getTransactionDelayMillis());
                // TODO(tal): create gaps as explicit taken down version.
                Log.message("** Creating gap collection version: %s/%s", cnxCollectionId, nextVersionNum);
                migrateNextCollectionVersion(atompubEntry, nextVersionNum, versionDirectory);
                nextVersionNum++;
            }

            // Create the actual version
            MigratorUtil.sleep(getConfig().getTransactionDelayMillis());
            migrateNextCollectionVersion(atompubEntry, nextVersionNum, versionDirectory);
            nextVersionNum++;
        }
    }

    /** Map collection data directory to CNX collection id in the repository. */
    private static String collectionDirectoryToId(Directory collectionDirectory) {
        final String directoryName = collectionDirectory.getName(); // e.g. "0000012"
        final int directoryNumber = Integer.valueOf(directoryName); // e.g. 12
        // MOTE(tal): This matches the repository collection key to id mapping
        return String.format("col%04d", directoryNumber); // e.g. "col0012"
    }

    /**
     * Create the collection entity in the repository. Collection versions are migrated later.
     * 
     * @returns the atompub entry to use to upload the first collection version.
     */
    private ClientEntry createCollection() {
        getContext().incrementCounter("COLLECTIONS", 1);
        Log.message("Going to migrate collection: %s", cnxCollectionId);
        int attempt;
        for (attempt = 1;; attempt++) {
            final ClientEntry atompubEntry;
            IdWrapper cnxCollectionIdWrapper =
                    new IdWrapper(cnxCollectionId, IdWrapper.Type.COLLECTION);
            try {
                atompubEntry =
                        getCnxClient().createNewCollectionForMigration(cnxCollectionIdWrapper);
                checkAtombuyEntryId(cnxCollectionId, 1, atompubEntry);
                message("Added collection: %s", atompubEntry.getId());
                return atompubEntry;
            } catch (Exception e) {
                if (attempt == 1) {
                    getContext().incrementCounter("COLLECTIONS_WITH_CREATION_RETRIES", 1);
                }
                getContext().incrementCounter("COLLECTION_CREATION_RETRIES", 1);
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException(String.format("Failed after %d attempts: %s",
                            attempt, cnxCollectionId), e);
                }
                Log.printStackTrace(e);
                Log.message("**** Attempt %d failed to write collection %s. Will retry", attempt,
                        cnxCollectionId);
                MigratorUtil.sleep(getConfig().getFailureDelayMillis());
            }
        }
    }

    /**
     * Migrate next version of this collection.
     * 
     * The method assumes that the collection has been created and that exactly all the version
     * prior to this version have already been migrated.
     * 
     * @param atompubEntry the atompub entry to use for posting this collection version. Upon
     *            return, this entry is modified so it can be used to upload the next version of
     *            this collection.
     * @param versionNum version number (1 based) of the next version to migrate.
     * @param versionDirectory root directory of this collection version data.
     */
    private void migrateNextCollectionVersion(ClientEntry atompubEntry, int versionNum,
            Directory versionDirectory) {
        getContext().incrementCounter("COLLECTION_VERSIONS", 1);
        int attempt;
        for (attempt = 1;; attempt++) {
            try {
                // TODO(tal): *** implement module version mapping from major.minor to number
                // in the xml file
                final String colxml = versionDirectory.readXmlFile("collection.xml");

                getCnxClient().createNewCollectionVersion(atompubEntry, colxml);
                checkAtombuyEntryId(cnxCollectionId, versionNum, atompubEntry);
                // NOTE(tal): here atompubEntry.getEditURI points to a URL to post the next version.

                Log.message("Migrated collection version %s/%s", cnxCollectionId, versionNum);
                return;
            } catch (Exception e) {
                if (attempt == 1) {
                    getContext().incrementCounter("COLLECTIONS_VERSIONS_WITH_UPLOAD_RETRIES", 1);
                }
                getContext().incrementCounter("COLLECTION_VERSTION_UPLOAD_RETRIES", 1);
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException(String.format("Failed after %d attempts: %s/%s",
                            attempt, cnxCollectionId, versionNum), e);
                }
                Log.message("**** Attempt %d failed to write collection version %s/%s. Will retry",
                        attempt, cnxCollectionId, versionNum);
                MigratorUtil.sleep(getConfig().getFailureDelayMillis());
                // NOTE(tal): If got an exception, atompubEntry is guaranteed to not be changed.
            }
        }
    }
}
