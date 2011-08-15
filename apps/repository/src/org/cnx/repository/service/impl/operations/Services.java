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

package org.cnx.repository.service.impl.operations;

import org.cnx.repository.service.api.CnxRepositoryConfiguration;
import org.cnx.repository.service.impl.configuration.CnxRepositoryConfigurationImpl;
import org.cnx.repository.service.impl.persistence.PersistenceService;

import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

/**
 * Provide access to singleton service clients.
 * 
 * @author Tal Dayan
 */
public class Services {

    // A single instance used by all queries of all threads.
    public static final BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();

    // A single instance used by all queries of all threads.
    public static final PersistenceService persistence = new PersistenceService(
        DatastoreServiceFactory.getDatastoreService());

    // TODO(tal): *** should we create an instance on the fly for each use?
    public static final BlobInfoFactory blobInfoFactory = new BlobInfoFactory();

    public static final CnxRepositoryConfiguration config = CnxRepositoryConfigurationImpl
        .getInstance();
}
