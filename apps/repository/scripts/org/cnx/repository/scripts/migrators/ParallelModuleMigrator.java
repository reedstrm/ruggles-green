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

import org.cnx.common.repository.atompub.objects.ModuleVersionWrapper;

import org.cnx.common.exceptions.CnxPreconditionFailedException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.ModuleWrapper;

/**
 * Migrator for a module. Module should be
 * 
 * @author Arjun Satyapal
 */
public class ParallelModuleMigrator implements Runnable {
    private Logger logger = Logger.getLogger(ParallelModuleMigrator.class.getName());

    private final CnxClient cnxClient;
    private ModuleWrapper moduleWrapper;
    private final String moduleLocation;
    private final VersionWrapper newVersion;
    @SuppressWarnings("unused")
    private final boolean isMigration;

    private boolean success = false;

    private String cnxml;
    private String resourceMappingXml;

    /**
     * Constructor for Parallel Module Migrator.
     */
    public ParallelModuleMigrator(CnxClient cnxClient, String moduleLocation,
            @Nullable ModuleWrapper moduleWrapper, @Nullable VersionWrapper newVersion,
            boolean isMigration) {
        this.cnxClient = checkNotNull(cnxClient);
        this.moduleLocation = checkNotNull(moduleLocation);

        this.moduleWrapper = moduleWrapper;
        this.newVersion = newVersion;

        if (isMigration || newVersion != null) {
            checkNotNull(moduleWrapper);
        }

        if (moduleWrapper != null) {
            checkNotNull(newVersion);
        }

        this.isMigration = isMigration;
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
    public void migrateModuleVersion() {
        logger.info("Attempting to upload module : " + moduleLocation);

        List<ParallelResourceMigrator> listOfResourceMigrators = Lists.newArrayList();
        try {
            List<File> listOfResourcesToUpload = getListOfResourcesToBeUploaded(moduleLocation);

            List<Thread> listOfThreads = Lists.newArrayList();

            for (File currFile : listOfResourcesToUpload) {
                // TODO(arjuns) : Need to handle only specific exception. Else test will never die.

                ParallelResourceMigrator resourceMigrator =
                        new ParallelResourceMigrator(cnxClient, currFile,
                                null /* resourceWrapper */, false);
                listOfResourceMigrators.add(resourceMigrator);

                Thread thread = new Thread(resourceMigrator);
                listOfThreads.add(thread);
                thread.start();
            }

            for (Thread currThread : listOfThreads) {
                currThread.join();
            }
        } catch (Exception e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }

        // Successfully uploaded all the resources. Now trying to upload the CNXML and
        // ResourceMapping XML.

        Map<String, IdWrapper> mapOfPrettyNameToResourceId =
                ParallelResourceMigrator
                        .getMapOfPrettyNameToResourceIdFromList(listOfResourceMigrators);

        try {
            resourceMappingXml = cnxClient.getResourceMappingXml(mapOfPrettyNameToResourceId);
        } catch (JAXBException e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }
        for (int i = 0; i < 10; i++) {
            try {
                /*
                 * Modules will have two types of CNXML : * index.cnxml *
                 * index_auto_generated.cnxml. index.cnxml is the one that is published on cnx.
                 * whereas index_auto_generated.cnxml is one which is upgraded to 0.7 version.
                 */
                File cnxmlFile = new File(moduleLocation + "/index_auto_generated.cnxml");
                cnxml = Files.toString(cnxmlFile, Charsets.UTF_8);

                VersionWrapper publishVersion = null;
                if (moduleWrapper == null) {
                    moduleWrapper = cnxClient.createModule();
                    publishVersion = CnxAtomPubUtils.DEFAULT_EDIT_VERSION;
                } else {
                    if (newVersion != null) {
                        if (newVersion.equals(CnxAtomPubUtils.LATEST_VERSION_WRAPPER)) {
                            try {
                                ModuleVersionWrapper tempModuleWrapper =
                                        cnxClient.getModuleVersion(moduleWrapper.getId(),
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
                    }
                }

                // TODO(arjuns) : Modify this to return moduleVersion.
                moduleWrapper =
                        cnxClient.createModuleVersion(moduleWrapper.getId(), publishVersion, cnxml,
                                resourceMappingXml);
                success = true;

                logger.info("Successfully uploaded : "
                        + moduleWrapper.getId()
                        + " to : " + moduleWrapper.getSelfUri());
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

        logger.severe("Failed to upload module after 10 attempts : " + moduleLocation);
        System.exit(1);
    }

    @Override
    public void run() {
        migrateModuleVersion();
    }

    public boolean isSuccess() {
        return success;
    }

    public ModuleWrapper getModuleWrapper() {
        return moduleWrapper;
    }

    public String getResourceMappingXml() {
        return resourceMappingXml;
    }

    public String getCnxml() {
        return cnxml;
    }
}
