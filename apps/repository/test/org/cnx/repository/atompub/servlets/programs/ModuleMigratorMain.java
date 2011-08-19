/*
 * Copyright The CNX Authors.
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
package org.cnx.repository.atompub.servlets.programs;

import java.net.URL;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.repository.atompub.servlets.migrators.ModuleMigrator;

import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 *
 * @author Arjun Satyapal
 */
public class ModuleMigratorMain {
    private static CnxAtomPubClient cnxClient;

    public static void main(String[] args) throws Exception {
        String existingModuleId = "m34287";
        URL url = new URL("http://127.0.0.1:" + CnxAtomPubConstants.LOCAL_SERVER_PORT + "/atompub");
        cnxClient = new CnxAtomPubClient(url);

        String moduleFolder = "/home/arjuns/mymodule";

        final ClientEntry clientEntry;
        final String moduleId;
        final VersionWrapper currentVersion;

//        boolean updateOldModuleId = true;
        boolean updateOldModuleId = false;
        if (!updateOldModuleId) {
            // Create a new module.
            clientEntry = cnxClient.createNewModule();
        } else {
            URL moduleLatestUrl =
                cnxClient.getConstants().getModuleVersionAbsPath(existingModuleId,
                    new VersionWrapper(CnxAtomPubConstants.LATEST_VERSION_STRING));
            clientEntry = cnxClient.getService().getEntry(moduleLatestUrl.toString());
        }

        moduleId = CnxAtomPubConstants.getIdFromAtomPubId(clientEntry.getId());
        currentVersion = CnxAtomPubConstants.getVersionFromAtomPubId(clientEntry.getId());

        ModuleMigrator migrator = new ModuleMigrator(cnxClient);
        migrator.migrateVersion(moduleId, currentVersion, moduleFolder);
    }
}
