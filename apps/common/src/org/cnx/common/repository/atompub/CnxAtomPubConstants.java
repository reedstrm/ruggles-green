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
package org.cnx.common.repository.atompub;

import com.google.common.base.Throwables;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * TODO(arjuns) : Add javadoc.
 * TODO(arjuns) : Add tests for methods. 
 * @author Arjun Satyapal
 *
 */
public class CnxAtomPubConstants {
    private final Logger logger = Logger.getLogger(CnxAtomPubConstants.class.getName());

    /** Path for REST URL for ATOMPUB API */
    private final URL atomPubRestUrl;

    // TODO(arjuns) : Fix this.
    public final static int LOCAL_SERVER_PORT = 8888;

    public CnxAtomPubConstants(URL atomPubRestUrl) {
        this.atomPubRestUrl = atomPubRestUrl;
    }

    // TODO(arjuns) : Convert URL to URI.
    public URL getAtomPubRestUrl() {
        return atomPubRestUrl;
    }

    /** Get URI for CategoryDocument. */
    // TODO(arjuns) : Rename this to getAPServiceDocumentAbsPath()
    public String getServiceDocumentAbsPath() {
        return atomPubRestUrl + ServletUris.ServiceDocument.SERVICE_DOCUMENT_SERVLET;
    }

    /** Get URI for CategoryDocument. */
    // TODO(arjuns) : Rename this to getAPCategoryDocumentAbsPath()
    public String getCategoryDocumentAbsPath() {
        return atomPubRestUrl + ServletUris.CategoryDocument.CATEGORY_DOCUMENT_SERVLET;
    }

    /*
     * Methods for handling CNX Collections.
     */
    /** Get URL for AtomPub-Collection for CNX-Resources. */
    public URL getAPCResourcesAbsPath() {
        try {
            return new URL(atomPubRestUrl + ServletUris.Resource.RESOURCE_SERVLET);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Get URI for AtomPub-Collection for CNX Modules. */
    public URL getAPCModulesAbsPath() {
        try {
            return new URL(atomPubRestUrl + ServletUris.Module.MODULE_SERVLET);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Get URI for AtomPub-Collection for CNX-Collections. */
    public URL getAPCCollectionsAbsPath() {
        try {
            return new URL(atomPubRestUrl + ServletUris.Collection.COLLECTION_SERVLET);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /*
     * Methods for handling CNX Schemes.
     */
    /** Scheme for AtomPub-Collection for Resources. */
    public final URL getAPCResourceScheme() {
        return getAPCResourcesAbsPath();
    }

    /** Scheme for AtomPub-Collection for CNX-Modules. */
    public final URL getAPCModuleScheme() {
        return getAPCModulesAbsPath();
    }

    /** Scheme for AtomPub-collection for CNX-Modules. */
    public final URL getAPCCollectionScheme() {
        return getAPCCollectionsAbsPath();
    }

    /*
     * Methods to handle Resources.
     */
    /** Get URL for Resource to fetch via AtomPub. */
    public URL getResourceAbsPath(IdWrapper resourceId) {
        try {
            return new URL(getAPCResourcesAbsPath() + "/" + resourceId.getId());
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /*
     * Methods to handle Modules.
     */

    /** Get URL for Module to fetch via AtomPub. */
    public URL getModuleAbsPath(IdWrapper moduleId) {
        try {
            return new URL(getAPCModulesAbsPath() + "/" + moduleId.getId());
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Get URL for ModuleVersion to fetch via AtomPup. */
    public URL getModuleVersionAbsPath(IdWrapper moduleId, VersionWrapper version) {
        try {
            return new URL(getAPCModulesAbsPath() + "/" + moduleId.getId() + "/"
                    + version.toString());
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Get URL for CNXML for a ModuleVersion. */
    public URL getModuleVersionXmlAbsPath(IdWrapper moduleId, VersionWrapper version) {
        try {
            return new URL(getModuleVersionAbsPath(moduleId, version).toString() + "/"
                    + ServletUris.XML_DOC);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }
    
    // TODO(arjuns) : Add tests for this.
    /** Get URL for ResourceMapping XML for a ModuleVersion. */
    public URL getModuleVersionResourceMappingAbsPath(IdWrapper moduleId, VersionWrapper version) {
        try {
            return new URL(getModuleVersionAbsPath(moduleId, version).toString() + "/"
                    + ServletUris.RESOURCE_MAPPING_DOC);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /*
     * Methods to handle Collections.
     */
    /** Get URL for Collection-Version to fetch via AtomPub. */
    public URL getCollectionVersionAbsPath(IdWrapper collectionId, VersionWrapper version) {
        try {
            return new URL(getAPCCollectionsAbsPath() + "/" + collectionId.getId() + "/" + version);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));
            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    // TODO(arjuns) : Add test for this.
    /** Get URL for CollectionXml for a Collection-Version. */
    public URL getCollectionVersionXmlAbsPath(IdWrapper collectionId, VersionWrapper version) {
        try {
            URL url = getCollectionVersionAbsPath(collectionId, version);
            return new URL(url.toString() + "/" + ServletUris.XML_DOC);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));
            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

}
