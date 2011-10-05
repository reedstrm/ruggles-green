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

import static org.cnx.atompubclient2.HttpClientWrapper.getHttpClient;

import com.google.common.io.CharStreams;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.io.FeedException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.JAXBException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.PrettyXmlOutputter;
import org.cnx.common.repository.atompub.CnxAtomPubCollectionEnum;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionVersionWrapper;
import org.cnx.common.repository.atompub.objects.CollectionWrapper;
import org.cnx.servicedocument.Service;
import org.jdom.JDOMException;

/**
 * 
 * @author Arjun Satyapal
 */
public class APCForCollection extends AbstractAPC {
    public APCForCollection(Service serviceDocumentObject) throws URISyntaxException {
        super(serviceDocumentObject, CnxAtomPubCollectionEnum.APC_COLLECTION);
    }

    // TODO(arjuns) : Add accept in collection.
    public CollectionWrapper createCollection() throws IOException, IllegalArgumentException,
            JDOMException, FeedException, URISyntaxException, CnxException {
        return createCollection(getAPCUri());
    }

    public CollectionWrapper createCollectionForMigration(IdWrapper id)
            throws IllegalArgumentException, IOException, JDOMException, FeedException,
            URISyntaxException, CnxException {
        return createCollection(getAPRUriForMigration(id));
    }

    private CollectionWrapper createCollection(URI postUri) throws IOException,
            IllegalArgumentException, JDOMException, FeedException, URISyntaxException,
            CnxException {
        HttpPost post = new HttpPost(postUri);
        HttpResponse httpResponse = getHttpClient().execute(post);

        String response =
                CharStreams.toString(new InputStreamReader(httpResponse.getEntity().getContent()));
        Entry entry = CnxAtomPubUtils.parseXmlToEntry(response);
        CollectionWrapper module = CollectionWrapper.fromEntry(entry);

        return module;
    }

    // public ResourceInfoWrapper getModuleInformation(IdWrapper id) throws URISyntaxException,
    // IOException, IllegalArgumentException, JDOMException, FeedException {
    // String response = getHttpClient().get(getAPRUriForInformation(id));
    // Entry entry = CnxAtomPubUtils.parseStringToEntry(response);
    // return ResourceInfoWrapper.fromEntry(entry);
    // }

    public CollectionWrapper createCollectionVersion(URI editUri, String collectionXml)
            throws JDOMException,
            IOException, CnxException, JAXBException, IllegalArgumentException, FeedException,
            URISyntaxException {
        Entry moduleVersionEntry = new Entry();
        moduleVersionEntry.setContents(CnxAtomPubUtils
                .getAtomPubListOfContentForCollectionEntry(collectionXml));

        StringEntity stringEntity =
                new StringEntity(PrettyXmlOutputter.prettyXmlOutputEntry(moduleVersionEntry));
        HttpPut httpPut = new HttpPut(editUri);
        httpPut.setEntity(stringEntity);
        HttpResponse httpResponse = getHttpClient().execute(httpPut);

        String response =
                CharStreams.toString(new InputStreamReader(httpResponse.getEntity().getContent()));
        Entry entry = CnxAtomPubUtils.parseXmlToEntry(response);
        return CollectionWrapper.fromEntry(entry);
    }

    public CollectionVersionWrapper getCollectionVersion(IdWrapper id, VersionWrapper version)
            throws IllegalStateException, IOException, URISyntaxException, JDOMException,
            IllegalArgumentException, FeedException, CnxException {
        HttpGet httpGet = new HttpGet(getAPRVUri(id, version));
        HttpResponse httpResponse = getHttpClient().execute(httpGet);

        String response =
                CharStreams.toString(new InputStreamReader(httpResponse.getEntity().getContent()));
        Entry entry = CnxAtomPubUtils.parseXmlToEntry(response);

        return CollectionVersionWrapper.fromEntry(entry);
    }
}
