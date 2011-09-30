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

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import java.util.List;

/**
 * Enum defining types of relations for Links for AtomPub API.
 * TODO(arjuns) : Add tests for this.
 * 
 * @author Arjun Satyapal
 */
public enum CnxAtomPubLinkRelations {
    EDIT("edit"),
    SELF("self"),
    UPLOAD_URL("uploadUrl");
    
    private String relation;
    
    private CnxAtomPubLinkRelations(String relation) {
        this.relation = relation;
    }
    
    public String getLinkRelation() {
        return relation;
    }
    
    /**
     * CNX AtomPub API should return the BlobStoreUrl with rel={@link #UPLOAD_URL} 
     * and href=<blobstore url> where clients are expected to post the blobs.
     * 
     * @param entry AtomPub entry returned by Server.
     * @return Link containing Upload URL.
     */
    public static Link getUploadUri(Entry entry) {
        return getUri(CnxAtomPubLinkRelations.UPLOAD_URL, entry);
    }

    /**
     * CNX AtomPub API should return the resourceUrl with rel={@link #SELF} and href=<resource URL>.
     * 
     * @param entry AtomPub entry returned by server.
     * @return Link to download this entry again.
     */
    public static Link getSelfUri(Entry entry) {
        return getUri(CnxAtomPubLinkRelations.SELF, entry);
    }
    
    /**
     * CNX AtomPub API should return the resourceUrl with rel={@link #EDIT} and href=<resource URL>.
     * TODO(arjuns) : Rename Link to URI.
     * @param entry AtomPub entry returned by server.
     * @return Link to post new version.
     */
    public static Link getEditUri(Entry entry) {
        return getUri(CnxAtomPubLinkRelations.EDIT, entry);
    }
    
    private static Link getUri(CnxAtomPubLinkRelations linkRelation, Entry entry) {
        @SuppressWarnings("unchecked")
        List<Link> otherLinks = entry.getOtherLinks();

        for (Link currLink : otherLinks) {
            if (currLink.getRel().equals(linkRelation.getLinkRelation())) {
                return currLink;
            }
        }
        
        throw new IllegalStateException(linkRelation.getLinkRelation() + " not found in entry.");
    }
}
