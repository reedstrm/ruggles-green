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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sun.syndication.feed.atom.Link;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;

/**
 *
 * @author Arjun Satyapal
 */
public abstract class AtomPubResource {
    private final IdWrapper id;
    private final VersionWrapper version;
    
    private Date published;
    private URI selfUri;
    private URI editUri;
    
    public AtomPubResource(IdWrapper id, VersionWrapper version) {
        this.id = checkNotNull(id);
        this.version = version;
        
        if (id.getType() != IdWrapper.Type.RESOURCE) {
            checkNotNull(version);
        }
    }
    
    protected StringBuilder getStringBuilder() {
        return new StringBuilder("Id:").append(id)
                .append(", Version:").append(version)
                .append(", published:").append(published)
                .append(", selfUri:").append(selfUri)
                .append(", editUri:").append(editUri);
    }
    
    public IdWrapper getId() {
        return id;
    }
    
    public VersionWrapper getVersion() {
        return version;
    }

    public URI getSelfUri() {
        return selfUri;
    }

    protected void setSelfUri(Link selfUri) throws URISyntaxException {
        checkNotNull(selfUri);
        this.selfUri = new URI(selfUri.getHrefResolved());
    }

    public URI getEditUri() {
        return editUri;
    }

    protected void setEditUri(Link editUri) throws URISyntaxException {
        checkNotNull(editUri);
        this.editUri = new URI(editUri.getHrefResolved());
    }
    
    public Date getPublished() {
        return published;
    }
    
    protected void setPublished(Date published) {
        this.published = published;
    }
}
