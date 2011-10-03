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
package org.cnx.atompubclient;

import static org.cnx.atompubclient.CnxClientUtils.getHttpClient;

import com.google.common.base.Preconditions;

import com.google.common.io.CharStreams;
import com.sun.syndication.io.FeedException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.cnx.atompubclient.cnxcollections.APCForResources;
import org.cnx.common.repository.atompub.CnxAtomPubConstants;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.objects.ResourceInfoWrapper;
import org.cnx.common.repository.atompub.objects.ResourceWrapper;
import org.cnx.servicedocument.Service;
import org.jdom.JDOMException;

/**
 * Client for CNX Repository.
 * 
 * @author Arjun Satyapal
 */
public class CnxClient {
    // TODO(arjuns) : See how this value can be cached.
    private final Service serviceDocumentObject;
    private final APCForResources apcForResources;
    // private final APCForModules apcForModules;
    private final CnxAtomPubConstants constants;
    private final HttpClient httpClient;

    /**
     * Constructor for CNX Client.
     * 
     * TODO(arjuns): Change atomPubUrl to URI.
     * 
     * @param atomPubUrl AtomPub URI for CNX AtomPub Service.
     * @throws IOException
     * @throws URISyntaxException
     * @throws JAXBException
     */
    public CnxClient(URL atomPubUrl) throws IOException, URISyntaxException, JAXBException {
        constants = new CnxAtomPubConstants(atomPubUrl);
        httpClient = CnxClientUtils.getHttpClient();

        String serviceDocumentXml = getServiceDocumentXml();

        JAXBContext jaxbContext = JAXBContext.newInstance(Service.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        serviceDocumentObject =
                (Service) unmarshaller.unmarshal(new StringReader(serviceDocumentXml));
        apcForResources = new APCForResources(getServiceDocumentObject());
        // apcForModules = new APCForModules(getServiceDocumentObject());
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
     */
    private String getServiceDocumentXml() throws IOException, URISyntaxException {
        HttpGet httpGet = new HttpGet(new URI(constants.getServiceDocumentAbsPath2()));
        HttpResponse httpResponse = httpClient.execute(httpGet);
        String serviceDocumentXml =
                CharStreams.toString(new InputStreamReader(httpResponse.getEntity().getContent()));
        return serviceDocumentXml;
    }

    public ResourceWrapper createNewResource() throws URISyntaxException,
            IllegalArgumentException,
            IOException, JDOMException, FeedException {
        return apcForResources.createNewResource();
    }

    public ResourceWrapper createNewResourceForMigration(IdWrapper id)
            throws IllegalArgumentException, IOException, JDOMException, FeedException,
            URISyntaxException {
        return apcForResources.createNewResourceForMigration(id);
    }

    /**
     * Upload Resource to CNX Repository.
     * 
     * @param uploadUri URL where file should be uploaded.
     * @param mediaType ContentType that client wants CNX Repository to set as header at the time of
     *            serving the file.
     * @param fileName Name that client wants to be stored as on the server side. If fileName is
     *            null then Name from fileSystem will be used.
     * @param file File handle.
     * @throws IOException
     */
    public void uploadResource(final URI uploadUri, final String contentType,
            @Nullable final String fileName, final File file) throws IOException {
        Preconditions.checkNotNull(contentType, "Content-Type cannot be null.");
        String uploadFileName = fileName == null ? file.getName() : fileName;

        HttpPost httpPost = new HttpPost(uploadUri);
        FileBody fileBody = new FileBody(file, uploadFileName, contentType, null /* charset */);

        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("bin", fileBody);

        httpPost.setEntity(reqEntity);

        HttpResponse response = getHttpClient().execute(httpPost);
        HttpEntity resEntity = response.getEntity();

        if (resEntity != null) {
            System.out.println("Response content length: " + resEntity.getContentLength());
        }

        System.out.println(response.getStatusLine());
    }

    /**
     * Get resource from CNX Server as InputStream.
     * 
     * @param id Id for resource that needs to be downloaded.
     * 
     * @return InputStream for the resource.
     * @throws IOException
     * @throws URISyntaxException
     */
    public InputStream getResource(IdWrapper id) throws IOException, URISyntaxException {
        return apcForResources.getResourceAsStream(id);
    }

    public ResourceInfoWrapper getResourceInformation(IdWrapper id)
            throws IllegalArgumentException, URISyntaxException, IOException, JDOMException,
            FeedException {
        return apcForResources.getResourceInformation(id);
    }

    // public ModuleWrapper createNewModule() throws IllegalArgumentException, IOException,
    // JDOMException, FeedException, URISyntaxException {
    // return apcForModules.createNewModule();
    // }
    //
    // public ModuleWrapper createNewModuleForMigration(IdWrapper id) throws
    // IllegalArgumentException,
    // IOException,
    // JDOMException, FeedException, URISyntaxException {
    // return apcForModules.createNewModuleForMigration(id);
    // }
    //
    // public ModuleVersionWrapper createNewModuleVersion(IdWrapper id, VersionWrapper newVersion,
    // String cnxml, String resourceMappingXml) throws IllegalArgumentException,
    // URISyntaxException, JDOMException, IOException, CnxException, JAXBException,
    // FeedException {
    // apcForModules.createNewModuleVersion(id, newVersion, cnxml, resourceMappingXml);
    // return null;
    // }
}
