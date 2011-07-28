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
package org.cnx.repository.atompub.servlets.servicedocument;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.propono.atom.common.AtomService;
import com.sun.syndication.propono.atom.common.Categories;
import com.sun.syndication.propono.atom.server.AtomHandler;
import com.sun.syndication.propono.atom.server.AtomMediaResource;
import com.sun.syndication.propono.atom.server.AtomRequest;

import org.cnx.repository.atompub.service.CnxAtomService;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomServiceDocumentHandler implements AtomHandler {
    private CnxAtomService atomService;

    public CnxAtomServiceDocumentHandler(HttpServletRequest req) {
        atomService = new CnxAtomService(req);
    }

    @Override
    public String getAuthenticatedUsername() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return AtomService associated with this handler.
     */
    @Override
    public AtomService getAtomService(AtomRequest req) {
        return atomService;
    }

    /**
     * ServiceDocument does not have any Category. So this operation on ServiceDocument is not
     * supported.
     */
    @Override
    public Categories getCategories(AtomRequest req) {
        throw new UnsupportedOperationException();
    }

    /**
     * ServiceDocument contains workspaces. In order to reach collection, first get Workspace, and
     * then user workspace to get Collections.
     */
    @Override
    public Feed getCollection(AtomRequest req) {
        throw new UnsupportedOperationException();
    }

    /**
     * ServiceDocument does not accept POST.
     */
    @Override
    public Entry postEntry(AtomRequest req, Entry entry) {
        throw new UnsupportedOperationException();
    }

    /**
     * ServiceDocument does not provide Entry.
     */
    @Override
    public Entry getEntry(AtomRequest req) {
        throw new UnsupportedOperationException();
    }

    /**
     * ServiceDocument does not provide Medai Resource.
     */
    @Override
    public AtomMediaResource getMediaResource(AtomRequest req) {
        throw new UnsupportedOperationException();
    }

    /**
     * ServiceDocument does not accept PUT.
     */
    @Override
    public void putEntry(AtomRequest req, Entry entry) {
        throw new UnsupportedOperationException();
    }

    /**
     * ServiceDocument does not accept DELETE.
     */
    @Override
    public void deleteEntry(AtomRequest req) {
        throw new UnsupportedOperationException();
    }

    /**
     * ServiceDocument does not accept POST.
     */
    @Override
    public Entry postMedia(AtomRequest req, Entry entry) {
        throw new UnsupportedOperationException();
    }

    /**
     * ServiceDocument does not accept POST.
     */
    @Override
    public void putMedia(AtomRequest req) {
        throw new UnsupportedOperationException();
    }

    /**
     * CnxAtomService Document is only for Service Document. So this will always return true.
     */
    @Override
    public boolean isAtomServiceURI(AtomRequest req) {
        return true;
    }

    /**
     * CnxAtomService Document is only for Service Document. So this will always return false.
     */
    @Override
    public boolean isCategoriesURI(AtomRequest req) {
        return false;
    }

    /**
     * CnxAtomService Document is only for Service Document. So this will always return false.
     */
    @Override
    public boolean isCollectionURI(AtomRequest req) {
        return false;
    }

    /**
     * CnxAtomService Document is only for Service Document. So this will always return true.
     */
    @Override
    public boolean isEntryURI(AtomRequest req) {
        return false;
    }

    /**
     * CnxAtomService Document is only for Service Document. So this will always return true.
     */
    @Override
    public boolean isMediaEditURI(AtomRequest req) {
        return false;
    }
}
