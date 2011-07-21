/*
 * Copyright 2011 Google Inc.
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

package org.cnx.repository.common;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

/**
 * Provide access to singleton service clients.
 * 
 * @author Tal Dayan
 */
public class Services {

    // A single instance used by all queries of all threads.
    public static final BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();

    // A single instance used by all queries of all threads.
    // Persistence policy string should match the specification in
    // jdoconfig.xml.
    public static final PersistenceManagerFactory datastore = JDOHelper
        .getPersistenceManagerFactory("default");

    // TODO(tal): should we create an instance on the fly for each use?
    public static final BlobInfoFactory blobInfoFactory = new BlobInfoFactory();

}
