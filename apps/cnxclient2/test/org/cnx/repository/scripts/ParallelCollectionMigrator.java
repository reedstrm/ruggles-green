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
package org.cnx.repository.scripts;

import org.cnx.atompubclient2.CnxClient;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionVersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionWrapper;

/**
 * Migrator for a Collection.
 * 
 * @author Arjun Satyapal
 */
public class ParallelCollectionMigrator implements Runnable {
    private Logger logger = Logger.getLogger(ParallelCollectionMigrator.class.getName());

    private Map<IdWrapper, IdWrapper> mapOfModuleIdToNewModuleEntry;
    private final CnxClient cnxClient;
    private final IdWrapper cnxCollectionId;
    private final String collectionLocation;
    private final VersionWrapper currentVersion;
    private final boolean isMigration;
    private CollectionWrapper responseCollectionWrapper;
    private String collectionXml;

    private boolean success = false;

    public ParallelCollectionMigrator(CnxClient cnxClient, String collectionLocation,
            IdWrapper cnxCollectionId, VersionWrapper currentVersion,
            boolean isMigration) {
        this.cnxClient = cnxClient;
        this.collectionLocation = collectionLocation;
        this.cnxCollectionId = cnxCollectionId;
        this.currentVersion = currentVersion;
        this.isMigration = isMigration;
        mapOfModuleIdToNewModuleEntry = Maps.newHashMap();
    }

    public boolean isSuccess() {
        return success;
    }
    
    public String getCollectionXml() {
        return collectionXml;
    }

    public CollectionWrapper migrateCollection() throws IllegalArgumentException {
        try {
        List<File> listOfModulesToUpload = getListOfModulesToBeUploaded(collectionLocation);

        List<ParallelModuleMigrator> listOfModuleMigrators = Lists.newArrayList();
        List<Thread> listOfThreads = Lists.newArrayList();

        int counter = 1;
        for (File currModule : listOfModulesToUpload) {
            // TODO(arjuns) : Need to handle only specific exception. Else test will never die.
            IdWrapper moduleId = new IdWrapper(currModule.getName(), IdWrapper.Type.MODULE);

            boolean isMigration = moduleId.isIdUnderForcedRange();

            ParallelModuleMigrator moduleMigrator = new ParallelModuleMigrator(cnxClient,
                    currModule.getAbsolutePath(), moduleId, CnxAtomPubUtils.LATEST_VERSION_WRAPPER,
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

            // TODO(arjuns) : This will always create a new module.
            mapOfModuleIdToNewModuleEntry.put(currModuleMigrtor.getCnxModuleId(),
                    currModuleMigrtor.getModuleWrapper().getId());
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

        CollectionVersionWrapper collectionVersionWrapper = null;

        try {
            collectionVersionWrapper =
                    cnxClient.getCollectionVersion(cnxCollectionId, currentVersion);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        URI editUri = null;
        if (collectionVersionWrapper != null) {
            editUri = collectionVersionWrapper.getEditUri();
        } else {
            CollectionWrapper collectionWrapper = null;
            if (isMigration) {
                collectionWrapper = cnxClient.createCollectionForMigration(cnxCollectionId);
            } else {
                collectionWrapper = cnxClient.createCollection();
            }
            editUri = collectionWrapper.getEditUri();
        }

        responseCollectionWrapper =
                cnxClient.createCollectionVersion(editUri, collectionXml);
        success = true;

        logger.info("Successfully uploaded : " + collectionLocation + " to : "
                + responseCollectionWrapper.getEditUri());

        return responseCollectionWrapper;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
