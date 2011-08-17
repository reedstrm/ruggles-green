package org.cnx.atompubclient;

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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.resourceentry.ResourceEntryValue;
import org.cnx.resourcemapping.LocationInformation;
import org.cnx.resourcemapping.ObjectFactory;
import org.cnx.resourcemapping.Repository;
import org.cnx.resourcemapping.Resource;
import org.cnx.resourcemapping.Resources;
import org.jdom.JDOMException;

import com.google.common.base.Preconditions;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.impl.Atom10Parser;
import com.sun.syndication.propono.atom.client.AtomClientFactory;
import com.sun.syndication.propono.atom.client.ClientAtomService;
import com.sun.syndication.propono.atom.client.ClientCollection;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.atom.client.ClientWorkspace;
import com.sun.syndication.propono.atom.client.CustomHttpClient;
import com.sun.syndication.propono.atom.client.NoAuthStrategy;
import com.sun.syndication.propono.utils.ProponoException;

/**
 * AtomPub client for CNX Repository.
 *
 * @author Arjun Satyapal
 */
public class CnxAtomPubClient {
    Logger logger = Logger.getLogger(CnxAtomPubClient.class.getName());

    // TODO(arjuns0 : Currently hardcoding these values.
    private final String REPOSITORY_ID = "cnx-repo";
    private final BigDecimal RESOURCE_MAPPING_DOC_VERSION = new BigDecimal(1.0);

    // TODO(arjuns) : Finalize on repositoryId. Hardcoding for the time being.
    private URL atomPubServerUrl;
    private ClientAtomService service;
    private ClientWorkspace workspace;
    private CustomHttpClient httpClient;
    private CnxAtomPubConstants constants;

    /**
     * Get workspace from CNX AtomPub Service Document.
     */
    public ClientWorkspace getWorkspace() {
        return workspace;
    }

    /**
     * Get AtomPub constants initialized to refer to a repository.
     */
    public CnxAtomPubConstants getConstants() {
        return constants;
    }

    /**
     * Construct a AtomPub client for CNX Repository.
     *
     * @param atomPubServerUrl : URL for CNX Repository AtomPub Service.
     * @throws ProponoException
     * @throws MalformedURLException
     */
    public CnxAtomPubClient(URL atomPubServerUrl) throws ProponoException, MalformedURLException {
        /*
         * rome library uses Apache Commons BeanUtils. With atom1.0 created data is optional. But
         * Beantuils fails when it is not set. So following is a way to bypass that.
         */
        java.util.Date defaultValue = null;
        DateConverter converter = new DateConverter(defaultValue);
        ConvertUtils.register(converter, java.util.Date.class);

        httpClient = new CustomHttpClient();
        constants = new CnxAtomPubConstants(atomPubServerUrl);

        URL serviceDocumentUri =
            new URL(atomPubServerUrl.toString() + CnxAtomPubConstants.SERVICE_DOCUMENT_PATH);

        service =
            AtomClientFactory.getAtomService(serviceDocumentUri.toString(), new NoAuthStrategy());

        @SuppressWarnings("unchecked")
        List<ClientWorkspace> listOfWorkspaces = service.getWorkspaces();
        Preconditions.checkArgument(1 == listOfWorkspaces.size(),
            "CNX ServiceDocument should have only one workspace.");
        workspace = listOfWorkspaces.get(0);
    }

    /**
     * Get AtomPub URL for CNX Repository. All AtomPub destinations are related to this.
     */
    public URL getAtomPubServerUrl() {
        return atomPubServerUrl;
    }

    /**
     * Get AtomPub collection for CNX Resources.
     */
    public ClientCollection getCollectionResource() {
        return getCollectionByTitle(CnxAtomPubConstants.COLLECTION_RESOURCE_TITLE);
    }

    /**
     * Get AtomPub collection for CNX Modules.
     */
    public ClientCollection getCollectionModule() {
        return getCollectionByTitle(CnxAtomPubConstants.COLLECTION_MODULE_TITLE);
    }

    /**
     * Get AtomPub collection for CNX Collections.
     */
    public ClientCollection getCollectionCnxCollection() {
        return getCollectionByTitle(CnxAtomPubConstants.COLLECTION_CNX_COLLECTION_TITLE);
    }

    /**
     * Get AtomPub collection from workspace via title.
     */
    private ClientCollection getCollectionByTitle(String title) {
        @SuppressWarnings("unchecked")
        List<ClientCollection> listOfCollections = workspace.getCollections();

        for (ClientCollection currCollection : listOfCollections) {
            if (currCollection.getTitle().equals(title)) {
                return currCollection;
            }
        }

        throw new IllegalArgumentException("Collection with Title " + title + " not found.");
    }

    /**
     * This is visible only for testing.
     */
    public ClientEntry createUploadUrl(String resourceName) throws ProponoException {
        ClientCollection collectionResource = getCollectionResource();
        ClientEntry entry = new ClientEntry(service, collectionResource);
        // TODO(arjuns) : Add test for this.
        entry.setTitle(resourceName);
        collectionResource.addEntry(entry);
        return entry;

    }

    /**
     * Upload file to blobstore.
     *
     * @param resourceName : Pretty name for the resource. This will be used to create the
     *            resourceMappingDoc.
     * @param file : File to be uploaded to CNX Repository.
     *
     *            TODO(arjuns) : Replace file with InputStream so that it can work on AppEngine.
     */
    public ClientEntry uploadFileToBlobStore(String resourceName, File file)
            throws ProponoException, HttpException, IOException {
        ClientEntry resourceEntry = createUploadUrl(resourceName);

        Link blobstoreUrl = getLinkForBlobStore(resourceEntry);

        URL postUrl = new URL(blobstoreUrl.getHrefResolved());
        postFileToBlobstore(postUrl, file);
        return resourceEntry;
    }

    public Link getLinkForResource(ClientEntry entry) {
        @SuppressWarnings("unchecked")
        List<Link> otherLinks = entry.getOtherLinks();

        for (Link currLink : otherLinks) {
            if (currLink.getRel().equals(CnxAtomPubConstants.REL_TAG_FOR_SELF_URL)) {
                return currLink;
            }
        }

        throw new IllegalStateException("Resource url not found in entry.");
    }

    public Link getLinkForBlobStore(ClientEntry entry) {
        @SuppressWarnings("unchecked")
        List<Link> otherLinks = entry.getOtherLinks();

        for (Link currLink : otherLinks) {
            if (currLink.getRel().equals(CnxAtomPubConstants.REL_TAG_FOR_BLOBSTORE_URL)) {
                return currLink;
            }
        }

        throw new IllegalStateException("Blobstore url not found in entry.");
    }

    /**
     * Upload file to Blobstore.
     *
     * @param blobstoreUrl where file needs to be uploaded.
     * @param file File to be uploaded to Blobstore.
     */
    void postFileToBlobstore(URL blobstoreUrl, File file) throws IOException {
        PostMethod postMethod = new PostMethod(blobstoreUrl.toString());
        Part[] parts = { new FilePart(file.getName(), file) };
        postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
        int status = httpClient.executeMethod(postMethod);
        // TODO(arjuns) : Confirm it will be always 302.
        // Preconditions.checkState(status == 302);
    }

    /**
     * Create New Module on CNX Repository.
     */
    public ClientEntry createNewModule() throws ProponoException {
        ClientCollection collectionModule = getCollectionModule();
        ClientEntry entry = new ClientEntry(service, collectionModule);
        collectionModule.addEntry(entry);
        return entry;
    }

    /**
     * Create New ModuleVersion on CNX Repository. Before first version can be created, a moduleId
     * must be obtained using {@link #createNewModule()}.
     *
     * @param moduleVersionEntry Entry returned as response for {@link #createNewModule}
     * @param cnxmlDoc CNXML Doc.
     * @param resourceMappingDoc XML for Resource Mapping.
     *
     * @throws JAXBException
     * @throws IOException
     * @throws JDOMException
     */
    public ClientEntry createNewModuleVersion(ClientEntry moduleVersionEntry, String cnxmlDoc,
            String resourceMappingDoc) throws ProponoException, JAXBException, JDOMException,
            IOException {
        // TODO(arjuns): rename AtomPubListOfContent to something better.
        moduleVersionEntry.setContents(constants.getAtomPubListOfContent(cnxmlDoc,
            resourceMappingDoc));
        moduleVersionEntry.update();

        String moduleId = CnxAtomPubConstants.getIdFromAtomPubId(moduleVersionEntry.getId());
        int currentVersion =
            Integer.parseInt(CnxAtomPubConstants
                .getVersionFromAtomPubId(moduleVersionEntry.getId()));
        // TODO(arjuns) : Validate this.
        String newVersion = Integer.toString(currentVersion);

        moduleVersionEntry.setId(CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(moduleId,
            newVersion));

        return moduleVersionEntry;
    }

    /**
     * Fetches Module Entry from CNX Repository for given ModuleId and Version. The AtomEntry will
     * have value which is representation for {@link ResourceEntryValue}
     *
     * @param moduleId
     * @param version
     */
    public Entry getModuleVersionEntry(String moduleId, String version) {
        URL moduleVersionUrl = constants.getModuleVersionAbsPath(moduleId, version);

        ClientCollection colModule = getCollectionModule();
        Entry getEntry = null;
        try {
            String encodedModuleVersionEntry =
                httpClient.getURLContentAsString(moduleVersionUrl.toString());
            getEntry =
                Atom10Parser.parseEntry(new StringReader(encodedModuleVersionEntry),
                    moduleVersionUrl.toString());
        } catch (Exception e) {
            // TODO(arjuns) : Handle exception.
            throw new RuntimeException(e);
        }

        return getEntry;
    }

    // TODO(arjuns) : Move this to common.
    public String getCnxml(String moduleId, String version) throws HttpException, IOException,
            JDOMException {
        return getCnxml(getModuleVersionEntry(moduleId, version));
    }

    // TODO(arjuns) : Move this to common.
    public String getCnxml(Entry moduleVersionEntry) throws JDOMException, IOException {
        String encodedModuleEntryValue = getContentFromEntry(moduleVersionEntry).getValue();
        String decodedModuleEntryValue =
            constants.decodeFrom64BitEncodedString(encodedModuleEntryValue);
        return constants.getCnxmlFromModuleEntryXml(decodedModuleEntryValue);
    }

    // TODO(arjuns) : Move this to common.
    public String getModuleVersionResourceMappingXml(String moduleId, String version)
            throws HttpException, JDOMException, IOException {
        return getResourceMappingXml(getModuleVersionEntry(moduleId, version));
    }

    // TODO(arjuns) : Move this to common.
    public String getResourceMappingXml(Entry moduleVersionEntry) throws JDOMException, IOException {
        String encodedModuleEntryValue = getContentFromEntry(moduleVersionEntry).getValue();
        String decodedModuleEntryValue =
            constants.decodeFrom64BitEncodedString(encodedModuleEntryValue);
        return constants.getResourceMappingDocFromModuleEntryXml(decodedModuleEntryValue);
    }

    /**
     * Get Content from AtomEntry.
     */
    public Content getContentFromEntry(Entry entry) {
        return (Content) entry.getContents().get(0);
    }

    /**
     * Provide ResourceMapping XML for uploaded resources. Users of CnxAtomPubClient are expected to
     * maintain a list of entries returned by CnxAtomPubClient.
     *
     * TODO(arjuns) : May be make CnxAtomPubClient statefule so that users of clients are not
     * expected to maintain list.
     *
     * @param resourceEntries List of resourceEntries returned by Client.
     */
    public String getResourceMappingFromResourceEntries(List<ClientEntry> resourceEntries)
            throws JAXBException {
        ObjectFactory objectFactory = new ObjectFactory();

        Resources resources = objectFactory.createResources();
        resources.setVersion(RESOURCE_MAPPING_DOC_VERSION);

        List<Resource> list = resources.getResource();

        for (ClientEntry currEntry : resourceEntries) {
            Resource resourceFromEntry = objectFactory.createResource();
            list.add(resourceFromEntry);

            resourceFromEntry.setName(currEntry.getTitle());

            Repository repository = objectFactory.createRepository();
            repository.setRepositoryId(REPOSITORY_ID);
            repository.setResourceId(currEntry.getId());

            LocationInformation locationInformation = objectFactory.createLocationInformation();
            locationInformation.setRepository(repository);

            resourceFromEntry.setLocationInformation(locationInformation);
        }

        return constants.jaxbObjectToString(Resources.class, resources);
    }

    /**
     * Create New CNX Collection on CNX Repository.
     */
    public ClientEntry createNewCollection() throws ProponoException {
        ClientCollection collectionCnxCollection = getCollectionCnxCollection();
        ClientEntry entry = new ClientEntry(service, collectionCnxCollection);
        collectionCnxCollection.addEntry(entry);
        return entry;
    }

    /**
     * Create New CNX Collection version on CNX Repository. Before first version can be created,
     * a collectionId must be obtained using {@link #createNewCollection}.
     *
     * @param collXmlVersionEntry Entry returned as response for {@link #createNewCollection}
     * @param collXmlDoc Collection XML Doc.
     *
     * @throws JAXBException
     * @throws IOException
     * @throws JDOMException
     */
    public ClientEntry createNewCollectionVersion(ClientEntry collXmlVersionEntry,
            String collXmlDoc) throws ProponoException, JAXBException, JDOMException, IOException {
        collXmlVersionEntry.setContents(constants
            .getAtomPubListOfContentForCollectionEntry(collXmlDoc));
        collXmlVersionEntry.update();

        String collectionId = CnxAtomPubConstants.getIdFromAtomPubId(collXmlVersionEntry.getId());
        int currentVersion =
            Integer.parseInt(CnxAtomPubConstants.getVersionFromAtomPubId(collXmlVersionEntry
                .getId()));

        String newVersion = Integer.toString(currentVersion);

        collXmlVersionEntry.setId(CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(collectionId,
            newVersion));

        return collXmlVersionEntry;
    }

    /**
     * Fetches Collection Entry from CNX Repository for given CollectionId and Version.
     *
     * @param collectionId
     * @param version
     */
    public Entry getCollectionVersionEntry(String collectionId, String version) {
        URL collectionVersionUrl = constants.getCollectionVersionAbsPath(collectionId, version);

        Entry getEntry = null;
        try {
            String collectionVersionEntry =
                httpClient.getURLContentAsString(collectionVersionUrl.toString());
            getEntry =
                Atom10Parser.parseEntry(new StringReader(collectionVersionEntry),
                    collectionVersionUrl.toString());
        } catch (Exception e) {
            // TODO(arjuns): Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return getEntry;
    }
}
