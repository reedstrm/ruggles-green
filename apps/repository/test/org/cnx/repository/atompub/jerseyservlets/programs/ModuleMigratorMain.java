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
package org.cnx.repository.atompub.jerseyservlets.programs;

import java.net.URL;
import java.util.logging.Logger;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.repository.atompub.jerseyservlets.migrators.ParallelModuleMigrator;
import org.cnx.repository.atompub.jerseyservlets.programs.ModuleMigratorMain;

import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 * Migrator for migrating Modules to CNX repository.
 *
 * TODO(arjuns) : Add java flags.
 *
 * @author Arjun Satyapal
 */
public class ModuleMigratorMain {
    private static Logger logger = Logger.getLogger(ModuleMigratorMain.class.getName());

    private static CnxAtomPubClient cnxClient;

    public static void main(String[] args) throws Exception {

        // TODO(arjuns) : Read this from properties file.
        URL url = new URL("http://qa-cnx-repo.appspot.com/atompub");
//         url = new URL("http://127.0.0.1:" + CnxAtomPubConstants.LOCAL_SERVER_PORT + "/atompub");
        cnxClient = new CnxAtomPubClient(url);

        // String existingModuleId = "m42355";
        String moduleLocation = "/home/arjuns/cnxmodules/col10064_1.12_complete/m34670";
        long startTime = System.currentTimeMillis();
        ParallelModuleMigrator migrator =
            new ParallelModuleMigrator(cnxClient, moduleLocation, null/* cnxModuleId */,
                null/* aerModuleId */, null /* version */);

        Thread thread = new Thread(migrator);
        thread.start();
        thread.join();

        ClientEntry newclientEntry = migrator.getModuleVersionEntry();
        long endTime = System.currentTimeMillis();

        logger.info("Time to migrate = " + (endTime - startTime) / 1000);
        logger.info("Latest url = " + newclientEntry.getEditURI());
    }
}
