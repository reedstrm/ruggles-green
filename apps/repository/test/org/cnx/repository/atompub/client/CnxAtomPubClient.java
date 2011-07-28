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
package org.cnx.repository.atompub.client;

import static com.google.common.base.Preconditions.checkArgument;

import com.sun.syndication.propono.atom.client.AtomClientFactory;
import com.sun.syndication.propono.atom.client.ClientAtomService;
import com.sun.syndication.propono.atom.client.ClientWorkspace;
import com.sun.syndication.propono.atom.client.NoAuthStrategy;
import com.sun.syndication.propono.atom.common.Collection;
import com.sun.syndication.propono.utils.ProponoException;

import org.cnx.repository.atompub.utils.CnxAtomPubConstants;

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

    public CnxAtomPubClient(URL atomPubServerUrl) throws ProponoException, MalformedURLException {
        this.atomPubServerUrl = atomPubServerUrl;

        URL serviceDocumentUri =
            new URL(atomPubServerUrl.toString() + CnxAtomPubConstants.SERVICE_DOCUMENT_PATH);

        service =
            AtomClientFactory.getAtomService(serviceDocumentUri.toString(), new NoAuthStrategy());

        List<ClientWorkspace> listOfWorkspaces = service.getWorkspaces();
        checkArgument(1 == listOfWorkspaces.size(),
            "CNX ServiceDocument should have only one workspace.");
        workspace = listOfWorkspaces.get(0);
    }

    public URL getAtomPubServerUrl() {
        return atomPubServerUrl;
    }

    public ClientWorkspace getWorkSpace() {
        return workspace;
    }

    public Collection getCollectionResource() {
        return getCollectionByTitle(CnxAtomPubConstants.COLLECTION_RESOURCE_TITLE);
    }

    public Collection getCollectionModule() {
        return getCollectionByTitle(CnxAtomPubConstants.COLLECTION_MODULE_TITLE);
    }

    public Collection getCollectionCnxCollection() {
        return getCollectionByTitle(CnxAtomPubConstants.COLLECTION_CNX_COLLECTION_TITLE);
    }

    private Collection getCollectionByTitle(String title) {
        @SuppressWarnings("unchecked")
        List<Collection> listOfCollections = workspace.getCollections();

        for (Collection currCollection : listOfCollections) {
            if (currCollection.getTitle().equals(title)) {
                return currCollection;
            }
        }

        return null;
    }
}
