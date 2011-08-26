/*
 Copyright (C) 2011 The CNX Authors
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
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.VersionWrapper;
import org.jdom.JDOMException;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

/**
 * Migrator for a module.
 *
 * @author Arjun Satyapal
 */
public class ModuleMigrator {
    Logger logger = Logger.getLogger(ModuleMigrator.class.getName());

    private final CnxAtomPubClient cnxClient;

    public ModuleMigrator(CnxAtomPubClient cnxClient) {
        this.cnxClient = cnxClient;
    }

    public ClientEntry createNewModule(String moduleLocation) throws Exception {
        return migrateVersion(null, CnxAtomPubConstants.NEW_MODULE_DEFAULT_VERSION, moduleLocation);
    }

    // From a given module, extract list of files that need to be uploaded to Repository.
    public static List<File> getListOfResourcesToBeUploaded(String moduleLocation) {
        File folder = new File(moduleLocation);
        File[] listOfProbableResourceToUpload = folder.listFiles(new ResourceFilter());

        List<File> listOfResources = Lists.newArrayList();
        for (File currFile : listOfProbableResourceToUpload) {
            if (currFile.isFile()) {
                listOfResources.add(currFile);
            } else {
                throw new RuntimeException("Unexpected directory : " + currFile.getAbsolutePath());
            }
        }

        return listOfResources;
    }

    public static class ResourceFilter implements FilenameFilter {
        // List of File Extensions that will not be uploaded to repository.
        private Set<String> ignoreExtensions = Sets.newHashSet(".xml", ".cnxml");

        @Override
        public boolean accept(File dir, String fileName) {
            // If its a file check for extensions.
            for (String currExt : ignoreExtensions) {
                if (fileName.endsWith(currExt)) {
                    return false;
                }
            }

            return true;
        }
    }

    // TODO(arjuns) : Probably should take original version?
    public ClientEntry migrateVersion(String moduleId, VersionWrapper currentVersion,
            String moduleLocation) throws Exception {
        List<File> listOfResourcesToUpload = getListOfResourcesToBeUploaded(moduleLocation);

        List<ClientEntry> listOfEntryForUploadedResources = Lists.newArrayList();

        int failureCount = 0;
        while (listOfResourcesToUpload.size() != 0) {
            logger.info("Remaining resources = " + listOfResourcesToUpload.size());

            File currFile = listOfResourcesToUpload.get(0);
            try {
                // TODO(arjuns) : Need to handle only specific exception. Else test will never die.

                logger.info("Attempting to upload : " + currFile.getAbsolutePath());
                ClientEntry resourceEntry =
                    cnxClient.uploadFileToBlobStore(ResourceMigrator
                        .getResourceNameForResourceMappingDoc(currFile.getName()), currFile);
                listOfEntryForUploadedResources.add(resourceEntry);
                logger.info("Successuflly uploaded [" + currFile.getName() + "] as resourceId["
                    + resourceEntry.getId() + "], and can be found here ["
                    + cnxClient.getLinkForResource(resourceEntry).getHrefResolved() + "].");
                logger.info("Remaining resources = " + listOfResourcesToUpload.size());
                listOfResourcesToUpload.remove(currFile);
            } catch (Exception e) {
                logger.info("Failed to upload file : " + currFile.getName() + " due to : "
                    + Throwables.getStackTraceAsString(e));
                ;
                failureCount++;
                if (failureCount > 10) {
                    throw new RuntimeException("Too many failures. Try again.");
                }
            }
        }

        String resourceMappingXml =
            cnxClient.getResourceMappingFromResourceEntries(listOfEntryForUploadedResources);

        /*
         * Modules will have two types of CNXML : * index.cnxml * index_auto_generated.cnxml.
         *
         * index.cnxml is the one that is published on cnx. whereas index_auto_generated.cnxml is
         * one which is upgraded to 0.7 version.
         */
        File cnxml = new File(moduleLocation + "/index_auto_generated.cnxml");
        String cnxmlAsString = Files.toString(cnxml, Charsets.UTF_8);

        URL currentModuleUrl =
            cnxClient.getConstants().getModuleVersionAbsPath(moduleId, currentVersion);

        final ClientEntry entryToUpdate;
        if (moduleId == null) {
            Preconditions.checkArgument(currentVersion.getVersionInt() == 1);
            entryToUpdate = cnxClient.createNewModule();
            moduleId = CnxAtomPubConstants.getIdFromAtomPubId(entryToUpdate.getId());
        } else {
            entryToUpdate = cnxClient.getService().getEntry(currentModuleUrl.toString());
        }

        return publishNewVersion(entryToUpdate, cnxmlAsString, resourceMappingXml);
    }

    public ClientEntry publishNewVersion(ClientEntry entryToUpdate, String cnxmlAsString,
            String resourceMappingXml) throws ProponoException, JAXBException, JDOMException,
            IOException {
        ClientEntry createModuleVersionEntry =
            cnxClient.createNewModuleVersion(entryToUpdate, cnxmlAsString, resourceMappingXml);

        return createModuleVersionEntry;
    }
}
