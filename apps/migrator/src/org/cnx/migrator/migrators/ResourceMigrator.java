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
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import javax.annotation.Nullable;

import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.objects.ResourceWrapper;
import org.cnx.migrator.context.MigratorContext;
import org.cnx.migrator.io.Directory;
import org.cnx.migrator.util.Log;
import org.cnx.migrator.util.MigratorUtil;


/**
 * A migrator for a resource item
 * 
 * @author tal
 */
public class ResourceMigrator extends ItemMigrator {

    /** The root directory of the resource to migrate */
    private final Directory resourceDirectory;

    public ResourceMigrator(MigratorContext context, Directory resourceDirectory) {
        super(context);
        this.resourceDirectory = resourceDirectory;
    }

    /**
     * Do the resource migration
     */
    @Override
    public void doWork() {
        final Properties properties = resourceDirectory.readPropertiesFile("properties");
        final String contentTypeProperty =
                checkNotNull(properties.getProperty("mimetype"), "Resource: %s", resourceDirectory);

        // An empty content type is mapped to null to represent 'unknown'.
        @Nullable String contentType = contentTypeProperty.isEmpty() ? null : contentTypeProperty;

        final String resourceId = resourceDirectoryToId(resourceDirectory);
        final File resourceFile = resourceDirectory.subFile("data");

        final URI uploadUri = createResource(resourceId);
        uploadResourceBlob(resourceId, uploadUri, resourceFile, resourceId, contentType);
    }

    /** Create a resource entity of given id and return an URI to upload its blob */
    private URI createResource(String resourceId) {
        getContext().incrementCounter("RESOURCES", 1);
        final IdWrapper idWrapper = new IdWrapper(resourceId, IdWrapper.Type.RESOURCE);

        int attempt;
        for (attempt = 1;; attempt++) {
            try {
                final ResourceWrapper resourceWrapper = getCnxClient().createResourceForMigration(idWrapper);
                checkArgument(resourceId.equals(resourceWrapper.getId().getId()), "Expected: [%s], found: [%s]",
                        resourceId, resourceWrapper.getId().getId());
                message("Resource entity creaded: %s", resourceId);
                return resourceWrapper.getUploadUri();
            } catch (Exception e) {
                if (attempt == 1) {
                    getContext().incrementCounter("RESOURCES_WITH_CREATION_RETRIES", 1);
                }
                getContext().incrementCounter("RESOURCE_CREATION_RETRIES", 1);
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException(
                            "Failed to create resource entity resource file after "
                                    + getConfig().getMaxAttempts() + " attempts: " + resourceId, e);
                }
                Log.message("Failed attemp %s to create resource %s", attempt, resourceId);
                Log.printStackTrace(e);
                MigratorUtil.sleep(getConfig().getFailureDelayMillis());
            }
        }
    }

    /** Upload a resource blob from a file */
    private void uploadResourceBlob(String resourceId, URI uploadUri, File resourceFile,
            String uploadFileName, @Nullable String contentType) {
        int attempt;
        for (attempt = 1;; attempt++) {
            try {
                // TODO(tal): send also content type from resource properties. Make sure
                // repository does not overide it.
                // TODO(tal): use upload file name from exported CNX migration data
                
                // TODO(tal): use const for the default content type.
                final String effectiveContentType = (contentType != null) ? contentType : "application/octet-stream";

                getCnxClient().uploadResource(uploadUri, effectiveContentType, uploadFileName, resourceFile);
                getContext().incrementCounter("RESOURCE_TYPE_[" + (contentType == null ? "(null)" : contentType) + "]", 1);
                message("Resource blob uploaded: %s", resourceId);
                // TODO(tal): verify resource by comparing its size and md5
                return;
            } catch (Exception e) {
                if (attempt == 1) {
                    getContext().incrementCounter("RESOURCES_WITH_BLOB_UPLOAD_RETRIES", 1);
                }
                getContext().incrementCounter("RESOURCE_BLOB_UPLOAD_RETRIES", 1);
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException("Failed to upload resource blob after "
                            + getConfig().getMaxAttempts() + " attempts: "
                            + resourceFile.getAbsolutePath(), e);
                }
                Log.message("Failed attemp %s to upload blob of resource %s", attempt, resourceId);
                Log.printStackTrace(e);
                MigratorUtil.sleep(getConfig().getFailureDelayMillis());
            }
        }
    }

    /** Map resource data directory to CNX resource id in the repository. */
    private static String resourceDirectoryToId(Directory resourceDirectory) {
        final String directoryName = resourceDirectory.getName(); // e.g. "0000012"
        final int directoryNumber = Integer.valueOf(directoryName); // e.g. 12
        // MOTE(tal): This matches the repository collection key to id mapping
        return String.format("r%04d", directoryNumber); // e.g. "m0012"
    }

}
