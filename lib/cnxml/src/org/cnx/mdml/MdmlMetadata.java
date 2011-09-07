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

package org.cnx.mdml;

import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 *  MdmlMetadata provides accessors for common MDML fields.
 */
public class MdmlMetadata implements Metadata {
    private final Element parent;
    private Map<String, Actor> actors;

    public interface Factory {
        public MdmlMetadata create(Element parent);
    }

    @Inject public MdmlMetadata(@Assisted Element parent) {
        this.parent = parent;
    }

    @Override
    public String getTitle() throws Exception {
        return parent.getChildText(MdmlTag.TITLE.getTag(), MdmlTag.NAMESPACE);
    }

    @Override
    public String getAbstract() throws Exception {
        return parent.getChildText(MdmlTag.ABSTRACT.getTag(), MdmlTag.NAMESPACE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Actor> getAuthors() throws Exception {
        final List<Actor> authors = new ArrayList<Actor>();

        buildActorMap();
        if (actors == null) {
            return authors;
        }

        final Element rolesElem = parent.getChild(MdmlTag.ROLES.getTag(), MdmlTag.NAMESPACE);
        if (rolesElem == null) {
            return authors;
        }
        final List<Element> roles = (List<Element>)rolesElem.getContent(new ElementFilter(
                    MdmlTag.ROLE.getTag(), MdmlTag.NAMESPACE));
        for (Element child : roles) {
            final MdmlAttributes.RoleType roleType = MdmlAttributes.RoleType.of(
                    child.getAttributeValue(MdmlAttributes.ROLE_TYPE));
            if (roleType == MdmlAttributes.RoleType.AUTHOR) {
                for (String userId : Splitter.on(MdmlTag.ROLE_SEP).split(child.getText())) {
                    if (actors.containsKey(userId)) {
                        authors.add(actors.get(userId));
                    }
                }
            }
        }
        return authors;
    }

    @SuppressWarnings("unchecked")
    private void buildActorMap() {
        actors = new HashMap<String, Actor>();

        final Element actorsElem = parent.getChild(MdmlTag.ACTORS.getTag(), MdmlTag.NAMESPACE);
        if (actorsElem == null) {
            return;
        }

        final List<Element> children = (List<Element>)actorsElem.getContent(
                new ElementFilter(MdmlTag.NAMESPACE));
        for (Element child : children) {
            final String userId = child.getAttributeValue(MdmlAttributes.USER_ID);
            if (userId != null) {
                final MdmlTag tag = MdmlTag.of(child.getName());
                switch (tag) {
                case PERSON:
                    actors.put(userId, buildPerson(child));
                    break;
                case ORGANIZATION:
                    actors.put(userId, buildOrganization(child));
                    break;
                }
            }
        }
    }

    private Person buildPerson(final Element elem) {
        final String fullName = elem.getChildText(MdmlTag.FULL_NAME.getTag(), MdmlTag.NAMESPACE);
        final String firstName = elem.getChildText(MdmlTag.FIRST_NAME.getTag(), MdmlTag.NAMESPACE);
        final String lastName = elem.getChildText(MdmlTag.LAST_NAME.getTag(), MdmlTag.NAMESPACE);
        final String emailAddress = elem.getChildText(
                MdmlTag.EMAIL_ADDRESS.getTag(), MdmlTag.NAMESPACE);
        final String homepage = elem.getChildText(MdmlTag.HOMEPAGE.getTag(), MdmlTag.NAMESPACE);
        return new Person(fullName, firstName, lastName, emailAddress, homepage);
    }

    private Organization buildOrganization(final Element elem) {
        final String fullName = elem.getChildText(MdmlTag.FULL_NAME.getTag(), MdmlTag.NAMESPACE);
        final String shortName = elem.getChildText(MdmlTag.SHORT_NAME.getTag(), MdmlTag.NAMESPACE);
        final String emailAddress = elem.getChildText(
                MdmlTag.EMAIL_ADDRESS.getTag(), MdmlTag.NAMESPACE);
        final String homepage = elem.getChildText(MdmlTag.HOMEPAGE.getTag(), MdmlTag.NAMESPACE);
        return new Organization(fullName, shortName, emailAddress, homepage);
    }
}
