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
package org.cnx.repository.atompub.utils;

import java.net.URL;
import java.util.List;

import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.service.api.RepositoryRequestContext;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.atom.Link;

/**
 *
 * @author Arjun Satyapal
 */
public class RepositoryUtils {
    // TODO(arjuns) : remove this.
    /** Temporary global userId. */
    public static final String GLOBAL_USER_ID = "temp_user";

    public static RepositoryRequestContext getRepositoryContext() {
        return new RepositoryRequestContext(GLOBAL_USER_ID);
    }

    public static List<Link> getListOfLinks(URL selfUrl, URL editUrl) {
        List<Link> listOfLinks = Lists.newArrayList();

        if (selfUrl != null) {
            Link selfLink = new Link();
            selfLink.setRel(CnxAtomPubConstants.LINK_RELATION_SELF_TAG);
            selfLink.setHref(selfUrl.toString());
            listOfLinks.add(selfLink);
        }

        if (editUrl != null) {
            Link editLink = new Link();
            editLink.setRel(CnxAtomPubConstants.LINK_RELATION_EDIT_TAG);
            editLink.setHref(editUrl.toString());
            listOfLinks.add(editLink);
        }

        return listOfLinks;
    }
}
