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

import static com.googlecode.charts4j.collect.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.common.repository.FileContentType;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.objects.ResourceWrapper;

/**
 * Migrator for a resource.
 * 
 * @author Arjun Satyapal
 */
public class ParallelResourceMigrator implements Runnable {
    private final Logger logger = Logger.getLogger(ParallelResourceMigrator.class.getName());
    private final CnxClient cnxClient;
    private final File localResource;
    private ResourceWrapper resourceWrapper;
    private boolean success = false;

    public File getLocalResource() {
        return localResource;
    }

    public ResourceWrapper getResourceWrapper() {
        return resourceWrapper;
    }

    public boolean isSuccess() {
        return success;
    }

    public ParallelResourceMigrator(CnxClient cnxClient, File localResourceLocation, 
            @Nullable ResourceWrapper resourceWrapper, boolean isMigration) {
        this.cnxClient = checkNotNull(cnxClient);
        this.resourceWrapper  = resourceWrapper;
        
        // For migration, values should be provided externally.
        if (isMigration) {
            checkNotNull(resourceWrapper);
        }
        this.localResource = checkNotNull(localResourceLocation);
    }

    // TODO(arjuns) : Replace probably with InputStream.
    public ResourceWrapper migrateResource() {
        for (int i = 0; i < 10; i++) {
            try {
                logger.info("Trying to upload : " + localResource.getAbsolutePath());
                String contentType =
                        FileContentType.getFileContentTypeEnumFromFileName(
                                localResource.getName())
                                .toString();
                if (resourceWrapper == null) {
                    resourceWrapper = cnxClient.createResource();
                }
                cnxClient.uploadResource(resourceWrapper.getUploadUri(), contentType,
                        localResource.getName(), localResource);
                success = true;
                logger.info("Successfully uploaded : " + localResource.getName() + " to : "
                        + resourceWrapper.getSelfUri());
                return resourceWrapper;
            } catch (Exception e) {
                // TODO(arjuns): Auto-generated catch block
                logger.severe(Throwables.getStackTraceAsString(e));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.severe(Throwables.getStackTraceAsString(e));
            }
        }

        logger.severe("Failed to upload Resource after 10 attempts : " + localResource);
        System.exit(1);
        return null;
    }

    public static Map<String, IdWrapper> getMapOfPrettyNameToResourceIdFromList(
            List<ParallelResourceMigrator> listOfMigrators) {
        Map<String, IdWrapper> mapOfPrettyNameToResourceIds = Maps.newHashMap();
        for (ParallelResourceMigrator currMigrator : listOfMigrators) {
            if (currMigrator.isSuccess()) {
                mapOfPrettyNameToResourceIds.put(currMigrator.getLocalResource().getName(),
                        currMigrator.getResourceWrapper().getId());
            } else {
                throw new RuntimeException("Failed to migrate : "
                        + currMigrator.getLocalResource().getAbsolutePath());
            }
        }
        
        return mapOfPrettyNameToResourceIds;
    }

    @Override
    public void run() {
        migrateResource();
    }
}
