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
public class ParallelModuleMigrator implements Runnable {
    private Logger logger = Logger.getLogger(ParallelModuleMigrator.class.getName());

    private final CnxAtomPubClient cnxClient;
    private String cnxModuleId;
    private String aerModuleId;
    private final String moduleLocation;
    private final VersionWrapper currentVersion;
    private boolean success = false;
    private ClientEntry moduleVersionEntry;

    public ParallelModuleMigrator(CnxAtomPubClient cnxClient, String moduleLocation,
        String cnxModuleId, String aerModuleId, VersionWrapper currentVersion) {
        this.cnxClient = cnxClient;
        this.cnxModuleId = cnxModuleId;
        this.aerModuleId = aerModuleId;
        this.moduleLocation = moduleLocation;
        this.currentVersion = currentVersion;
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
    public ClientEntry migrateVersion() {
        logger.info("Attempting to upload module : " + moduleLocation);

        List<ClientEntry> listOfEntryForUploadedResources = Lists.newArrayList();
        try {
            List<File> listOfResourcesToUpload = getListOfResourcesToBeUploaded(moduleLocation);
            List<ParallelResourceMigrator> listOfResourceMigrators = Lists.newArrayList();
            List<Thread> listOfThreads = Lists.newArrayList();

            for (File currFile : listOfResourcesToUpload) {
                // TODO(arjuns) : Need to handle only specific exception. Else test will never die.

                ParallelResourceMigrator resourceMigrator =
                    new ParallelResourceMigrator(cnxClient, currFile.getAbsolutePath());
                listOfResourceMigrators.add(resourceMigrator);

                Thread thread = new Thread(resourceMigrator);
                listOfThreads.add(thread);
                thread.start();
            }

            for (Thread currThread : listOfThreads) {
                currThread.join();
            }

            for (ParallelResourceMigrator currMigrator : listOfResourceMigrators) {
                if (currMigrator.isSuccess()) {
                    listOfEntryForUploadedResources.add(currMigrator.getResourceEntry());
                } else {
                    throw new RuntimeException("Failed to upload resource : "
                        + currMigrator.getResourceLocation());
                }
            }

        } catch (Exception e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }

        // Successfully uploaded all the resources. Now trying to upload the CNXML and
        // ResourceMapping XML.
        for (int i = 0; i < 10; i++) {
            try {
                String resourceMappingXml =
                    cnxClient
                        .getResourceMappingFromResourceEntries(listOfEntryForUploadedResources);

                /*
                 * Modules will have two types of CNXML : * index.cnxml *
                 * index_auto_generated.cnxml.
                 *
                 * index.cnxml is the one that is published on cnx. whereas
                 * index_auto_generated.cnxml is one which is upgraded to 0.7 version.
                 */
                File cnxml = new File(moduleLocation + "/index_auto_generated.cnxml");
                String cnxmlAsString = Files.toString(cnxml, Charsets.UTF_8);

                final ClientEntry entryToUpdate;
                if (aerModuleId == null) {
                    entryToUpdate = cnxClient.createNewModule();
                    aerModuleId = CnxAtomPubConstants.getIdFromAtomPubId(entryToUpdate.getId());
                } else {
                    URL currentModuleUrl =
                        cnxClient.getConstants().getModuleVersionAbsPath(aerModuleId, currentVersion);
                    entryToUpdate = cnxClient.getService().getEntry(currentModuleUrl.toString());
                }

                moduleVersionEntry =
                    publishNewVersion(entryToUpdate, cnxmlAsString, resourceMappingXml);
                success = true;

                logger.info("Successfully uploaded : " + moduleLocation + " to : "
                    + moduleVersionEntry.getEditURI());
                return moduleVersionEntry;
            } catch (Exception e) {
                logger.severe(Throwables.getStackTraceAsString(e));
            }


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.severe(Throwables.getStackTraceAsString(e));
            }
        }

        logger.severe("Failed to upload module after 10 attempts : " + moduleLocation);
        System.exit(1);
        return null;
    }

    public ClientEntry publishNewVersion(ClientEntry entryToUpdate, String cnxmlAsString,
            String resourceMappingXml) throws ProponoException, JAXBException, JDOMException,
            IOException {
        ClientEntry createModuleVersionEntry =
            cnxClient.createNewModuleVersion(entryToUpdate, cnxmlAsString, resourceMappingXml);

        return createModuleVersionEntry;
    }

    @Override
    public void run() {
        migrateVersion();
    }

    public boolean isSuccess() {
        return success;
    }

    public ClientEntry getModuleVersionEntry() {
        return moduleVersionEntry;
    }

    public String getAerModuleId() {
        return aerModuleId;
    }

    public String getCnxModuleId() {
        return cnxModuleId;
    }
}