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

import java.io.File;
import java.util.Properties;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.migrator.config.MigratorConfiguration;
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

    public ResourceMigrator(MigratorConfiguration config, CnxAtomPubClient cnxClient,
            Directory resourceDirectory) {
        super(config, cnxClient);
        this.resourceDirectory = resourceDirectory;
    }

    /**
     * Do the resource migration
     */
    @Override
    public void doWork() {
        // TODO(tal): read and use resource properties
        @SuppressWarnings("unused")
        final Properties properties =
        resourceDirectory.readPropertiesFile("resource_properties.txt");

        // Upload resource file
        final File resourceFile = resourceDirectory.subFile("resource_data");
        int attempt;
        for (attempt = 1;; attempt++) {
            try {
                // TODO(tal): force resource id
                getCnxClient().uploadFileToBlobStore(resourceFile.getName(), resourceFile);
                message("Resource uploaded: %s", resourceDirectory.getName());
                // TODO(tal): verify resource by comparing its size and md5
                return;
            } catch (Exception e) {
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException("Failed to upload resource file after "
                            + getConfig().getMaxAttempts() + " attempts: "
                            + resourceFile.getAbsolutePath());
                }
                Log.printStackTrace(e);
                // wait a little before next try
                MigratorUtil.sleep(1000);
            }
        }
    }

}
