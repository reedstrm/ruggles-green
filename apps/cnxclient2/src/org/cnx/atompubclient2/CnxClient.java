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
package org.cnx.atompubclient2;

import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.sun.syndication.io.FeedException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.cnx.atompubclient2.cnxcollections.APCForCollection;
import org.cnx.atompubclient2.cnxcollections.APCForModules;
import org.cnx.atompubclient2.cnxcollections.APCForResources;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.atompub.CnxAtomPubConstants;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionVersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionWrapper;
import org.cnx.common.repository.atompub.objects.ModuleVersionWrapper;
import org.cnx.common.repository.atompub.objects.ModuleWrapper;
import org.cnx.common.repository.atompub.objects.ResourceInfoWrapper;
import org.cnx.common.repository.atompub.objects.ResourceWrapper;
import org.cnx.resourcemapping.LocationInformation;
import org.cnx.resourcemapping.ObjectFactory;
import org.cnx.resourcemapping.Repository;
import org.cnx.resourcemapping.Resource;
import org.cnx.resourcemapping.Resources;
import org.cnx.servicedocument.Service;
import org.jdom.JDOMException;

/**
 * Client for CNX Repository.
 * 
 * @author Arjun Satyapal
 */
public class CnxClient {
    // TODO(arjuns) : Currently hardcoding these values.
    private final String REPOSITORY_ID = "cnx-repo";
    private final BigDecimal RESOURCE_MAPPING_DOC_VERSION = new BigDecimal(1.0);

    // TODO(arjuns) : See how this value can be cached.
    private final Service serviceDocumentObject;
    private final APCForResources apcForResources;
    private final APCForModules apcForModules;
    private final APCForCollection apcForCollection;
    private final CnxAtomPubConstants constants;
    private final HttpClientWrapper httpClient;

    /**
     * Constructor for CNX Client.
     * 
     * TODO(arjuns): Change atomPubUrl to URI.
     * 
     * @param atomPubUrl AtomPub URI for CNX AtomPub Service.
     * @throws IOException
     * @throws URISyntaxException
     * @throws JAXBException
     * @throws CnxException
     */
    public CnxClient(URL atomPubUrl) throws IOException, URISyntaxException, JAXBException,
            CnxException {
        constants = new CnxAtomPubConstants(atomPubUrl);
        httpClient = HttpClientWrapper.getHttpClient();

        String serviceDocumentXml = getServiceDocumentXml();

        JAXBContext jaxbContext = JAXBContext.newInstance(Service.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        serviceDocumentObject =
                (Service) unmarshaller.unmarshal(new StringReader(serviceDocumentXml));
        apcForResources = new APCForResources(getServiceDocumentObject());
        apcForModules = new APCForModules(getServiceDocumentObject());
        apcForCollection = new APCForCollection(getServiceDocumentObject());
    }

    public CnxAtomPubConstants getConstants() {
        return constants;
    }

    /**
     * Get JAXB Generated Java Representation for Service Document.
     * 
     * @return Java representation for Service Document.
     */
    public Service getServiceDocumentObject() {
        return serviceDocumentObject;
    }

    /**
     * Fetch AtomPub ServiceDocument from CNX Repository.
     * 
     * @return XML Representation for ServiceDocument.
     * @throws IOException
     * @throws URISyntaxException
     * @throws CnxException
     */
    private String getServiceDocumentXml() throws IOException, URISyntaxException, CnxException {
        HttpGet httpGet = new HttpGet(new URI(constants.getServiceDocumentAbsPath2()));
        HttpResponse httpResponse = httpClient.execute(httpGet);
        String serviceDocumentXml =
                CharStreams.toString(new InputStreamReader(httpResponse.getEntity().getContent()));
        return serviceDocumentXml;
    }

    public ResourceWrapper createResource() throws URISyntaxException,
            IllegalArgumentException,
            IOException, JDOMException, FeedException, CnxException {
        return apcForResources.createResource();
    }

    public ResourceWrapper createResourceForMigration(IdWrapper id)
            throws IllegalArgumentException, IOException, JDOMException, FeedException,
            URISyntaxException, CnxException {
        return apcForResources.createResourceForMigration(id);
    }

    /**
     * Upload Resource to CNX Repository.
     * 
     * @param uploadUri URL where file should be uploaded.
     * @param contentType ContentType that client wants CNX Repository to set as header at the time
     *            of serving the file.
     * @param fileName Name that client wants to be stored as on the server side. If fileName is
     *            null then Name from fileSystem will be used.
     * @param file File handle.
     * @throws IOException
     * @throws CnxException
     */
    public void uploadResource(final URI uploadUri, final String contentType,
            @Nullable final String fileName, final File file) throws IOException, CnxException {
        Preconditions.checkNotNull(contentType, "Content-Type cannot be null.");
        String uploadFileName = fileName == null ? file.getName() : fileName;

        HttpPost httpPost = new HttpPost(uploadUri);
        FileBody fileBody = new FileBody(file, uploadFileName, contentType, null /* charset */);

        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("bin", fileBody);

        httpPost.setEntity(reqEntity);

        HttpResponse response = HttpClientWrapper.getHttpClient().execute(httpPost);
        HttpEntity resEntity = response.getEntity();

        // TODO(arjuns) : Add more checks here.
    }

    /**
     * Get resource from CNX Server as InputStream.
     * 
     * @param id Id for resource that needs to be downloaded.
     * 
     * @return InputStream for the resource.
     * @throws IOException
     * @throws URISyntaxException
     * @throws CnxException
     */
    public InputStream getResource(IdWrapper id) throws IOException, URISyntaxException,
            CnxException {
        return apcForResources.getResourceAsStream(id);
    }

    public ResourceInfoWrapper getResourceInformation(IdWrapper id)
            throws IllegalArgumentException, URISyntaxException, IOException, JDOMException,
            FeedException, CnxException {
        return apcForResources.getResourceInformation(id);
    }

    /**
     * Provide ResourceMapping XML for uploaded resources. Users of CnxAtomPubClient are expected to
     * maintain a list of ResourceWrappers returned by CnxClient.
     * 
     * @param prettyNameToResourceIdMap Map whose keys are PrettyNames, and Values are Ids of
     *            Resources.
     */
    public String getResourceMappingXml(Map<String, IdWrapper> prettyNameToResourceIdMap)
            throws JAXBException {
        ObjectFactory objectFactory = new ObjectFactory();
        Resources resources = objectFactory.createResources();
        resources.setVersion(RESOURCE_MAPPING_DOC_VERSION);

        List<Resource> list = resources.getResource();

        for (String prettyName : prettyNameToResourceIdMap.keySet()) {
            Resource resourceFromEntry = objectFactory.createResource();
            list.add(resourceFromEntry);

            resourceFromEntry.setName(prettyName);

            Repository repository = objectFactory.createRepository();
            repository.setRepositoryId(REPOSITORY_ID);

            IdWrapper idWrapper = prettyNameToResourceIdMap.get(prettyName);
            Preconditions.checkArgument(idWrapper.getType() == IdWrapper.Type.RESOURCE);
            repository.setResourceId(idWrapper.getId());

            LocationInformation locationInformation = objectFactory.createLocationInformation();
            locationInformation.setRepository(repository);

            resourceFromEntry.setLocationInformation(locationInformation);
        }

        return CnxAtomPubUtils.jaxbObjectToString(Resources.class, resources);
    }

    /*
     * Methods related to Module.
     */

    public ModuleWrapper createModule() throws IllegalArgumentException, IOException,
            JDOMException, FeedException, URISyntaxException, CnxException {
        return apcForModules.createModule();
    }

    public ModuleWrapper createModuleForMigration(IdWrapper id) throws
            IllegalArgumentException,
            IOException,
            JDOMException, FeedException, URISyntaxException, CnxException {
        return apcForModules.createModuleForMigration(id);
    }

    public ModuleWrapper createModuleVersion(IdWrapper id, VersionWrapper newVersion,
            String cnxml, String resourceMappingXml) throws IllegalArgumentException,
            URISyntaxException, IOException, CnxException, JAXBException,
            JDOMException, FeedException {
        URL editUrl = getConstants().getModuleVersionAbsPath(id, newVersion);
        return createModuleVersion(editUrl.toURI(), cnxml, resourceMappingXml);
    }

    public ModuleWrapper createModuleVersion(URI editUri, String cnxml, String resourceMappingXml)
            throws IllegalArgumentException, JDOMException, IOException, CnxException,
            JAXBException, FeedException, URISyntaxException {
        return apcForModules.createModuleVersion(editUri, cnxml, resourceMappingXml);
    }

    public ModuleVersionWrapper getModuleVersion(IdWrapper id, VersionWrapper version)
            throws IllegalStateException, IllegalArgumentException, IOException,
            URISyntaxException, JDOMException, FeedException, CnxException {
        return apcForModules.getModuleVersion(id, version);
    }

    /*
     * Methods related to Collection.
     */
    public CollectionWrapper createCollection() throws IllegalArgumentException, IOException,
            JDOMException, FeedException, URISyntaxException, CnxException {
        return apcForCollection.createCollection();
    }

    public CollectionWrapper createCollectionForMigration(IdWrapper id) throws
            IllegalArgumentException,
            IOException,
            JDOMException, FeedException, URISyntaxException, CnxException {
        return apcForCollection.createCollectionForMigration(id);
    }

    public CollectionWrapper createCollectionVersion(IdWrapper id, VersionWrapper newVersion,
            String collectionXml) throws IllegalArgumentException,
            URISyntaxException, IOException, CnxException, JAXBException,
            JDOMException, FeedException {
        URL editUrl = getConstants().getModuleVersionAbsPath(id, newVersion);
        return createCollectionVersion(editUrl.toURI(), collectionXml);
    }

    public CollectionWrapper createCollectionVersion(URI editUri, String collectionXml)
            throws IllegalArgumentException, JDOMException, IOException, CnxException,
            JAXBException, FeedException, URISyntaxException {
        return apcForCollection.createCollectionVersion(editUri, collectionXml);
    }

    public CollectionVersionWrapper getCollectionVersion(IdWrapper id, VersionWrapper version)
            throws IllegalStateException, IllegalArgumentException, IOException,
            URISyntaxException, JDOMException, FeedException, CnxException {
        return apcForCollection.getCollectionVersion(id, version);
    }
}
