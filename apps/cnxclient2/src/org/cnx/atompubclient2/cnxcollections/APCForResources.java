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

import com.google.cloud.sql.jdbc.internal.Charsets;
import com.google.common.io.CharStreams;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.io.FeedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.http.HttpStatusEnum;
import org.cnx.common.repository.atompub.CnxAtomPubCollectionEnum;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.objects.ResourceInfoWrapper;
import org.cnx.common.repository.atompub.objects.ResourceWrapper;
import org.cnx.servicedocument.Service;
import org.jdom.JDOMException;

/**
 * 
 * @author Arjun Satyapal
 */
public class APCForResources extends AbstractAPC {
    public APCForResources(Service serviceDocumentObject) throws URISyntaxException {
        super(serviceDocumentObject, CnxAtomPubCollectionEnum.APC_RESOURCES);
    }

    // TODO(arjuns) : Add accept in collection.
    public ResourceWrapper createResource() throws IOException, IllegalArgumentException,
            JDOMException, FeedException, URISyntaxException, CnxException {
        return createResource(getAPCUri());
    }

    public ResourceWrapper createResourceForMigration(IdWrapper id)
            throws IllegalArgumentException, IOException, JDOMException, FeedException,
            URISyntaxException, CnxException {
        return createResource(getAPRUriForMigration(id));
    }

    private ResourceWrapper createResource(URI postUri) throws IOException,
            IllegalArgumentException, JDOMException, FeedException, URISyntaxException,
            CnxException {
        HttpPost post = new HttpPost(postUri);
        HttpResponse httpResponse = getHttpClient().execute(post);

        handleHttpResponse(httpResponse);

        String response =
                CharStreams.toString(new InputStreamReader(httpResponse.getEntity().getContent()));
        Entry entry = CnxAtomPubUtils.parseXmlToEntry(response);
        ResourceWrapper resource = ResourceWrapper.fromEntry(entry);

        return resource;
    }

    /**
     * @param httpResponse
     * @throws CnxException
     */
    private void handleHttpResponse(HttpResponse httpResponse) throws CnxException {
        StatusLine statusLine = httpResponse.getStatusLine();
        HttpStatusEnum status =
                HttpStatusEnum.getHttpStatusEnumByStatusCode(statusLine.getStatusCode());

        switch (status.getStatusCategories()) {
            case SUCCESSFUL:
                break;

            default:
                throw new CnxException(status, "Fail", null);
        }
    }

    public InputStream getResourceAsStream(IdWrapper id) throws IOException, URISyntaxException,
            CnxException {
        HttpGet httpGet = new HttpGet(getAPRUri(id));
        HttpEntity httpEntity = getHttpClient().execute(httpGet).getEntity();
        if (httpEntity != null) {
            return httpEntity.getContent();
        }

        // TODO(arjuns) : Handle exceptional cases.
        return null;
    }

    public ResourceInfoWrapper getResourceInformation(IdWrapper id) throws URISyntaxException,
            IOException, IllegalArgumentException, JDOMException, FeedException, CnxException {
        HttpGet httpGet = new HttpGet(getAPRUriForInformation(id));

        HttpEntity httpEntity = getHttpClient().execute(httpGet).getEntity();
        if (httpEntity != null) {
            String response =
                    CharStreams.toString(new InputStreamReader(httpEntity.getContent(),
                            Charsets.UTF_8));
            Entry entry = CnxAtomPubUtils.parseXmlToEntry(response);
            return ResourceInfoWrapper.fromEntry(entry);
        }

        // TODO(arjuns) : Handle exceptional cases.
        return null;

    }
}
