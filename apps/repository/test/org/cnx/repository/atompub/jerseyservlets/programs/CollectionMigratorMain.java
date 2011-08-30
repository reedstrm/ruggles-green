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
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.jerseyservlets.migrators.ParallelCollectionMigrator;

import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 *
 * @author Arjun Satyapal
 */
public class CollectionMigratorMain {

    private static Logger logger = Logger.getLogger(ModuleMigratorMain.class.getName());

    private static CnxAtomPubClient cnxClient;

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        // TODO(arjuns) : Convert this into an AppEngine app.

        // TODO(arjuns) : Read this from properties file.
        URL url = new URL("http://qa-cnx-repo.appspot.com/atompub");
        url = new URL("http://127.0.0.1:" + CnxAtomPubConstants.LOCAL_SERVER_PORT + "/atompub");
        cnxClient = new CnxAtomPubClient(url);

        String originalCollectionId = null;
        String collectionLocation = "/home/arjuns/cnxmodules/col10064_1.12_complete";
        // TODO(arjuns) : Add support so that new version for a collection can be posted.

        ParallelCollectionMigrator migrator = new ParallelCollectionMigrator(cnxClient);
        ClientEntry clientEntry =
            migrator.migrateCollection(originalCollectionId, collectionLocation);

        long endTime = System.currentTimeMillis();
        logger.info("Time taken to create new collection : " + (endTime - startTime) / 1000);
        logger.info("New collection created at : " + clientEntry.getEditURI());
    }
}
