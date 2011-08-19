/*
 *  Copyright 2011 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cnx.mdml;

import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cnx.util.DOMUtils;
import org.w3c.dom.Element;

/**
 *  MdmlMetadata provides accessors for common MDML fields.
 */
public class MdmlMetadata implements Metadata {
    private static final String TITLE_TAG = "title";
    private static final String ABSTRACT_TAG = "abstract";
    private static final String ACTORS_TAG = "actors";
    private static final String ROLES_TAG = "roles";

    private static final String PERSON_TAG = "person";
    private static final String ORGANIZATION_TAG = "organization";
    private static final String USER_ID_ATTR = "userid";

    private static final String FULL_NAME_TAG = "fullname";
    private static final String SHORT_NAME_TAG = "shortname";
    private static final String FIRST_NAME_TAG = "firstname";
    private static final String SURNAME_TAG = "surname";
    private static final String EMAIL_ADDRESS_TAG = "email";
    private static final String HOMEPAGE_TAG = "homepage";

    private static final String ROLE_TAG = "role";
    private static final String ROLE_TYPE_ATTR = "type";
    private static final String AUTHOR_ROLE_TYPE = "author";
    private static final String ROLE_USER_ID_SEP = " ";

    private final Element parent;
    private final String mdmlNamespace;
    private Map<String, Actor> actors;

    public interface Factory {
        public MdmlMetadata create(Element parent);
    }

    @Inject public MdmlMetadata(@Assisted Element parent,
            @MdmlNamespace String metadataNamespace) {
        this.parent = parent;
        this.mdmlNamespace = metadataNamespace;
    }

    public String getTitle() throws Exception {
        return DOMUtils.getElementText(parent, mdmlNamespace, TITLE_TAG);
    }

    public String getAbstract() throws Exception {
        return DOMUtils.getElementText(parent, mdmlNamespace, ABSTRACT_TAG);
    }

    public List<Actor> getAuthors() throws Exception {
        final List<Actor> authors = new ArrayList<Actor>();

        buildActorMap();
        if (actors == null) {
            return authors;
        }
        final Element rolesElement = DOMUtils.findFirstChild(parent, mdmlNamespace, ROLES_TAG);
        if (rolesElement == null) {
            return authors;
        }

        for (Element child : DOMUtils.iterElements(rolesElement)) {
            if (mdmlNamespace.equals(child.getNamespaceURI())
                    && ROLE_TAG.equals(child.getLocalName())
                    && AUTHOR_ROLE_TYPE.equals(child.getAttribute(ROLE_TYPE_ATTR))) {
                for (String userId : Splitter.on(ROLE_USER_ID_SEP).split(child.getTextContent())) {
                    if (actors.containsKey(userId)) {
                        authors.add(actors.get(userId));
                    }
                }
            }
        }
        return authors;
    }

    private void buildActorMap() {
        actors = new HashMap<String, Actor>();

        final Element actorsElement = DOMUtils.findFirstChild(parent, mdmlNamespace, ACTORS_TAG);
        if (actorsElement == null) {
            return;
        }

        for (Element child : DOMUtils.iterElements(actorsElement)) {
            if (mdmlNamespace.equals(child.getNamespaceURI())
                    && child.hasAttribute(USER_ID_ATTR)) {
                final String key = child.getAttribute(USER_ID_ATTR);
                final String localName = child.getLocalName();
                if (PERSON_TAG.equals(localName)) {
                    actors.put(key, parsePerson(child));
                } else if (ORGANIZATION_TAG.equals(localName)) {
                    actors.put(key, parseOrganization(child));
                }
            }
        }
    }

    private Person parsePerson(final Element elem) {
        final String fullName = DOMUtils.getElementText(elem, mdmlNamespace, FULL_NAME_TAG);
        final String firstName = DOMUtils.getElementText(elem, mdmlNamespace, FIRST_NAME_TAG);
        final String surname = DOMUtils.getElementText(elem, mdmlNamespace, SURNAME_TAG);
        final String emailAddress = DOMUtils.getElementText(elem, mdmlNamespace, EMAIL_ADDRESS_TAG);
        final String homepage = DOMUtils.getElementText(elem, mdmlNamespace, HOMEPAGE_TAG);
        return new Person(fullName, firstName, surname, emailAddress, homepage);
    }

    private Organization parseOrganization(final Element elem) {
        final String fullName = DOMUtils.getElementText(elem, mdmlNamespace, FULL_NAME_TAG);
        final String shortName = DOMUtils.getElementText(elem, mdmlNamespace, SHORT_NAME_TAG);
        final String emailAddress = DOMUtils.getElementText(elem, mdmlNamespace, EMAIL_ADDRESS_TAG);
        final String homepage = DOMUtils.getElementText(elem, mdmlNamespace, HOMEPAGE_TAG);
        return new Organization(fullName, shortName, emailAddress, homepage);
    }
}
