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

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.impl.Atom10Parser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.commons.codec.binary.Base64;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.PrettyXmlOutputter;
import org.cnx.resourceentry.ResourceEntryValue;
import org.cnx.resourcemapping.LocationInformation;
import org.cnx.resourcemapping.Repository;
import org.cnx.resourcemapping.Resource;
import org.cnx.resourcemapping.Resources;
import org.cnx.servicedocument.AtomTextConstruct;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * TODO(arjuns) : Add tests for this. TODO(arjuns) : Add javadoc.
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomPubUtils {
    // Utility Class.
    private CnxAtomPubUtils() {
    }

    public static final String YES = "yes";
    public static final String NO = "no";

    // TODO(arjuns) : Fix URL to URI.
    /** Sub-domain for AtomPub relative to host. */
    public static final String ATOMPUB_URL_PREFIX = "atompub";

    /** String representation for latest version. */
    public static String LATEST_VERSION_STRING = "latest";

    /** Default new Version. */
    public static final VersionWrapper DEFAULT_VERSION = new VersionWrapper(0);

    /** Default Edit Version. */
    public static final VersionWrapper DEFAULT_EDIT_VERSION = new VersionWrapper(1);

    /** Latest version for any Module/collection. */
    public static final VersionWrapper LATEST_VERSION_WRAPPER = new VersionWrapper(
            LATEST_VERSION_STRING);

    /** Relation tag for Self links for CNX Resources/Modules/Collections. */
    public static final String LINK_RELATION_SELF_TAG = "self";

    /** Delimiter to connect Ids and Versions. */
    public static final String DELIMITER_ID_VERSION = ":";

    /**
     * Get AtomPubId from moduleId/collectionId and version.
     */
    public static String getAtomPubIdFromCnxIdAndVersion(IdWrapper cnxId, VersionWrapper version) {
        return cnxId.getId() + CnxAtomPubUtils.DELIMITER_ID_VERSION + version.toString();
    }

    // TODO(arjuns) : Make this config params.
    private static final BigDecimal RESOURCE_MAPPING_DOC_VERSION = new BigDecimal(1.0);
    private static final String REPOSITORY_ID = "cnx-repo";

    /**
     * Get AtomPub List of Contents from CNXMl and ResourceMappingDoc.
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws JDOMException
     * 
     *             TODO(arjuns) : Rename this function.
     */
    public static List<Content> getAtomPubListOfContent(String cnxmlDoc, String resourceMappingDoc)
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
    public static List<Content> getAtomPubListOfContentForCollectionEntry(String collXmlDoc)
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
     * 
     * @deprecated use {@link #getCnxmlFromModuleVersionEntry(Entry)}
     */
    @Deprecated
    public static String getCnxmlFromModuleEntryXml(String moduleEntryXml) throws JDOMException,
            IOException {
        return getDecodedChild("cnxml-doc", moduleEntryXml);
    }

    /**
     * Get CNXML-doc from ModuleEntry.
     * 
     * @param moduleVersionEntry ModuleEntry returned as part of the response. This method expects
     *            that moduleEntryXml is already decoded.
     * @return CNXML (response is already decoded).
     * @throws CnxException
     * 
     * TODO(arjuns) : Optimize this method.
     */
    public static String getCnxmlFromModuleVersionEntry(Entry moduleVersionEntry)
            throws JDOMException,
            IOException, CnxException {
        return getDecodedChild("cnxml-doc",
                PrettyXmlOutputter.prettyXmlOutputEntry(moduleVersionEntry));
    }

    /**
     * Get ResourceMapping doc from decoded ModuleEntry XML.
     * 
     * @param moduleEntryXml ModuleEntry returned as part of the response. This method expects that
     *            moduleEntryXml is already decoded.
     * @return ResourceMapping XML (response is already decoded).
     * @deprecated use {@link #getResourceMappingDocFromModuleEntry(Entry)}
     * 
     */
    @Deprecated
    public static String getResourceMappingDocFromModuleEntryXml(String moduleEntryXml)
            throws JDOMException, IOException {
        return getDecodedChild("resource-mapping-doc", moduleEntryXml);
    }

    /**
     * Get ResourceMapping doc from ModuleEntry.
     * 
     * @param moduleEntry ModuleEntry returned as part of the response. This method expects that
     *            moduleEntryXml is already decoded.
     * @return ResourceMapping XML (response is already decoded).
     * @throws CnxException
     * 
     * TODO(arjuns) : Optimize this method.
     */
    public static String getResourceMappingDocFromModuleEntry(Entry moduleEntry)
            throws JDOMException, IOException, CnxException {
        return getDecodedChild("resource-mapping-doc",
                PrettyXmlOutputter.prettyXmlOutputEntry(moduleEntry));
    }

    private static String getDecodedChild(String childElement, String moduleEntryXml)
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
     * Decode a 64 Bit encoded String.
     * 
     * @param encodedString 64bit encoded string.
     * @return Decoded string.
     * @throws UnsupportedEncodingException
     */
    public static String decodeFromBase64EncodedString(String encodedString)
            throws UnsupportedEncodingException {
        byte[] decodedBytes = decodeBase64EncodedStringToBytes(encodedString);
        return new String(decodedBytes, Charsets.UTF_8.displayName());
    }

    private static byte[] decodeBase64EncodedStringToBytes(String encodedString) {
        return Base64.decodeBase64(encodedString);
    }

    /**
     * Encode a String to a Base64-encoded string.
     * 
     * @param originalString : String to be encoded.
     * @return Base64-encoded String.
     * @throws UnsupportedEncodingException
     */
    public static String encodeToBase64EncodedString(String originalString)
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
    public static String getModuleEntryValue(String cnxml, String resourceMappingXml)
            throws JAXBException {
        org.cnx.resourceentry.ObjectFactory objectFactory =
                new org.cnx.resourceentry.ObjectFactory();
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
     * @throws JAXBException
     */
    // TODO(arjuns) : Move this to XmlPrinter.
    @SuppressWarnings("rawtypes")
    public static String jaxbObjectToString(Class rootClass, Object jaxbObject) 
            throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbObject.getClass());

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, Charsets.UTF_8.displayName());
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(jaxbObject, stringWriter);

        return stringWriter.toString();
    }

    /**
     * Get Collection XML from AtomPub Entry.
     */
    public static String getCollXmlDocFromAtomPubCollectionEntry(Entry apCollectionEntry)
            throws UnsupportedEncodingException {
        Content content = (Content) apCollectionEntry.getContents().get(0);
        return CnxAtomPubUtils.decodeFromBase64EncodedString(content.getValue());
    }

    /**
     * Converts a given XML to Atom Entry.
     * 
     * @param xml Input XML.
     * @return AtomEntry.
     * 
     * @throws IllegalArgumentException
     * @throws JDOMException
     * @throws IOException
     * @throws FeedException
     */
    public static Entry parseXmlToEntry(String xml) throws IllegalArgumentException,
            JDOMException, IOException, FeedException {
        InputStream is = new ByteArrayInputStream(xml.getBytes(Charsets.UTF_8.displayName()));
        return Atom10Parser.parseEntry(new InputStreamReader(is), null/* baseUri */);
    }

    /**
     * Get title from TextConstruct
     */
    public static String getTitleString(AtomTextConstruct titleTextConstruct) {
        Preconditions.checkArgument(titleTextConstruct.getContent().size() == 1);
        return (String) titleTextConstruct.getContent().get(0);
    }

    public static String getResourceMappingXmlFromResources(
            Map<String, IdWrapper> mapPrettyNameToResourceId) throws JAXBException {
        org.cnx.resourcemapping.ObjectFactory objectFactory =
                new org.cnx.resourcemapping.ObjectFactory();

        Resources resources = objectFactory.createResources();
        resources.setVersion(RESOURCE_MAPPING_DOC_VERSION);

        List<Resource> list = resources.getResource();

        for (String currPrettyName : mapPrettyNameToResourceId.keySet()) {
            Resource resourceFromEntry = objectFactory.createResource();
            list.add(resourceFromEntry);

            resourceFromEntry.setName(currPrettyName);

            Repository repository = objectFactory.createRepository();
            repository.setRepositoryId(REPOSITORY_ID);

            IdWrapper repoId = mapPrettyNameToResourceId.get(currPrettyName);
            repository.setResourceId(repoId.getId());

            LocationInformation locationInformation = objectFactory.createLocationInformation();
            locationInformation.setRepository(repository);
            resourceFromEntry.setLocationInformation(locationInformation);
        }

        return jaxbObjectToString(Resources.class, resources);
    }
}
