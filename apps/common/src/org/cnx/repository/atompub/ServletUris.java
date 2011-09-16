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
package org.cnx.repository.atompub;

import org.cnx.repository.RepositoryConstants;

/**
 * This class is used to track servlet URL patterns for Repository and Webviewer.
 * 
 * @author Arjun Satyapal
 */
public class ServletUris {
    public static final String RESOURCE_ID_PATH_PARAM = "resourceId";

    public static final String MODULE_ID_PATH_PARAM = "moduleId";
    public static final String MODULE_VERSION_PATH_PARAM = "moduleVersion";

    public static final String COLLECTION_ID_PATH_PARAM = "collectionId";
    public static final String COLLECTION_VERSION_PATH_PARAM = "collectionVersion";

    public static final String MIGRATION = "migration";
    public static final String XML_DOC = "xml";
    public static final String RESOURCE_MAPPING_DOC = "resources";

    public static class ServiceDocument {
        /**
         * URI relative to for CnxAtomPub ServiceDocument Servlet relative to
         * Jersey root package. It is defined in Application's web.xml file.
         */
        public static final String SERVICE_DOCUMENT_SERVLET = "/service_document";

        /**
         * URI relative to : {@value #SERVICE_DOCUMENT_SERVLET}.
         * 
         * To fetch AtomPub ServiceDocument, clients should do HTTP Get on this URI.
         */
        public static final String SERVICE_DOCUMENT_PATH = "/";
    }

    public static class CategoryDocument {
        /**
         * URI for CnxAtomPub CategoryDocument Servlet relative to
         * Jersey root package. It is defined in Application's web.xml file.
         */
        public static final String CATEGORY_DOCUMENT_SERVLET = "/category_document";

        /**
         * URI relative to : {@link #CATEGORY_DOCUMENT_SERVLET}.
         * 
         * To fetch AtomPub CategoryDocument, clients should do HTTP Get on this URI.
         */
        public static final String CATEGORY_DOCUMENT_PATH = "/";
    }

    public static class Resource {
        /**
         * URI for CnxAtomPub ResourceServlet relative to Jersey root package. It is defined in
         * Application's web.xml file.
         * .
         */
        public static final String RESOURCE_SERVLET = "/resource";

        /**
         * URI relative to : {@link #RESOURCE_SERVLET}.
         * 
         * In order to create a new ResourceId, clients should post to this URI.
         */
        public static final String RESOURCE_POST_NEW = "/";

        /**
         * URI relative to : {@link #RESOURCE_SERVLET}
         * 
         * In order to create a ResourceId below{@link RepositoryConstants#MIN_NON_RESERVED_KEY_ID}
         * clients should post to this URI.
         */
        public static final String RESOURCE_POST_MIGRATION = "/" + MIGRATION + "/{"
                + RESOURCE_ID_PATH_PARAM + "}";

        /**
         * URI relative to : {@link #RESOURCE_SERVLET}
         * 
         * In order to fetch the resource, clients should do HTTP Get on this URI.
         */
        public static final String RESOURCE_PATH = "/{" + RESOURCE_ID_PATH_PARAM + "}";
    }

    public static class Module {
        /**
         * URI for CnxAtomPub ModuleServlet relative to Jersey root package. It is defined in
         * Application's web.xml file.
         */
        public static final String MODULE_SERVLET = "/module";

        /**
         * URI relative to : {@link #MODULE_SERVLET}.
         * 
         * In order to create a new ModuleId, clients should post to this URI.
         */
        public static final String MODULE_POST_NEW = "/";

        /**
         * URI relative to : {@link #MODULE_SERVLET}
         * 
         * In order to create a ModuleId below{@link RepositoryConstants#MIN_NON_RESERVED_KEY_ID}
         * clients should post to this URI.
         */
        public static final String MODULE_POST_MIGRATION = "/" + MIGRATION + "/{"
                + MODULE_ID_PATH_PARAM + "}";

        /**
         * URI relative to : {@link #MODULE_SERVLET}
         * 
         * Path to Module.
         */
        public static final String MODULE_PATH = "/{" + MODULE_ID_PATH_PARAM + "}";

        /**
         * URI relative to : {@link #MODULE_PATH}
         * 
         * Path to a Module-Version.
         */
        public static final String MODULE_VERSION_PATH = MODULE_PATH + "/{"
                + MODULE_VERSION_PATH_PARAM + "}";

        /**
         * URI relative to : {@link #MODULE_VERSION_PATH}
         * 
         * In order to fetch CNXML for a Module-Version, clients should do HTTP Get on this URI.
         */
        public static final String MODULE_VERSION_CNXML = MODULE_VERSION_PATH + "/" + XML_DOC;

        /**
         * URI relative to : {@link #MODULE_VERSION_PATH}
         */
        public static final String MODULE_VERSION_RESOURCE_MAPPING = MODULE_VERSION_PATH + "/"
                + RESOURCE_MAPPING_DOC;
    }

    public static class Collection {
        /**
         * URI for CnxAtomPub CollectionServlet relative to Jersey root package. It is defined in
         * Application's web.xml file.
         */
        public static final String COLLECTION_SERVLET = "/collection";

        /**
         * URI relative to : {@link #COLLECTION_SERVLET}.
         * 
         * In order to create a new CollectionId, clients should post to this URI.
         */
        public static final String COLLECTION_POST_NEW = "/";

        /**
         * URI relative to : {@link #COLLECTION_SERVLET}
         * 
         * In order to create a CollectionId below
         * {@link RepositoryConstants#MIN_NON_RESERVED_KEY_ID} clients should post to this URI.
         */
        public static final String COLLECTION_POST_MIGRATION = "/" + MIGRATION + "/{"
                + COLLECTION_ID_PATH_PARAM + "}";

        /**
         * URI relative to : {@link #COLLECTION_SERVLET}
         * 
         * Path to Collection.
         */
        public static final String COLLECTION_PATH = "/{" + COLLECTION_ID_PATH_PARAM + "}";

        /**
         * URI relative to : {@link #COLLECTION_PATH}
         * 
         * Path to a Collection-Version.
         */
        public static final String COLLECTION_VERSION_PATH = COLLECTION_PATH + "/{"
                + COLLECTION_VERSION_PATH_PARAM + "}";

        /**
         * URI relative to : {@link #COLLECTION_VERSION_PATH}
         * 
         * In order to fetch COLLXML for a Collection-Version, clients should do HTTP Get on this
         * URI.
         */
        public static final String COLLECTION_VERSION_COLLXML = COLLECTION_VERSION_PATH + "/"
                + XML_DOC;
    }
}
