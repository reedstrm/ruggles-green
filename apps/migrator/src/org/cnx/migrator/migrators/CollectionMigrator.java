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
import static org.cnx.migrator.util.MigratorUtil.checkResourceId;

import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionWrapper;
import org.cnx.migrator.context.MigratorContext;
import org.cnx.migrator.io.Directory;
import org.cnx.migrator.util.Log;
import org.cnx.migrator.util.MigratorUtil;

import com.google.common.collect.ImmutableList;

/**
 * A migrator for a collection item, including all of its versions
 * 
 * @author tal
 */
public class CollectionMigrator extends ItemMigrator {

    /** The root directory of the collection to migrate. */
    private final Directory collectionDirectory;

    /** Collection id in the repository. E.g. "col0012". */
    private final String collectionId;

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
        this.collectionId = collectionDirectoryToId(collectionDirectory);
    }

    /** This is called by a worker thread to do the migration */
    @Override
    public void doWork() {

        createCollection(collectionId);

        int nextVersionNum = 1;

        // NOTE(tal): Version directories have sequential numeric names starting from 1 and
        // their lexicographic order preserves the numeric order using zero padding.
        final ImmutableList<Directory> versionDirectories = collectionDirectory.getSubDirectories();
        // TODO(tal): decide what we want to do with these collections, if any
        checkArgument(versionDirectories.size() > 0, "Collection has no versions: %s",
                collectionDirectory);

        for (Directory versionDirectory : versionDirectories) {
            final int directoryVersionNum = Integer.parseInt(versionDirectory.getName());
            checkArgument(directoryVersionNum >= nextVersionNum, "%s", versionDirectory);

            // Track version gap is needed.
            if (directoryVersionNum > nextVersionNum) {
                getContext().incrementCounter("COLLECTION_VERSION_GAPS", 1);
                getContext().incrementCounter("COLLECTION_VERSION_TAKEDOWNS",
                        (directoryVersionNum - nextVersionNum));
                nextVersionNum = directoryVersionNum;
            }

            // Create the actual version
            MigratorUtil.sleep(getConfig().getTransactionDelayMillis());
            migrateNextCollectionVersion(collectionId, nextVersionNum, versionDirectory);
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
     */
    private void createCollection(String collectionId) {
        getContext().incrementCounter("COLLECTIONS", 1);
        Log.message("Going to migrate collection: %s", collectionId);
        int attempt;
        for (attempt = 1;; attempt++) {
            IdWrapper cnxCollectionIdWrapper =
                    new IdWrapper(collectionId, IdWrapper.Type.COLLECTION);
            try {
                final CollectionWrapper collectionWrapper =
                        getCnxClient().createCollectionForMigration(cnxCollectionIdWrapper);
                checkResourceId(collectionId, 1, collectionWrapper);
                message("Added collection: %s", collectionId);
                return;
            } catch (Exception e) {
                if (attempt == 1) {
                    getContext().incrementCounter("COLLECTIONS_WITH_CREATION_RETRIES", 1);
                }
                getContext().incrementCounter("COLLECTION_CREATION_RETRIES", 1);
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException(String.format("Failed after %d attempts: %s",
                            attempt, collectionId), e);
                }
                Log.printStackTrace(e);
                Log.message("**** Attempt %d failed to write collection %s. Will retry", attempt,
                        collectionId);
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
     * TODO(tal): confirm and mention here that collectionWrapper is not modified.
     * 
     * @param collectionId the id of the collection to migrate
     * @param versionNum version number (1 based) of the next version to migrate.
     * @param versionDirectory root directory of this collection version data.
     */
    private void migrateNextCollectionVersion(String collectionId, int versionNum,
            Directory versionDirectory) {
        getContext().incrementCounter("COLLECTION_VERSIONS", 1);

        // TODO(tal): *** implement module version mapping from major.minor to number
        // in the xml file
        final String colxml = versionDirectory.readXmlFile("collection.xml");

        final IdWrapper idWrapper = new IdWrapper(collectionId, IdWrapper.Type.COLLECTION);
        final VersionWrapper versionWrapper = new VersionWrapper(versionNum);

        int attempt;
        for (attempt = 1;; attempt++) {
            try {
                final CollectionWrapper collectionWrapper =
                        getCnxClient().createCollectionVersionForMigration(idWrapper, versionWrapper, colxml);
                checkResourceId(collectionId, versionNum, collectionWrapper);
                Log.message("Added collection version %s/%s", collectionId, versionNum);
                return;
            } catch (Exception e) {
                if (attempt == 1) {
                    getContext().incrementCounter("COLLECTIONS_VERSIONS_WITH_UPLOAD_RETRIES", 1);
                }
                getContext().incrementCounter("COLLECTION_VERSTION_UPLOAD_RETRIES", 1);
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException(String.format("Failed after %d attempts: %s/%s",
                            attempt, collectionId, versionNum), e);
                }
                Log.message("**** Attempt %d failed to write collection version %s/%s. Will retry",
                        attempt, collectionId, versionNum);
                MigratorUtil.sleep(getConfig().getFailureDelayMillis());
                // NOTE(tal): If got an exception, atompubEntry is guaranteed to not be changed.
            }
        }
    }
}
