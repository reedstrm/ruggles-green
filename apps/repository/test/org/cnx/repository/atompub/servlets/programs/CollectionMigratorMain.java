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
import org.cnx.repository.atompub.servlets.migrators.CollectionMigrator;

import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 *
 * @author Arjun Satyapal
 */
public class CollectionMigratorMain {

    private static Logger logger = Logger.getLogger(ModuleMigratorMain.class.getName());

    private static CnxAtomPubClient cnxClient;

    public static void main(String[] args) throws Exception {
        // TODO(arjuns) : Convert this into an AppEngine app.
        URL url = new URL("http://101.cnx-repo.appspot.com/atompub");
//         URL url = new URL("http://127.0.0.1:" + CnxAtomPubConstants.LOCAL_SERVER_PORT +
//         "/atompub");
        cnxClient = new CnxAtomPubClient(url);

        String originalCollectionId = null;
        String collectionLocation = "/home/arjuns/cnxmodules/col10064_1.12_complete";
        // TODO(arjuns) : Add support so that new version for a collection can be posted.

        CollectionMigrator migrator = new CollectionMigrator(cnxClient);
        ClientEntry clientEntry = migrator.migrateCollection(originalCollectionId, collectionLocation);

        logger.info("New collection created at : " + clientEntry.getEditURI());
    }
}
