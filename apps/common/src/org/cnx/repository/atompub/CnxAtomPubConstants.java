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
package org.cnx.repository.atompub;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.atom.Content;

/**
 *
 * @author Arjun Satyapal
 */
public class CnxAtomPubConstants {
    Logger logger = Logger.getLogger(CnxAtomPubConstants.class.getName());
    /** Subdomain for AtomPub relative to {@link #applicationUrl} */
    public static final String ATOMPUB_URL_PREFIX = "atompub";

    /** Path for REST URL for ATOMPUB API */
    public final URL atomPubRestUrl;

    // TODO(arjuns) : Fix this.
    public final static int LOCAL_SERVER_PORT = 8888;

    public CnxAtomPubConstants(URL atomPubRestUrl) {
        // TODO(arjuns) : Find a better way to handle this as for unittests this returns null.
        this.atomPubRestUrl = atomPubRestUrl;
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

    /** Get URL for AtomPub collection for CNX Resources. */
    public URL getCollectionResourcesAbsPath() {
        try {
            return new URL(atomPubRestUrl + COLLECTION_RESOURCE_REL_PATH);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Get URL for Resource to fetch via AtomPub. */
    public URL getResourceAbsPath(String resourceId) {
        try {
            return new URL(getCollectionResourcesAbsPath() + "/" + resourceId);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Relation tag for BlobstoreUrl under Other Link. */
    public static final String REL_TAG_FOR_BLOBSTORE_URL = "related";

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

    /** Version String for all versioned items. */
    public static final String VERSION_STRING = "version";

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

    /** Get URL for Module to fetch via AtomPub. */
    public URL getModuleAbsPath(String moduleId) {
        try {
            return new URL(getCollectionModulesAbsPath() + "/" + moduleId);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Get URL for ModuleVersion to fetch via AtomPub. */
    public URL getModuleVersionAbsPath(String moduleId, String version) {
        try {
            return new URL(getCollectionModulesAbsPath() + "/" + moduleId + "/" + VERSION_STRING
                + "/" + version);
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

    /** Relation tag for Edit links for CNX Resources/Modules/Collections. */
    public static final String LINK_RELATION_EDIT_TAG = "edit";

    /** Delimiter to connect Ids and Versions. */
    public static final String DELIMITER_ID_VERSION = ":";

    /** Delimiter for combining CNXML and Resource */
    public static final String DELIMITER_CONTENT = "!!!!----!!!!";

    /** Default new Version for any module. */
    public static final int NEW_MODULE_DEFAULT_VERSION = 0;

    /** Get AtomPub Content for CNXML Doc. */
    private Content getCnxmlContent(String cnxmlDoc) {
        Content cnxmlContent = new Content();
        // TODO(arjuns) : Move this to proper media type.
        cnxmlContent.setType("text");
        cnxmlContent.setValue(cnxmlDoc);
        cnxmlContent.setSrc("CNXML");

        return cnxmlContent;
    }

    /** Get AtomPub Content for ResourceMapping Doc. */
    private Content getResourceMappingContent(String resourceMappingDoc) {
        Content resourceMappingContent = new Content();
        // TODO(arjuns) : Move this to proper media type.
        resourceMappingContent.setType("text");
        resourceMappingContent.setValue(resourceMappingDoc);
        resourceMappingContent.setSrc("RESOURCE_MAPPING");

        return resourceMappingContent;
    }

    /** Get AtomPub List of Contents from CNXMl and ResourceMappingDoc. */
    public List<Content> getAtomPubListOfContent(String cnxmlDoc, String resourceMappingDoc) {

        StringBuilder contentValueBuilder =
            new StringBuilder().append(cnxmlDoc).append(DELIMITER_CONTENT).append(
                resourceMappingDoc);

        Content content = new Content();
        // TODO(arjuns) : Fix this to common media type.
        content.setType("application/xml");
        content.setValue(contentValueBuilder.toString());

        return Lists.newArrayList(content);

        // TODO(arjuns) : Fix this.
        // return Lists.newArrayList(getCnxmlContent(cnxmlDoc),
        // getResourceMappingContent(resourceMappingDoc));
    }

    /** Get CNXML Doc from Content */
    public static String getCnxmlDocFromContent(Content content) {
        String contentValue = content.getValue();

        String args[] = contentValue.split(DELIMITER_CONTENT);
        return args[0];
    }

    /** Get ResourceMapping Doc from Content */
    public static String getResourceMappingDocFromContent(Content content) {
        String contentValue = content.getValue();

        String args[] = contentValue.split(DELIMITER_CONTENT);
        return args[1];
    }

    /** Get moduleId/collectionId from AtomId. */
    public static String getIdFromAtomPubId(String atomPubId) {
        String[] args = atomPubId.split(":");
        return args[0];
    }

    /** Get version from AtomId. */
    public static String getVersionFromAtomPubId(String atomPubId) {
        String[] args = atomPubId.split(":");
        return args[1];
    }

    /** Get AtomPubId from moduleId/collectionId and version. */
    public static String getAtomPubIdFromCnxIdAndVersion(String cnxId, String version) {
        return cnxId + DELIMITER_ID_VERSION + version;
    }
}
