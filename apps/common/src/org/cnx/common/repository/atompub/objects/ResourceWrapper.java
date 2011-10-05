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
package org.cnx.common.repository.atompub.objects;

import org.cnx.common.repository.atompub.CnxAtomPubLinkRelations;

import com.google.common.base.Preconditions;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import java.net.URI;
import java.net.URISyntaxException;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;

/**
 * Wrapper object for CNX Resource.
 * 
 * @author Arjun Satyapal
 */
public class ResourceWrapper extends AtomPubResource {
    private URI uploadUri;

    public ResourceWrapper(IdWrapper id, VersionWrapper version) {
        super(id, version);
    }
    
    public URI getUploadUri() {
        return uploadUri;
    }
    
    @Override
    protected StringBuilder getStringBuilder() {
        return super.getStringBuilder().append(", uploadUri:").append(uploadUri);
    }
    
    @Override
    public String toString() {
        return this.getStringBuilder().toString();
    }

    // TODO(arjuns) : Replace Link with URI.
    public void setUploadUri(Link uploadUri) throws URISyntaxException {
        Preconditions.checkNotNull(uploadUri);
        this.uploadUri = new URI(uploadUri.getHrefResolved());
    }
    
    public static ResourceWrapper fromEntry(Entry entry) throws URISyntaxException {
        IdWrapper id = CnxAtomPubUtils.getIdFromAtomPubId(entry.getId());
        ResourceWrapper resource = new ResourceWrapper(id, null /*version*/);
        
        resource.setSelfUri(CnxAtomPubLinkRelations.getSelfUri(entry));
        resource.setUploadUri(CnxAtomPubLinkRelations.getUploadUri(entry));
        resource.setPublished(entry.getPublished());
        
        return resource;
    }
}
