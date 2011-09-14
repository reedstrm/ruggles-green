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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;

import org.apache.commons.codec.binary.Base64;
import org.cnx.resourceentry.ObjectFactory;
import org.cnx.resourceentry.ResourceEntryValue;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomPubConstants {
    private final Logger logger = Logger.getLogger(CnxAtomPubConstants.class.getName());

    /** Sub-domain for AtomPub relative to host. */
    public static final String ATOMPUB_URL_PREFIX = "atompub";

    /**
     * URLs ending with XML will point to CNXML : for modules. CollXml : For collections.
     */
    public static final String END_URL_XML = "/xml";

    /**
     * URLs ending with resources will point to ResourceMappingDoc for Collections.
     */
    public static final String END_URL_RESOURCES = "/resources";

    /** Path for REST URL for ATOMPUB API */
    public final URL atomPubRestUrl;

    // TODO(arjuns) : Fix this.
    public final static int LOCAL_SERVER_PORT = 8888;

    public CnxAtomPubConstants(URL atomPubRestUrl) {
        this.atomPubRestUrl = atomPubRestUrl;
    }

    // TODO(arjuns) : Convert URL to URI.
    public URL getAtomPubRestUrl() {
        return atomPubRestUrl;
    }

    /** String representation for latest version. */
    public static String LATEST_VERSION_STRING = "latest";

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

    /** Get URL for Collection-Version to fetch via AtomPub. */
    public URL getCollectionVersionAbsPath(IdWrapper collectionId, VersionWrapper version) {
        try {
            return new URL(getCollectionCnxCollectionsAbsPath() + "/"
                    + collectionId.getId() + "/"
                    + version);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));
            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Get URL for CollectionXml for a Collection-Version. */
    public URL getCollectionVersionXmlAbsPath(IdWrapper collectionId, VersionWrapper version) {
        try {
            URL url = getCollectionVersionAbsPath(collectionId, version);
            return new URL(url.toString() + END_URL_XML);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));
            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Get URL for Resource to fetch via AtomPub. */
    public URL getResourceAbsPath(IdWrapper resourceId) {
        try {
            return new URL(getCollectionResourcesAbsPath() + "/" + resourceId.getId());
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Relation tag for BlobstoreUrl under Other Link. */
    public static final String REL_TAG_FOR_BLOBSTORE_URL = "blobstore";

    /** Relation tag for Resource under Other Link. */
    public static final String REL_TAG_FOR_SELF_URL = "self";

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

    /** Default new Version for any module. */
    public static final VersionWrapper NEW_CNX_COLLECTION_DEFAULT_VERSION = new VersionWrapper("1");

    /** Latest version for any Module/collection. */
    public static final VersionWrapper LATEST_VERSION_WRAPPER = new VersionWrapper(
            LATEST_VERSION_STRING);

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
    public URL getModuleAbsPath(IdWrapper moduleId) {
        try {
            return new URL(getCollectionModulesAbsPath() + "/" + moduleId.getId());
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Get URL for ModuleVersion to fetch via AtomPup. */
    public URL getModuleVersionAbsPath(IdWrapper moduleId, VersionWrapper version) {
        try {
            return new URL(getCollectionModulesAbsPath() + "/" + moduleId.getId() + "/"
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
            return new URL(getModuleVersionAbsPath(moduleId, version).toString() + END_URL_XML);
        } catch (MalformedURLException e) {
            logger.severe("Failed to create URL due to : " + Throwables.getStackTraceAsString(e));

            // TODO(arjuns): Create a CNXAtomPubException to handle this.
            throw new RuntimeException(e);
        }
    }

    /** Get URL for ResourceMapping XML for a ModuleVersion. */
    public URL getModuleVersionResourceMappingAbsPath(IdWrapper moduleId, VersionWrapper version) {
        try {
            return new URL(getModuleVersionAbsPath(moduleId, version).toString()
                    + END_URL_RESOURCES);
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

    // TODO(arjuns) : Rename CnxCollection -> Collection and Collection-> AtomPubCollection
    // so that it is consistent with repository.
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

    /**
     * Get Collection XML from AtomPub Entry.
     * 
     * TODO(arjuns) : Make it static and move it to utils.
     */
    public String getCollXmlDocFromAtomPubCollectionEntry(Entry apCollectionEntry)
            throws UnsupportedEncodingException {
        Content content = (Content) apCollectionEntry.getContents().get(0);
        return decodeFromBase64EncodedString(content.getValue());
    }

    /** Scheme for AtomPub collection for CnxModules. */
    public final URL getCollectionCnxCollectionScheme() {
        return getCollectionCnxCollectionsAbsPath();
    }

    /** Name for CNX Workspace. */
    public static final String CNX_WORKSPACE_TITLE = "Connexions Workspace";

    /** Relation tag for Self links for CNX Resources/Modules/Collections. */
    public static final String LINK_RELATION_SELF_TAG = "self";

    /** Relation tag for Edit links for CNX Resources/Modules/Collections. */
    public static final String LINK_RELATION_EDIT_TAG = "edit";

    /** Delimiter to connect Ids and Versions. */
    public static final String DELIMITER_ID_VERSION = ":";

    /** Default new Version for any module. */
    public static final VersionWrapper NEW_MODULE_DEFAULT_VERSION = new VersionWrapper(1);

    /**
     * Get AtomPub List of Contents from CNXMl and ResourceMappingDoc.
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws JDOMException
     */
    public List<Content> getAtomPubListOfContent(String cnxmlDoc, String resourceMappingDoc)
            throws JAXBException, JDOMException, IOException {
        String moduleEntryXml = getModuleEntryValue(cnxmlDoc, resourceMappingDoc);
        String encodedModuleEntryXml = encodeToBase64EncodedString(moduleEntryXml);

        Content content = new Content();
        // TODO(arjuns) : Fix this to common media type.
        content.setType("text");
        content.setValue(encodedModuleEntryXml);

        return Lists.newArrayList(content);
    }

    /**
     * Get AtomPub List of Contents from CollXml.
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws JDOMException
     */
    public List<Content> getAtomPubListOfContentForCollectionEntry(String collXmlDoc)
            throws JAXBException, JDOMException, IOException {
        String encodedCollXml = encodeToBase64EncodedString(collXmlDoc);

        Content content = new Content();
        // TODO(arjuns) : Fix this to common media type.
        content.setType("text");
        content.setValue(encodedCollXml);

        return Lists.newArrayList(content);
    }

    /**
     * Get CNXML-doc from ModuleEntry XML.
     * 
     * @param moduleEntryXml ModuleEntry returned as part of the response. This method expects that
     *            moduleEntryXml is already decoded.
     * @return CNXML (response is already decoded).
     */
    public String getCnxmlFromModuleEntryXml(String moduleEntryXml) throws JDOMException,
            IOException {
        return getDecodedChild("cnxml-doc", moduleEntryXml);
    }

    /**
     * Get ResourceMapping doc from decoded ModuleEntry XML.
     * 
     * @param moduleEntryXml ModuleEntry returned as part of the response. This method expects that
     *            moduleEntryXml is already decoded.
     * @return ResourceMapping XML (response is already decoded).
     */
    public String getResourceMappingDocFromModuleEntryXml(String moduleEntryXml)
            throws JDOMException, IOException {
        return getDecodedChild("resource-mapping-doc", moduleEntryXml);
    }

    private String getDecodedChild(String childElement, String moduleEntryXml)
            throws JDOMException, IOException {

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(new StringReader(moduleEntryXml));

        Element root = document.getRootElement();
        String encodedXml = root.getChild(childElement).getText();
        String originalXml = decodeFromBase64EncodedString(encodedXml);

        return originalXml;
    }

    /**
     * Get moduleId/collectionId from AtomId.
     * 
     * TODO(arjuns) : Write a wrapper for this.
     */
    public static IdWrapper getIdFromAtomPubId(String atomPubId) {
        String[] args = atomPubId.split(":");
        return IdWrapper.getIdWrapper(args[0]);
    }

    /**
     * Get version from AtomId.
     */
    public static VersionWrapper getVersionFromAtomPubId(String atomPubId) {
        String[] args = atomPubId.split(":");
        return new VersionWrapper(args[1]);
    }

    /**
     * Get AtomPubId from moduleId/collectionId and version.
     */
    public static String getAtomPubIdFromCnxIdAndVersion(IdWrapper cnxId, VersionWrapper version) {
        return cnxId.getId() + DELIMITER_ID_VERSION + version.toString();

    }

    /**
     * Decode a 64 Bit encoded String.
     * 
     * @param encodedString 64bit encoded string.
     * @return Decoded string.
     * @throws UnsupportedEncodingException
     */
    public String decodeFromBase64EncodedString(String encodedString)
            throws UnsupportedEncodingException {
        byte[] decodedBytes = decodeBase64EncodedStringToBytes(encodedString);
        return new String(decodedBytes, Charsets.UTF_8.displayName());
    }

    private byte[] decodeBase64EncodedStringToBytes(String encodedString) {
        return Base64.decodeBase64(encodedString);
    }

    /**
     * Encode a String to a Base64-encoded string.
     * 
     * @param originalString : String to be encoded.
     * @return Base64-encoded String.
     * @throws UnsupportedEncodingException
     */
    public String encodeToBase64EncodedString(String originalString)
            throws UnsupportedEncodingException {
        return new String(Base64.encodeBase64(originalString.getBytes(Charsets.UTF_8)),
                Charsets.UTF_8);
    }

    /**
     * Get XML for Module Entry. This consists of 64 bit encoded CNXML and 64 bit encoded
     * ResourceMapping Doc. This is used to create {@link ResourceEntryValue}
     * 
     * @param cnxml CNXML source
     * @param resourceMappingXml Resource mapping XML source
     * 
     * @return XML representation for {@link ResourceEntryValue}
     * @throws JAXBException
     */
    public String getModuleEntryValue(String cnxml, String resourceMappingXml) throws JAXBException {
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<byte[]> encodedCnxmlDoc;
        JAXBElement<byte[]> encodedResourceMappingDoc;
        encodedCnxmlDoc = objectFactory.createCnxmlDoc(cnxml.getBytes(Charsets.UTF_8));
        encodedResourceMappingDoc =
                objectFactory.createResourceMappingDoc(resourceMappingXml.getBytes(Charsets.UTF_8));

        ResourceEntryValue resourceEntryValue = objectFactory.createResourceEntryValue();
        resourceEntryValue.setCnxmlDoc(encodedCnxmlDoc.getValue());
        resourceEntryValue.setResourceMappingDoc(encodedResourceMappingDoc.getValue());

        // TODO(arjuns) : Move this inside xmls folder.

        String moduleEntryValueXml =
                jaxbObjectToString(ResourceEntryValue.class, resourceEntryValue);
        return moduleEntryValueXml;
    }

    /**
     * Convert a JAXB object to its XML Representation.
     * 
     * @param jaxbObject JAXB object whose XML representation is required.
     * @param rootClass Class annotated with RootElement for JAXB objects.
     * 
     * @return XML representation for JAXB Object.
     */
    @SuppressWarnings("rawtypes")
    public String jaxbObjectToString(Class rootClass, Object jaxbObject) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(rootClass);
        Marshaller marshaller = jaxbContext.createMarshaller();

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(jaxbObject, stringWriter);

        return stringWriter.toString();
    }
}
