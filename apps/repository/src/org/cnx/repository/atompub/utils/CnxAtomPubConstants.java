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
package org.cnx.repository.atompub.utils;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment;
import com.google.common.base.Throwables;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 *
 * @author Arjun Satyapal
 */
public class CnxAtomPubConstants {
    Logger logger = Logger.getLogger(CnxAtomPubConstants.class.getName());
    /** URL for the Application. */
    public final URL applicationUrl;

    /** Subdomain for AtomPub relative to {@link #applicationUrl} */
    public static final String ATOMPUB_URL_PREFIX = "atompub";

    /** Path for REST URL for ATOMPUB API */
    public final URL atomPubRestUrl;

    public CnxAtomPubConstants(String reqUrl, int port) {
        // TODO(arjuns) : Find a better way to handle this as for unittests this returns null.
        String applicationId = SystemProperty.applicationId.get();
        Environment env = SystemProperty.environment;

        try {
            URL url = null;
            if (env.value() == SystemProperty.Environment.Value.Development) {
                url = new URL("http://localhost:" + port);
            } else {
                url = new URL("http://" + applicationId + ".appspot.com");
            }

            applicationUrl = url;
            atomPubRestUrl = new URL(applicationUrl.toString() + "/" + ATOMPUB_URL_PREFIX);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /**
     * Constants related to Service Document.
     */
    /** Path for Service Document relative to {@link #atomPubRestUrl}. */
    public static final String SERVICE_DOCUMENT_PATH = "/service_document";

    /** Get URI for CategoryDocument. */
    public String getServiceDocumentAbsPath() {
        return atomPubRestUrl + SERVICE_DOCUMENT_PATH;
    }

    /**
     * Constants related to Category Document.
     */
    /** Path for CategoryDocument relative to {@link #atomPubRestUrl}. */
    public static final String CATEGORIES_DOCUMENT_PATH = "/category_document";

    /** Get URI for CategoryDocument. */
    public String getCategoryDocumentAbsPath() {
        return atomPubRestUrl + CATEGORIES_DOCUMENT_PATH;
    }

    /**
     * Constants for Resources.
     */
    /** Name for AtomPub collection for CnxResources. */
    public static final String COLLECTION_RESOURCE_TITLE = "AtomPub Collection for CNX Resources.";

    /** Path for Resource AtomPub collection relative to {@link #atomPubRestUrl}. */
    public static final String COLLECTION_RESOURCE_REL_PATH = "/resource";

    /** Get URI for AtomPub collection for CNX Resources. */
    public URL getCollectionResourcesAbsPath() {
        try {
            return new URL(atomPubRestUrl + COLLECTION_RESOURCE_REL_PATH);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Scheme for AtomPub collection for CnxResources. */
    public final URL getCollectionResourceScheme() {
        return getCollectionResourcesAbsPath();
    }

    /**
     * Constants for Modules.
     */
    /** Name for AtomPub collection for CnxModules. */
    public static final String COLLECTION_MODULE_TITLE = "AtomPub Collection for CNX Modules.";

    /** Path for Module AtomPub Collection relative to {@link #atomPubRestUrl}. */
    public static final String COLLECTION_MODULE_REL_PATH = "/module";
    /** Path for GET operation relative to {@link #COLLECTION_MODULE_REL_PATH}. */
    public static final String COLLECTION_MODULE_GET_PATH = "/";
    public static final String COLLECTION_MODULE_POST_PATH = "/";

    /** Get URI for AtomPub collection for CNX Modules. */
    public URL getCollectionModulesAbsPath() {
        try {
            return new URL(atomPubRestUrl + COLLECTION_MODULE_REL_PATH);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Scheme for AtomPub collection for CnxModules. */
    public final URL getCollectionModuleScheme() {
        return getCollectionModulesAbsPath();
    }

    /**
     * Constants for Collections.
     */
    /** Name for AtomPub collection for CnxCollections.. */
    public static final String COLLECTION_CNX_COLLECTION_TITLE =
        "AtomPub Collection for Cnx Collections.";

    /** Path for CnxCollection AtomPub collection relative to {@link #atomPubRestUrl}. */
    public static final String COLLECTION_CNX_COLLECTION_REL_PATH = "/collection";

    /** Path for GET operation relative to {@link #COLLECTION_CNX_COLLECTION_REL_PATH}. */
    public static final String COLLECTION_CNX_COLLECTION_GET_PATH = "/";

    /** Get URI for AtomPub collection for CNX Collections. */
    public URL getCollectionCnxCollectionsAbsPath() {
        try {
            return new URL(atomPubRestUrl + COLLECTION_CNX_COLLECTION_REL_PATH);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Scheme for AtomPub collection for CnxModules. */
    public final URL getCollectionCnxCollectionScheme() {
        return getCollectionCnxCollectionsAbsPath();
    }

    /** Name for CNX Workspace. */
    public static final String CNX_WORKSPACE_TITLE = "Connexions Workspace";
}
