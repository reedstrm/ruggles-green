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
package org.cnx.atompubclient2.cnxcollections;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.cnx.common.repository.atompub.CnxAtomPubCollectionEnum;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.CommonUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.ServletUris;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.servicedocument.Collection;
import org.cnx.servicedocument.Service;
import org.cnx.servicedocument.Workspace;

/**
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractAPC extends Collection {
    private final Service serviceDocumentObject;
    private final Collection apc;
    private final CnxAtomPubCollectionEnum apcType;
    private final URI apcUri;

    public AbstractAPC(Service serviceDocumentObject, CnxAtomPubCollectionEnum apcType)
            throws URISyntaxException {
        checkArgument(serviceDocumentObject.getWorkspace().size() == 1);
        Workspace workspace = serviceDocumentObject.getWorkspace().get(0);
        checkArgument(workspace.getCollection().size() == 3);

        this.serviceDocumentObject = serviceDocumentObject;
        this.apc = getApc(workspace.getCollection(), apcType);
        this.apcType = apcType;
        apcUri = new URI(apc.getHref());
    }

    public Service getServiceDocumentObject() {
        return serviceDocumentObject;
    }

    /** Get AtomPub Collection. */
    public Collection getApc() {
        return apc;
    }

    public URI getAPCUri() {
        return apcUri;
    }

    /**
     * Get AtomPubResource URI for Migration.
     * 
     * @param id id for AtomPub Resource
     * @return URI where client should do post to create new ID for migration.
     * @throws URISyntaxException
     */
    protected URI getAPRUriForMigration(IdWrapper id) throws URISyntaxException {
        URI tail = new URI(ServletUris.MIGRATION + "/" + id.getId());
        return CommonUtils.appendUri(getAPCUri(), tail);
    }

    /**
     * Get AtomPubResource URI for a Version.
     * 
     * @param id id for AtomPub Resource
     * @param version version for AtomPub Resource.
     * @return URI Uri for AtomPubResource Version.
     * 
     * @throws URISyntaxException
     */
    protected URI getAPRUriForMigration(IdWrapper id, VersionWrapper version)
            throws URISyntaxException {
        Preconditions.checkArgument(id.getType() != IdWrapper.Type.RESOURCE);
        URI tail = new URI(id.getId() + "/" + version.toString());
        return CommonUtils.appendUri(getAPCUri(), tail);
    }

    /**
     * URI for downloading AtomPubResource.
     * 
     * @param id id for AtomPubResource.
     * @return URI where client should do GET to download the AtomPubResource.
     * @throws URISyntaxException
     */
    protected URI getAPRUri(IdWrapper id) throws URISyntaxException {
        URI tail = new URI(id.toString());
        return CommonUtils.appendUri(getAPCUri(), tail);
    }

    /**
     * Get URI to fetch information for AtomPubResource.
     * 
     * @param id for AtomPubResource
     * @return URI from where client can fetch Information.
     * @throws URISyntaxException
     */
    protected URI getAPRUriForInformation(IdWrapper id) throws URISyntaxException {
        URI tail = new URI(id.toString() + "/" + ServletUris.INFORMATION);
        return CommonUtils.appendUri(getAPCUri(), tail);
    }

    /**
     * Get URI to fetch AtomPubResourceVersion. This is not applicable for CNX Resources.
     * 
     * @param id for AtomPubResource
     * @param version for AtomPubResource
     * @return URI from where client can fetch Information.
     * @throws URISyntaxException
     */
    protected URI getAPRVUri(IdWrapper id, VersionWrapper version)
            throws URISyntaxException {
        if (id.getType() == IdWrapper.Type.RESOURCE) {
            throw new RuntimeException("getAPRUriForInformation is not defined for Resources.");
        }
        URI tail =
                new URI(id.toString() + "/" + version.toString());
        return CommonUtils.appendUri(getAPCUri(), tail);
    }

    // TODO(arjuns) : Add test for this.
    private Collection getApc(List<Collection> apcs,
            CnxAtomPubCollectionEnum apcType) {
        for (Collection curApc : apcs) {
            String currTitle = CnxAtomPubUtils.getTitleString(curApc.getTitle());
            if (currTitle.equals(apcType.getTitle())) {
                return curApc;
            }
        }

        return null;
    }
}
