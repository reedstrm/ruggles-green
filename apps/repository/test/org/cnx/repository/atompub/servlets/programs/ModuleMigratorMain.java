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
import java.util.logging.Logger;

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
    private static Logger logger = Logger.getLogger(ModuleMigratorMain.class.getName());

    private static CnxAtomPubClient cnxClient;

    public static void main(String[] args) throws Exception {

        URL url = new URL("http://100.cnx-repo.appspot.com/atompub");
        // URL url = new URL("http://127.0.0.1:" + CnxAtomPubConstants.LOCAL_SERVER_PORT +
        // "/atompub");
        cnxClient = new CnxAtomPubClient(url);

//        String existingModuleId = null;
        String existingModuleId = "m42355";
        String moduleLocation = "/home/arjuns/mymodule";

        final ClientEntry clientEntry;
        final String moduleId;
        final VersionWrapper currentVersion;

        ModuleMigrator migrator = new ModuleMigrator(cnxClient);
        if (existingModuleId == null) {
            // Create a new module.
            clientEntry = migrator.createNewModule(moduleLocation);
            logger.info("Created new module at : " + clientEntry.getEditURI());
        } else {
            URL moduleLatestUrl =
                cnxClient.getConstants().getModuleVersionAbsPath(existingModuleId,
                    new VersionWrapper(CnxAtomPubConstants.LATEST_VERSION_STRING));
            clientEntry = cnxClient.getService().getEntry(moduleLatestUrl.toString());
            currentVersion = CnxAtomPubConstants.getVersionFromAtomPubId(clientEntry.getId());

            migrator.migrateVersion(existingModuleId, currentVersion, moduleLocation);
            moduleLatestUrl =
                cnxClient.getConstants().getModuleVersionAbsPath(existingModuleId,
                    new VersionWrapper(CnxAtomPubConstants.LATEST_VERSION_STRING));
            ClientEntry newclientEntry = cnxClient.getService().getEntry(moduleLatestUrl.toString());

            logger.info("Latest url = " + newclientEntry.getEditURI());
        }
    }
}
