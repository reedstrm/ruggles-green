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

import static org.cnx.repository.atompub.CnxAtomPubConstants.LATEST_VERSION_WRAPPER;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.exceptions.CnxRuntimeException;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.IdWrapper;
import org.cnx.repository.atompub.VersionWrapper;
import org.jdom.JDOMException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

/**
 * Migrator for a Collection.
 * 
 * @author Arjun Satyapal
 */
public class ParallelCollectionMigrator implements Runnable {
    private Logger logger = Logger.getLogger(ParallelCollectionMigrator.class.getName());

    private Map<String, Entry> mapOfModuleIdToNewModuleEntry;
    private final CnxAtomPubClient cnxClient;
    private final IdWrapper cnxCollectionId;
    private final IdWrapper aerCollectionId;
    private final String collectionLocation;
    private final VersionWrapper currentVersion;
    private final boolean preserveModuleIds;

    private boolean success = false;
    private ClientEntry collectionVersionEntry;

    public ParallelCollectionMigrator(CnxAtomPubClient cnxClient, String collectionLocation,
            IdWrapper cnxCollectionId, IdWrapper aerCollectionId, VersionWrapper currentVersion,
            boolean preserveModuleIds) {
        this.cnxClient = cnxClient;
        this.collectionLocation = collectionLocation;
        this.aerCollectionId = aerCollectionId;
        this.cnxCollectionId = cnxCollectionId;
        this.currentVersion = currentVersion;
        this.preserveModuleIds = preserveModuleIds;
        mapOfModuleIdToNewModuleEntry = Maps.newHashMap();
    }

    public ClientEntry getCollectionVersionEntry() {
        return collectionVersionEntry;
    }

    public boolean isSuccess() {
        return success;
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

    public ClientEntry migrateCollectionVersion() {
        try {
            List<File> listOfModulesToUpload = getListOfModulesToBeUploaded(collectionLocation);

            List<ParallelModuleMigrator> listOfModuleMigrators = Lists.newArrayList();
            List<Thread> listOfThreads = Lists.newArrayList();

            int counter = 1;
            for (File currModule : listOfModulesToUpload) {
                // TODO(arjuns) : Need to handle only specific exception. Else test will never die.

                String cnxModuleId = currModule.getName();
                logger.info("Starting to migrate : " + cnxModuleId);

                IdWrapper requiredCnxModuleId = null;
                IdWrapper requiredAerModuleId = null;
                VersionWrapper requiredVersion = null;

                if (cnxCollectionId != null && preserveModuleIds) {
                    // Publish version in restricted range.
                    requiredCnxModuleId = IdWrapper.getIdWrapper(cnxModuleId);
                    requiredVersion = CnxAtomPubConstants.LATEST_VERSION_WRAPPER;
                }

                if (aerCollectionId != null && preserveModuleIds) {
                    requiredAerModuleId = IdWrapper.getIdWrapper(cnxModuleId);
                }

                ParallelModuleMigrator moduleMigrator =
                        new ParallelModuleMigrator(cnxClient, currModule.getAbsolutePath(),
                                currModule.getName(), requiredCnxModuleId, requiredAerModuleId,
                                requiredVersion);
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

                ClientEntry migratedModuleEntry = currModuleMigrtor.getModuleVersionEntry();
                // TODO(arjuns) : This will always create a new module.
                mapOfModuleIdToNewModuleEntry.put(currModuleMigrtor.getCollXmlModuleId(),
                        migratedModuleEntry);
            }

            // Successfully uploaded all the modules. Now attempting to upload collection.xml

            // Updating references for modules to new moduleId.
            File collXml = new File(collectionLocation + "/collection.xml");
            String collXmlAsString = Files.toString(collXml, Charsets.UTF_8);

            for (String cnxModuleId : mapOfModuleIdToNewModuleEntry.keySet()) {
                Entry newModuleEntry = mapOfModuleIdToNewModuleEntry.get(cnxModuleId);
                IdWrapper aerModuleId =
                        CnxAtomPubConstants.getIdFromAtomPubId(newModuleEntry.getId());
                String oldString = "\"" + cnxModuleId + "\"";
                String newString = "\"" + aerModuleId.getId() + "\"";
                collXmlAsString = collXmlAsString.replaceAll(oldString, newString);
                logger.info("Old ModuleId = " + cnxModuleId + " New ModuleId = "
                        + aerModuleId.getId());
            }

            ClientEntry entryToUpdate = null;

            if (cnxCollectionId != null) {
                /*
                 * This means that we are trying to migrate a cnx collection and want to retain its
                 * original Id. So first check if it exists and if not, then create one.
                 */
                ClientEntry existingEntry = null;

                try {
                    existingEntry =
                            cnxClient.getCollectionVersionEntry(cnxCollectionId,
                                    LATEST_VERSION_WRAPPER);
                } catch (CnxRuntimeException e) {
                    if (e.getJerseyStatus() == Status.NOT_FOUND) {
                        // Expected.
                        logger.info(e.getLocalizedMessage());
                    } else {
                        throw e;
                    }
                }

                if (existingEntry != null) {
                    entryToUpdate = existingEntry;
                } else {
                    entryToUpdate = cnxClient.createNewCollectionForMigration(cnxCollectionId);
                }
            } else if (aerCollectionId == null) {
                entryToUpdate = cnxClient.createNewCollection();
            } else {
                /*
                 * Collection already exists. Try to get the entry and then update it.
                 */
                Preconditions.checkNotNull(currentVersion);
                entryToUpdate =
                        cnxClient.getCollectionVersionEntry(aerCollectionId, currentVersion);
            }

            collectionVersionEntry = publishNewVersion(entryToUpdate, collXmlAsString);
            success = true;
            logger.info("Successfully uploaded Collection : " + collectionLocation + " to : "
                    + collectionVersionEntry.getEditURI());
            return collectionVersionEntry;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ClientEntry publishNewVersion(ClientEntry entryToUpdate, String collXmlAsString)
            throws ProponoException, JAXBException, JDOMException, IOException {
        cnxClient.createNewCollectionVersion(entryToUpdate, collXmlAsString);

        return entryToUpdate;
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
        migrateCollectionVersion();
    }
}
