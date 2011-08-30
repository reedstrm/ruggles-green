/*
 * Copyright 2011 Google Inc.
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
package org.cnx.repository.atompub.servlets.migrators;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.VersionWrapper;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 * Migrator for a Collection.
 *
 * @author Arjun Satyapal
 */
public class ParallelCollectionMigrator {
    private Logger logger = Logger.getLogger(ParallelCollectionMigrator.class.getName());

    private Map<String, Entry> mapOfModuleIdToNewModuleEntry;
    private final CnxAtomPubClient cnxClient;

    public ParallelCollectionMigrator(CnxAtomPubClient cnxClient) {
        this.cnxClient = cnxClient;
        mapOfModuleIdToNewModuleEntry = Maps.newHashMap();
    }

    // TODO(arjuns) : Move this to common.
    public Link getSelfLinkFromEntry(Entry entry) {
        @SuppressWarnings("unchecked")
        List<Link> otherLinks = entry.getOtherLinks();

        for (Link currLink : otherLinks) {
            if (currLink.getRel().equals(CnxAtomPubConstants.REL_TAG_FOR_SELF_URL)) {
                return currLink;
            }
        }

        return null;
    }

    public ClientEntry migrateCollection(String origCollectionId, String collectionLocation) {
        try {
            List<File> listOfModulesToUpload = getListOfModulesToBeUploaded(collectionLocation);

            List<ParallelModuleMigrator> listOfModuleMigrators = Lists.newArrayList();
            List<Thread> listOfThreads = Lists.newArrayList();

            int counter = 1;
            for (File currModule : listOfModulesToUpload) {
                // TODO(arjuns) : Need to handle only specific exception. Else test will never die.


                String cnxModuleId = currModule.getName();
                logger.info("Starting to migrate : " + cnxModuleId);

                ParallelModuleMigrator moduleMigrator =
                    new ParallelModuleMigrator(cnxClient, currModule.getAbsolutePath(),
                        cnxModuleId, null/*aerModuleId*/, null /*version*/);
                listOfModuleMigrators.add(moduleMigrator);

                Thread thread = new Thread(moduleMigrator);
                listOfThreads.add(thread);

                thread.start();
//                thread.join();

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

                ClientEntry migratedModuleEntry = currModuleMigrtor.getModuleVersionEntry();
                // TODO(arjuns) : This will always create a new module.
                mapOfModuleIdToNewModuleEntry.put(currModuleMigrtor.getCnxModuleId(),
                    migratedModuleEntry);
            }

            // Successfully uploaded all the apps. Now attempting to upload collection.xml

             // Updating references for modules to new moduleId.
            File collXml = new File(collectionLocation + "/collection.xml");
            String collXmlAsString = Files.toString(collXml, Charsets.UTF_8);

            for (String cnxModuleId : mapOfModuleIdToNewModuleEntry.keySet()) {
                Entry newModuleEntry = mapOfModuleIdToNewModuleEntry.get(cnxModuleId);
                String aerModuleId = CnxAtomPubConstants.getIdFromAtomPubId(newModuleEntry.getId());
                String oldString = "\"" + cnxModuleId + "\"";
                String newString = "\"" + aerModuleId + "\"";
                collXmlAsString = collXmlAsString.replaceAll(oldString, newString);
                logger.info("Old ModuleId = " + cnxModuleId + " New ModuleId = " + aerModuleId);
            }

            ClientEntry createCollectionEntry = cnxClient.createNewCollection();
            ClientEntry createCollectionVersionEntry =
                cnxClient.createNewCollectionVersion(createCollectionEntry, collXmlAsString);

            String newCollectionId =
                CnxAtomPubConstants.getIdFromAtomPubId(createCollectionVersionEntry.getId());
            VersionWrapper newVersion =
                CnxAtomPubConstants.getVersionFromAtomPubId(createCollectionVersionEntry.getId());

            ClientEntry getCollectionVersionEntry =
                cnxClient.getCollectionVersionEntry(newCollectionId, newVersion);

            Link newLink = getSelfLinkFromEntry(getCollectionVersionEntry);
            logger.info("New location for collection = \n" + newLink.getHrefResolved());

            return createCollectionVersionEntry;
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
}
