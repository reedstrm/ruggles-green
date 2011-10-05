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

import com.sun.syndication.feed.atom.Entry;
import java.io.IOException;
import java.net.URISyntaxException;
import org.cnx.common.repository.atompub.CnxAtomPubLinkRelations;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;

/**
 * Wrapper object for CNX Collection Version.
 * 
 * @author Arjun Satyapal
 */
public class CollectionVersionWrapper extends AtomPubResource {
    private final String collectionXml;

    public CollectionVersionWrapper(IdWrapper id, VersionWrapper version, String collectionXml) {
        super(id, version);
        this.collectionXml = collectionXml;
    }

    @Override
    protected StringBuilder getStringBuilder() {
        return super.getStringBuilder()
                .append("collectionXml=").append(collectionXml);
    }

    @Override
    public String toString() {
        return this.getStringBuilder().toString();
    }

    public static CollectionVersionWrapper fromEntry(Entry entry) throws URISyntaxException,
            IOException {
        IdWrapper id = CnxAtomPubUtils.getIdFromAtomPubId(entry.getId());
        VersionWrapper version = CnxAtomPubUtils.getVersionFromAtomPubId(entry.getId());

        String encodedCollectionXml = CnxAtomPubUtils.getContentAsString(entry.getContents());
        String decodedCollectionXml = CnxAtomPubUtils.decodeFromBase64EncodedString(encodedCollectionXml);

        CollectionVersionWrapper collection =
                new CollectionVersionWrapper(id, version, decodedCollectionXml);

        collection.setSelfUri(CnxAtomPubLinkRelations.getSelfUri(entry));
        collection.setEditUri(CnxAtomPubLinkRelations.getEditUri(entry));
        collection.setPublished(entry.getPublished());

        return collection;
    }

    public String getCollectionXml() {
        return collectionXml;
    }
}
