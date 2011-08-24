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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpException;
import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.VersionWrapper;
import org.jdom.JDOMException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

/**
 * Migrator for a Collection.
 *
 * @author Arjun Satyapal
 */
public class CollectionMigrator {
    Logger logger = Logger.getLogger(CollectionMigrator.class.getName());

    private Map<String, Entry> mapOfModuleIdToNewModuleEntry;
    private final CnxAtomPubClient cnxClient;

    public CollectionMigrator(CnxAtomPubClient cnxClient) {
        this.cnxClient = cnxClient;
        mapOfModuleIdToNewModuleEntry = Maps.newHashMap();
    }

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

    public ClientEntry migrateCollection(String origCollectionId, String collectionLocation)
            throws HttpException, ProponoException, IOException, JAXBException, JDOMException {
        List<File> listOfModulesToUpload = getListOfModulesToBeUploaded(collectionLocation);

        StringBuilder stringBuilder = new StringBuilder();

        List<File> listOfFailedModules = Lists.newArrayList();

        int failureCount = 0;
        while (listOfModulesToUpload.size() != 0) {
            // TODO(arjuns) : Need to handle only specific exception. Else test will never die.
            logger.info("**** Remaining size = " + listOfModulesToUpload.size());
            File currModule = listOfModulesToUpload.get(0);
            try {
                String existingModuleId = currModule.getName();
                logger.info("Starting to migrate : " + existingModuleId);
                ModuleMigrator moduleMigrator = new ModuleMigrator(cnxClient);
                ClientEntry migratedModuleEntry =
                    moduleMigrator.createNewModule(currModule.getAbsolutePath());

                // TODO(arjuns) : This will always create a new module.
                mapOfModuleIdToNewModuleEntry.put(existingModuleId, migratedModuleEntry);
                logger.info("Finished migrating : " + existingModuleId + " to : "
                    + migratedModuleEntry.getEditURI());
                listOfModulesToUpload.remove(currModule);
                logger.info("**** Remaining size = " + listOfModulesToUpload.size());
            } catch (Exception e) {
                logger.severe("Failed to upload module : " + currModule.getName() + " due to : "
                    + Throwables.getStackTraceAsString(e));
                failureCount++;
                if (failureCount > 10) {
                    throw new RuntimeException("Too many failures. Try again.");
                }
                continue;
            }
        }

        // Updating references for modules to new moduleId.
        File collXml = new File(collectionLocation + "/collection.xml");
        String origCollXmlAsString = Files.toString(collXml, Charsets.UTF_8);
        String collXmlAsString = Files.toString(collXml, Charsets.UTF_8);

        for (String oldModuleId : mapOfModuleIdToNewModuleEntry.keySet()) {
            Entry newModuleEntry = mapOfModuleIdToNewModuleEntry.get(oldModuleId);
            String newModuleId = CnxAtomPubConstants.getIdFromAtomPubId(newModuleEntry.getId());
            collXmlAsString = collXmlAsString.replaceAll(oldModuleId, newModuleId);
            logger.info("Old ModuleId = " + oldModuleId + " New ModuleId = " + newModuleId);
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
