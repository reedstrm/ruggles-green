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


import com.google.common.base.Preconditions;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.impl.Atom10Parser;
import com.sun.syndication.propono.atom.client.AtomClientFactory;
import com.sun.syndication.propono.atom.client.ClientAtomService;
import com.sun.syndication.propono.atom.client.ClientCollection;
import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.atom.client.ClientWorkspace;
import com.sun.syndication.propono.atom.client.NoAuthStrategy;
import com.sun.syndication.propono.utils.ProponoException;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 *
 * @author Arjun Satyapal
 */
public class CnxAtomPubClient {
    private URL atomPubServerUrl;
    private ClientAtomService service;
    private ClientWorkspace workspace;
    private HttpClient httpClient;
    private CnxAtomPubConstants constants;

    public ClientAtomService getService() {
        return service;
    }

    public ClientWorkspace getWorkspace() {
        return workspace;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public CnxAtomPubConstants getConstants() {
        return constants;
    }

    public CnxAtomPubClient(URL atomPubServerUrl) throws ProponoException, MalformedURLException {
        /*
         * rome library uses Apache Commons BeanUtils. With atom1.0 created data is optoinal. But
         * Beantuils fails when it is not set. So following is a way to bypass that.
         */
        java.util.Date defaultValue = null;
        DateConverter converter = new DateConverter(defaultValue);
        ConvertUtils.register(converter, java.util.Date.class);

        httpClient = new HttpClient();
        constants =
            new CnxAtomPubConstants(atomPubServerUrl.toString(), atomPubServerUrl.getPort());

        this.atomPubServerUrl = atomPubServerUrl;

        URL serviceDocumentUri =
            new URL(atomPubServerUrl.toString() + CnxAtomPubConstants.SERVICE_DOCUMENT_PATH);

        service =
            AtomClientFactory.getAtomService(serviceDocumentUri.toString(), new NoAuthStrategy());

        List<ClientWorkspace> listOfWorkspaces = service.getWorkspaces();
        Preconditions.checkArgument(1 == listOfWorkspaces.size(),
            "CNX ServiceDocument should have only one workspace.");
        workspace = listOfWorkspaces.get(0);
    }

    public URL getAtomPubServerUrl() {
        return atomPubServerUrl;
    }

    public ClientWorkspace getWorkSpace() {
        return workspace;
    }

    public ClientCollection getCollectionResource() {
        return getCollectionByTitle(CnxAtomPubConstants.COLLECTION_RESOURCE_TITLE);
    }

    public ClientCollection getCollectionModule() {
        return getCollectionByTitle(CnxAtomPubConstants.COLLECTION_MODULE_TITLE);
    }

    public ClientCollection getCollectionCnxCollection() {
        return getCollectionByTitle(CnxAtomPubConstants.COLLECTION_CNX_COLLECTION_TITLE);
    }

    private ClientCollection getCollectionByTitle(String title) {
        @SuppressWarnings("unchecked")
        List<ClientCollection> listOfCollections = workspace.getCollections();

        for (ClientCollection currCollection : listOfCollections) {
            if (currCollection.getTitle().equals(title)) {
                return currCollection;
            }
        }

        return null;
    }

    /**
     * This is visible only for testing.
     */
    public ClientEntry createUploadUrl() throws ProponoException {
        ClientCollection collectionResource = getCollectionResource();
        ClientEntry entry = new ClientEntry(service, collectionResource);
        collectionResource.addEntry(entry);
        return entry;
    }

    public ClientEntry uploadFileToBlobStore(File file) throws ProponoException, HttpException,
            IOException {
        ClientEntry resourceEntry = createUploadUrl();

        @SuppressWarnings("unchecked")
        List<Link> otherLinks = resourceEntry.getOtherLinks();
        // TODO(arjuns) : Create a function to extract out blobstore URL.
        uploadFileToBlobStore(otherLinks.get(0).getHrefResolved(), file);

        return resourceEntry;
    }

    public void uploadFileToBlobStore(String blobstoreUrl, File file) throws HttpException, IOException {
        PostMethod postMethod = new PostMethod(blobstoreUrl);
        Part[] parts = { new FilePart(file.getName(), file) };
        postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
        int status = httpClient.executeMethod(postMethod);
        // TODO(arjuns) : Confirm it will be always 302.
        // Preconditions.checkState(status == 302);
    }

    public ClientEntry createNewModule() throws ProponoException {
        ClientCollection collectionModule = getCollectionModule();
        ClientEntry entry = new ClientEntry(service, collectionModule);
        collectionModule.addEntry(entry);
        return entry;
    }

    public ClientEntry createNewModuleVersion(ClientEntry moduleEntry, String cnxmlDoc,
            String resourceMappingDoc) throws ProponoException {
        moduleEntry.setContents(constants.getAtomPubListOfContent(cnxmlDoc, resourceMappingDoc));
        moduleEntry.update();

        String moduleId = CnxAtomPubConstants.getIdFromAtomPubId(moduleEntry.getId());
        // TODO(arjuns) : Remove this hardcoding of +1.
        int currentVersion =
            Integer.parseInt(CnxAtomPubConstants.getVersionFromAtomPubId(moduleEntry.getId()));

        String newVersion = Integer.toString(currentVersion + 1);

        moduleEntry
            .setId(CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(moduleId, newVersion));

        return moduleEntry;
    }

    public Entry getModuleVersion(String moduleId, String version) throws HttpException,
            IOException {
        URL moduleVersionUrl = constants.getModuleVersionAbsPath(moduleId, version);
        GetMethod getMethod = new GetMethod(moduleVersionUrl.toString());

        int status = httpClient.executeMethod(getMethod);
        // TODO(arjuns) : check the status.
        String response = getMethod.getResponseBodyAsString();

        Entry getEntry = null;
        try {
            getEntry = Atom10Parser.parseEntry(new StringReader(response), null);
        } catch (IllegalArgumentException e1) {
            // TODO(arjuns): Auto-generated catch block
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            // TODO(arjuns): Auto-generated catch block
            e1.printStackTrace();
        } catch (JDOMException e1) {
            // TODO(arjuns): Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO(arjuns): Auto-generated catch block
            e1.printStackTrace();
        } catch (FeedException e1) {
            // TODO(arjuns): Auto-generated catch block
            e1.printStackTrace();
        }

        return getEntry;
    }
}