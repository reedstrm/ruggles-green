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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.common.exceptions.CnxPreconditionFailedException;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionVersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionWrapper;
import org.cnx.common.repository.atompub.objects.ModuleWrapper;

/**
 * Migrator for a Collection.
 * 
 * @author Arjun Satyapal
 */
public class ParallelCollectionMigrator implements Runnable {
    private Logger logger = Logger.getLogger(ParallelCollectionMigrator.class.getName());

    private Map<IdWrapper, IdWrapper> mapOfModuleIdToNewModuleEntry;
    private final CnxClient cnxClient;
    private CollectionWrapper collectionWrapper;
    private final String collectionLocation;
    private final VersionWrapper newVersion;
    private final boolean isMigration;
    private String collectionXml;

    private boolean success = false;

    public ParallelCollectionMigrator(CnxClient cnxClient, String collectionLocation,
            @Nullable CollectionWrapper collectionWrapper, @Nullable VersionWrapper newVersion,
            boolean isMigration) {
        this.cnxClient = checkNotNull(cnxClient);
        this.collectionLocation = checkNotNull(collectionLocation);
        this.collectionWrapper = collectionWrapper;
        this.newVersion = newVersion;

        if (isMigration || newVersion != null) {
            checkNotNull(collectionWrapper);
        }

        if (collectionWrapper != null) {
            checkNotNull(newVersion);
        }

        this.isMigration = isMigration;
        mapOfModuleIdToNewModuleEntry = Maps.newHashMap();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCollectionXml() {
        return collectionXml;
    }
    
    public CollectionWrapper getCollectionWrapper() {
        return collectionWrapper;
    }

    public void migrateCollection() throws IllegalArgumentException {
        try {
            List<File> listOfModulesToUpload = getListOfModulesToBeUploaded(collectionLocation);

            List<ParallelModuleMigrator> listOfModuleMigrators = Lists.newArrayList();
            List<Thread> listOfThreads = Lists.newArrayList();

            int counter = 1;
            for (File currModule : listOfModulesToUpload) {
                // TODO(arjuns) : Need to handle only specific exception. Else test will never die.
                IdWrapper moduleId = new IdWrapper(currModule.getName(), IdWrapper.Type.MODULE);
                ModuleWrapper moduleWrapper = null;

                if (isMigration && moduleWrapper == null) {
                    // TODO(arjuns) : Fix this hack. No way to create ModuleWrapper.
                    MigratorUtils.cleanUp(cnxClient, moduleId);
                    moduleWrapper = cnxClient.createModuleForMigration(moduleId);
                } else {
                    moduleWrapper = cnxClient.createModule();
                }

                mapOfModuleIdToNewModuleEntry.put(moduleId, moduleWrapper.getId());

                ParallelModuleMigrator moduleMigrator =
                        new ParallelModuleMigrator(cnxClient,
                                currModule.getAbsolutePath(), moduleWrapper,
                                CnxAtomPubUtils.LATEST_VERSION_WRAPPER,
                                isMigration);
                listOfModuleMigrators.add(moduleMigrator);

                Thread thread = new Thread(moduleMigrator);
                listOfThreads.add(thread);
                thread.start();

                int remainingModules = listOfModulesToUpload.size() - counter;
                counter++;
                logger.info("**** Remaining size = " + remainingModules);
            }

            for (Thread currThread : listOfThreads) {
                currThread.join();
            }

            for (ParallelModuleMigrator currModuleMigrtor : listOfModuleMigrators) {
                if (!currModuleMigrtor.isSuccess()) {
                    throw new RuntimeException("Failed to migrate module.");
                }
            }

            // Successfully uploaded all the modules. Now attempting to upload collection.xml

            // Updating references for modules to new moduleId.
            File collXml = new File(collectionLocation + "/collection.xml");
            collectionXml = Files.toString(collXml, Charsets.UTF_8);

            for (IdWrapper cnxModuleId : mapOfModuleIdToNewModuleEntry.keySet()) {
                IdWrapper newModuleEntry = mapOfModuleIdToNewModuleEntry.get(cnxModuleId);
                IdWrapper aerModuleId = CnxAtomPubUtils.getIdFromAtomPubId(newModuleEntry.getId());
                String oldString = "\"" + cnxModuleId + "\"";
                String newString = "\"" + aerModuleId.getId() + "\"";
                collectionXml = collectionXml.replaceAll(oldString, newString);
                logger.info("Old ModuleId = " + cnxModuleId + " New ModuleId = "
                        + aerModuleId.getId());
            }

            VersionWrapper publishVersion = null;
            if (collectionWrapper == null) {
                collectionWrapper = cnxClient.createCollection();
                publishVersion = CnxAtomPubUtils.DEFAULT_EDIT_VERSION;
            } else {
                if (newVersion != null) {
                    if (newVersion.equals(CnxAtomPubUtils.LATEST_VERSION_WRAPPER)) {
                        try {
                            CollectionVersionWrapper tempModuleWrapper =
                                    cnxClient.getCollectionVersion(collectionWrapper.getId(),
                                            newVersion);
                            publishVersion = tempModuleWrapper.getVersion().getNextVersion();
                        } catch (CnxPreconditionFailedException e) {
                            publishVersion = CnxAtomPubUtils.DEFAULT_EDIT_VERSION;
                        }
                    } else if (newVersion.getVersionInt() == 0) {
                        publishVersion = CnxAtomPubUtils.DEFAULT_EDIT_VERSION;
                    } else {
                        publishVersion = newVersion;
                    }
                } else {
                    throw new RuntimeException("Code should not reach here.");
                }
            }

            // TODO(arjuns) : Modify this to return collectionVersion.
            collectionWrapper =
                    cnxClient.createCollectionVersion(collectionWrapper.getId(), publishVersion,
                            collectionXml);
            success = true;

            logger.info("Successfully uploaded : "
                    + collectionWrapper.getId()
                    + " to : " + collectionWrapper.getSelfUri());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            // If control reaches here, that means there was some exception on server side.
            // Wait for some time and then retry.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                logger.severe(Throwables.getStackTraceAsString(e1));
            }
        }
    }

    // From a given collection, extract list of modules that need to be uploaded to Repository.
    // TODO(arjuns) : Repository needs to tell what modules are already uploaded.
    // otherwise it will end up in duplicating modules on repository.
    public static List<File> getListOfModulesToBeUploaded(String collectionLocation) {
        File folder = new File(collectionLocation);
        File[] listOfProbableModulesToUpload = folder.listFiles(new ModuleListFilter());

        List<File> listOfModules = Lists.newArrayList();
        for (File currModule : listOfProbableModulesToUpload) {
            if (currModule.isDirectory()) {
                listOfModules.add(currModule);
            }
        }

        return listOfModules;
    }

    public static class ModuleListFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String fileName) {
            if (fileName.startsWith("m")) {
                return true;
            }
            return false;
        }
    }

    @Override
    public void run() {
        migrateCollection();
    }
}
