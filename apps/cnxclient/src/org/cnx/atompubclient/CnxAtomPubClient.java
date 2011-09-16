/*
 * Copyright (C) 2011 The CNX Authors.
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
package org.cnx.atompubclient;

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
import com.sun.syndication.propono.utils.Utilities;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.cnx.exceptions.CnxConflictException;
import org.cnx.exceptions.CnxException;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.CnxAtomPubUtils;
import org.cnx.repository.atompub.IdWrapper;
import org.cnx.repository.atompub.ServletUris;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.resourceentry.ResourceEntryValue;
import org.cnx.resourcemapping.LocationInformation;
import org.cnx.resourcemapping.ObjectFactory;
import org.cnx.resourcemapping.Repository;
import org.cnx.resourcemapping.Resource;
import org.cnx.resourcemapping.Resources;
import org.jdom.JDOMException;

/**
 * AtomPub client for CNX Repository. This client is thread safe.
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomPubClient {
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
     * Get ClientAtomService.
     */
    public ClientAtomService getService() {
        return service;
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
                new URL(atomPubServerUrl.toString()
                        + ServletUris.ServiceDocument.SERVICE_DOCUMENT_SERVLET);

        service =
                AtomClientFactory.getAtomService(serviceDocumentUri.toString(),
                        new NoAuthStrategy());

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
        return getCollectionByTitle(CnxAtomPubUtils.COLLECTION_RESOURCE_TITLE);
    }

    /**
     * Get AtomPub collection for CNX Modules.
     */
    public ClientCollection getCollectionModule() {
        return getCollectionByTitle(CnxAtomPubUtils.COLLECTION_MODULE_TITLE);
    }

    /**
     * Get AtomPub collection for CNX Collections.
     */
    public ClientCollection getCollectionCnxCollection() {
        return getCollectionByTitle(CnxAtomPubUtils.COLLECTION_CNX_COLLECTION_TITLE);
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
     * @param resourceEntry Entry of Resource, which contains link to Blobstore.
     * @param resourceName : Pretty name for the resource. This will be used to create the
     *            resourceMappingDoc.
     * @param file : File to be uploaded to CNX Repository.
     * 
     *            TODO(arjuns) : Replace file with InputStream so that it can work on AppEngine.
     */
    public void uploadFileToBlobStore(ClientEntry resourceEntry, String resourceName, File file)
            throws HttpException, IOException {
        Link blobstoreUrl = CnxClientUtils.getBlobstoreUri(resourceEntry);
        URL postUrl = new URL(blobstoreUrl.getHrefResolved());
        postFileToBlobstore(postUrl, file);
    }

    /**
     * Create New Resource on CNX Repository.
     */
    public ClientEntry createNewResource() throws ProponoException {
        ClientCollection collectionResource = getCollectionResource();
        ClientEntry entry = new ClientEntry(service, collectionResource);
        collectionResource.addEntry(entry);
        return entry;
    }

    /**
     * Create New Module on CNX Repository for Migration.
     * 
     * @param resourceId Original CNX Module id on cnx.org.
     * @throws IOException
     * @throws ProponoException
     * @throws FeedException
     * @throws JDOMException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws CnxException
     */
    public ClientEntry createNewResourceForMigration(IdWrapper resourceId) throws IOException,
            ProponoException, JDOMException, FeedException, IllegalAccessException,
            InvocationTargetException, CnxException {
        // TODO(arjuns) : Create a function for URL appending.
        // TODO(arjuns) : move this url to constants.
        String migrationUrlForResource =
                constants.getAtomPubRestUrl().toString() + "/resource/migration/"
                        + resourceId.getId();
        return handlePostForMigration(migrationUrlForResource);
    }

    /**
     * Upload file to Blobstore.
     * 
     * @param blobstoreUrl where file needs to be uploaded.
     * @param file File to be uploaded to Blobstore.
     */
    private void postFileToBlobstore(URL blobstoreUrl, File file) throws IOException {
        PostMethod postMethod = new PostMethod(blobstoreUrl.toString());
        Part[] parts = { new FilePart(file.getName(), file), };
        postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
        // int status = httpClient.executeMethod(postMethod);
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
     * Create New Module on CNX Repository for Migration.
     * 
     * @param cnxModuleId Original CNX Module id on cnx.org.
     * @throws IOException
     * @throws ProponoException
     * @throws FeedException
     * @throws JDOMException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws CnxConflictException
     */
    public ClientEntry createNewModuleForMigration(IdWrapper cnxModuleId) throws IOException,
            ProponoException, IllegalArgumentException, JDOMException, FeedException,
            IllegalAccessException, InvocationTargetException, CnxConflictException {
        // TODO(arjuns) : Create a function for URL appending.
        // TODO(arjuns) : move this url to constants.
        String migrationUrlForModule =
                constants.getAtomPubRestUrl().toString() + "/module/migration/"
                        + cnxModuleId.getId();
        return handlePostForMigration(migrationUrlForModule);
    }

    /**
     * Create New ModuleVersion on CNX Repository.
     * 
     * NOTE : Before first version can be created, a moduleId must be obtained using
     * {@link #createNewModule()}.
     * 
     * @param moduleVersionEntry Module Version entry to be updated. On success, moduleVersionEntry
     *            will be updated with response returned from Repository.
     * @param cnxmlDoc CNXML Doc.
     * @param resourceMappingXml XML for Resource Mapping.
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws JDOMException
     */
    public void createNewModuleVersion(ClientEntry moduleVersionEntry, final String cnxmlDoc,
            final String resourceMappingXml) throws ProponoException, JAXBException, JDOMException,
            IOException {
        moduleVersionEntry.setContents(CnxAtomPubUtils.getAtomPubListOfContent(cnxmlDoc,
                resourceMappingXml));
        moduleVersionEntry.update();
    }

    /**
     * Fetches Module Entry from CNX Repository for given ModuleId and Version. The AtomEntry will
     * have value which is representation for {@link ResourceEntryValue}
     * 
     * @param moduleId
     * @param version
     * @throws ProponoException
     */
    public ClientEntry getModuleVersionEntry(IdWrapper moduleId, VersionWrapper version)
            throws ProponoException {
        URL moduleVersionUrl = constants.getModuleVersionAbsPath(moduleId, version);

        return service.getEntry(moduleVersionUrl.toString());
    }

    /**
     * Get CNXML from AtomEntry for a Module-Version. TODO(arjuns) : Move this to utility class.
     * 
     * @param moduleVersionEntry Atom Entry returned by Server for a particular module-version.
     * @return Returns CNXML (response is already decoded).
     */
    public String getCnxml(ClientEntry moduleVersionEntry) throws JDOMException, IOException {
        String encodedModuleEntryValue = getContentFromEntry(moduleVersionEntry).getValue();
        String decodedModuleEntryValue =
                CnxAtomPubUtils.decodeFromBase64EncodedString(encodedModuleEntryValue);
        return CnxAtomPubUtils.getCnxmlFromModuleEntryXml(decodedModuleEntryValue);
    }

    /**
     * Get ResourceMapping XML from AtomEntry for a ModuleVersion.
     * 
     */
    public String getModuleVersionResourceMappingXml(IdWrapper moduleId, VersionWrapper version)
            throws HttpException, JDOMException, IOException, ProponoException {
        return getResourceMappingXml(getModuleVersionEntry(moduleId, version));
    }

    public String getResourceMappingXml(ClientEntry moduleVersionEntry) throws JDOMException,
            IOException {
        String encodedModuleEntryValue = getContentFromEntry(moduleVersionEntry).getValue();
        String decodedModuleEntryValue =
                CnxAtomPubUtils.decodeFromBase64EncodedString(encodedModuleEntryValue);
        return CnxAtomPubUtils.getResourceMappingDocFromModuleEntryXml(decodedModuleEntryValue);
    }

    /**
     * Get Content from AtomEntry.
     */
    public Content getContentFromEntry(ClientEntry entry) {
        return entry.getContent();
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

        return CnxAtomPubUtils.jaxbObjectToString(Resources.class, resources);
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
     * Create New Module on CNX Repository for Migration.
     * 
     * @param cnxCollectionId Original CNX Module id on cnx.org.
     * @throws IOException
     * @throws ProponoException
     * @throws FeedException
     * @throws JDOMException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws CnxConflictException
     */
    public ClientEntry createNewCollectionForMigration(IdWrapper cnxCollectionId)
            throws IOException, ProponoException, IllegalArgumentException, JDOMException,
            FeedException, IllegalAccessException, InvocationTargetException, CnxConflictException {
        // TODO(arjuns) : Create a function for URL appending.
        // TODO(arjuns) : move this url to constants.
        // TODO(arjuns): Handle exceptions.
        String migrationUrlForCollection =
                constants.getAtomPubRestUrl().toString() + "/collection/migration/"
                        + cnxCollectionId.getId();
        return handlePostForMigration(migrationUrlForCollection);
    }

    @SuppressWarnings("deprecation")
    private ClientEntry handlePostForMigration(String migrationUrl) throws IOException,
            ProponoException, JDOMException, FeedException, IllegalAccessException,
            InvocationTargetException, CnxConflictException {
        PostMethod postMethod = new PostMethod(migrationUrl);

        postMethod.setRequestEntity(new StringRequestEntity(""));

        httpClient.executeMethod(postMethod);
        int code = postMethod.getStatusCode();

        // TODO(arjuns) : Refactor following method to customHttpClient
        InputStream is = postMethod.getResponseBodyAsStream();
        code = postMethod.getStatusCode();

        if (code == Status.CONFLICT.getStatusCode()) {
            throw new CnxConflictException("ServerSide conflict for URL : " + migrationUrl, null /* throwable */);
        }
        if (code != 200 && code != 201) {
            throw new ProponoException("ERROR HTTP status=" + code + " : "
                    + Utilities.streamToString(is));
        }
        Entry romeEntry = Atom10Parser.parseEntry(new InputStreamReader(is), null /* baseUri */);
        BeanUtils.copyProperties(this, romeEntry);

        return new ClientEntry(service, null, romeEntry, false);
    }

    /**
     * Create New CNX Collection version on CNX Repository.
     * 
     * NOTE : If it is first version, then a collectionId must be obtained using
     * {@link #createNewCollection}.
     * 
     * @param collectionVersionEntry Collection Entry that needs to be published. On success,
     *            collectionVersionEntry will be updated with response returned from Repository.
     * @param collXml Collection XML Doc.
     * 
     * @throws JAXBException
     * @throws IOException
     * @throws JDOMException
     */
    public void
            createNewCollectionVersion(ClientEntry collectionVersionEntry, final String collXml)
                    throws ProponoException, JAXBException, JDOMException, IOException {
        collectionVersionEntry.setContents(CnxAtomPubUtils
                .getAtomPubListOfContentForCollectionEntry(collXml));
        collectionVersionEntry.update();
    }

    /**
     * Fetches Collection Entry from CNX Repository for given CollectionId and Version.
     * 
     * @param collectionId
     * @param version
     * @throws ProponoException
     */
    public ClientEntry getCollectionVersionEntry(IdWrapper collectionId, VersionWrapper version)
            throws ProponoException {
        URL collectionVersionUrl = constants.getCollectionVersionAbsPath(collectionId, version);

        return service.getEntry(collectionVersionUrl.toString());
    }
}
