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

import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.propono.atom.client.ClientEntry;
import java.util.List;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;

/**
 * Utility class for CNX Clients.
 * 
 * @author Arjun Satyapal
 */
public class CnxClientUtils {
    // Utility class.
    private CnxClientUtils() {
    }
    
    /**
     * CNX AtomPub API should return the BlobStoreUrl with rel=
     * {@code CnxAtomPubConstants#REL_TAG_FOR_BLOBSTORE_URL} and href=<blobstore url> where clients
     * are expected to post the blobs.
     * 
     * TODO(arjuns) : Findout better name instead of BlobstoreURL.
     * 
     * @param entry AtomPub entry returned by Server.
     * @return Link containing Blobstore URL.
     */
    public static Link getBlobstoreUri(ClientEntry entry) {
        @SuppressWarnings("unchecked")
        List<Link> otherLinks = entry.getOtherLinks();

        for (Link currLink : otherLinks) {
            if (currLink.getRel().equals(CnxAtomPubUtils.REL_TAG_FOR_BLOBSTORE_URL)) {
                return currLink;
            }
        }

        throw new IllegalStateException("BlobstoreUri not found in entry.");
    }
    
    /**
     * CNX AtomPub API should return the resourceUrl with rel=
     * {@code CnxAtomPubConstants#REL_TAG_FOR_SELF_URL} and href=<resource URL>.
     * 
     * @param entry AtomPub entry returned by server.
     * @return Link containing Resource URL.
     */
    public static Link getSelfUri(ClientEntry entry) {
        @SuppressWarnings("unchecked")
        List<Link> otherLinks = entry.getOtherLinks();

        for (Link currLink : otherLinks) {
            if (currLink.getRel().equals(CnxAtomPubUtils.REL_TAG_FOR_SELF_URL)) {
                return currLink;
            }
        }

        throw new IllegalStateException("SelfUri not found in entry.");
    }
}
