/*
 * Copyright (C) 2011 The CNX Authors.
 * Copyright 2007 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.syndication.propono.atom.client;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.io.impl.Atom10Generator;
import com.sun.syndication.io.impl.Atom10Parser;
import com.sun.syndication.propono.utils.ProponoException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sun.syndication.propono.utils.Utilities;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Client implementation of Atom entry, extends ROME Entry to add methods for
 * easily getting/setting content, updating and removing the entry from the server.
 */
public class ClientEntry extends Entry {
    private static final Log  logger = LogFactory.getLog(ClientEntry.class);

    boolean partial = false;

    private ClientAtomService service = null;
    private ClientCollection collection = null;

    public ClientEntry(ClientAtomService service, ClientCollection collection) {
        super();
        this.service = service;
        this.collection = collection;
    }

    public ClientEntry(ClientAtomService service, ClientCollection collection,
            Entry entry, boolean partial) throws ProponoException {
        super();
        this.service = service;
        this.collection = collection;
        this.partial = partial;
        try {
            BeanUtils.copyProperties(this, entry);
        } catch (Exception e) {
            throw new ProponoException("ERROR: copying fields from ROME entry", e);
        }
    }

    /**
     * Set content of entry.
     * @param contentString content string.
     * @param type Must be "text" for plain text, "html" for escaped HTML,
     *             "xhtml" for XHTML or a valid MIME content-type.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setContent(String contentString, String type) {
        Content newContent = new Content();
        newContent.setType(type == null ? Content.HTML : type);
        newContent.setValue(contentString);
        ArrayList contents = new ArrayList();
        contents.add(newContent);
        setContents(contents);
    }

    /**
     * Convenience method to set first content object in content collection.
     * Atom 1.0 allows only one content element per entry.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setContent(Content c) {
        ArrayList contents = new ArrayList();
        contents.add(c);
        setContents(contents);
    }

    /**
     * Convenience method to get first content object in content collection.
     * Atom 1.0 allows only one content element per entry.
     */
    public Content getContent() {
        if (getContents() != null && getContents().size() > 0) {
            Content c = (Content)getContents().get(0);
            return c;
        }
        return null;
    }

    /**
     * Determines if entries are equal based on edit URI.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ClientEntry) {
            ClientEntry other = (ClientEntry)o;
            if (other.getEditURI() != null && getEditURI() != null) {
                return other.getEditURI().equals(getEditURI());
            }
        }
        return false;
    }

    /**
     * Update entry by posting new representation of entry to server.
     * Note that you should not attempt to update entries that you get from
     * iterating over a collection they may be "partial" entries. If you want
     * to update an entry, you must get it via one of the <code>getEntry()</code>
     * methods in
     * {@link com.sun.syndication.propono.atom.common.Collection} or
     * {@link com.sun.syndication.propono.atom.common.AtomService}.
     * @throws ProponoException If entry is a "partial" entry.
     */
    @SuppressWarnings("deprecation")
    public void update() throws ProponoException {
        if (partial) {
            throw new ProponoException("ERROR: attempt to update partial entry");
        }
        EntityEnclosingMethod method = new PutMethod(getEditURI());
        addAuthentication(method);
        StringWriter sw = new StringWriter();
        int code = -1;
        try {
            Atom10Generator.serializeEntry(this, sw);
            method.setRequestEntity(new StringRequestEntity(sw.toString()));
            method.setRequestHeader(
                "Content-type", "application/atom+xml; charset=utf-8");
            getHttpClient().executeMethod(method);
            InputStream is = method.getResponseBodyAsStream();
            if (method.getStatusCode() != 200 && method.getStatusCode() != 201) {
                throw new ProponoException(
                    "ERROR HTTP status=" + method.getStatusCode() + " : " + Utilities.streamToString(is));
            }

        } catch (Exception e) {
            String msg = "ERROR: updating entry, HTTP code: " + code;
            logger.debug(msg, e);
            throw new ProponoException(msg, e);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Remove entry from server.
     */
    public void remove() throws ProponoException {
        if (getEditURI() == null) {
            throw new ProponoException("ERROR: cannot delete unsaved entry");
        }
        DeleteMethod method = new DeleteMethod(getEditURI());
        addAuthentication(method);
        try {
            getHttpClient().executeMethod(method);
        } catch (IOException ex) {
            throw new ProponoException("ERROR: removing entry, HTTP code", ex);
        } finally {
            method.releaseConnection();
        }
    }

    void setCollection(ClientCollection collection) {
        this.collection = collection;
    }

    ClientCollection getCollection() {
        return collection;
    }

    /**
     * Get the URI that can be used to edit the entry via HTTP PUT or DELETE.
     */
    public String getEditURI() {
        for (int i=0; i<getOtherLinks().size(); i++) {
            Link link = (Link)getOtherLinks().get(i);
            if (link.getRel() != null && link.getRel().equals("edit")) {
                return link.getHrefResolved();
            }
        }
        return null;
    }

    @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
    void addToCollection(ClientCollection col) throws ProponoException {
        setCollection(col);
        EntityEnclosingMethod method = new PostMethod(getCollection().getHrefResolved());
        addAuthentication(method);
        StringWriter sw = new StringWriter();
        int code = -1;
        try {
            Atom10Generator.serializeEntry(this, sw);
            method.setRequestEntity(new StringRequestEntity(sw.toString()));
            method.setRequestHeader(
                "Content-type", "application/atom+xml; charset=utf-8");
            getHttpClient().executeMethod(method);
            InputStream is = method.getResponseBodyAsStream();
            code = method.getStatusCode();
            if (code != 200 && code != 201) {
                throw new ProponoException(
                    "ERROR HTTP status=" + code + " : " + Utilities.streamToString(is));
            }
            Entry romeEntry = Atom10Parser.parseEntry(
                new InputStreamReader(is), getCollection().getHrefResolved());
            BeanUtils.copyProperties(this, romeEntry);

        } catch (Exception e) {
            String msg = "ERROR: saving entry, HTTP code: " + code;
            logger.debug(msg, e);
            throw new ProponoException(msg, e);
        } finally {
            method.releaseConnection();
        }
        Header locationHeader = method.getResponseHeader("Location");
        if (locationHeader == null) {
            logger.warn("WARNING added entry, but no location header returned");
        } else if (getEditURI() == null) {
            List links = getOtherLinks();
            Link link = new Link();
            link.setHref(locationHeader.getValue());
            link.setRel("edit");
            links.add(link);
            setOtherLinks(links);
        }
    }

    void addAuthentication(HttpMethodBase method) throws ProponoException {
        if (service != null) {
            service.addAuthentication(method);
        } else if (collection != null) {
            collection.addAuthentication(method);
        }
    }

    HttpClient getHttpClient() {
        if (service != null) {
            return service.getHttpClient();
        } else if (collection != null) {
            return collection.getHttpClient();
        }
        return null;
    }

}
