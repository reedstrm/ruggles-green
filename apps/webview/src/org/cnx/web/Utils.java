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

package org.cnx.web;

import com.google.common.base.Preconditions;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import java.util.List;
import org.cnx.mdml.Actor;
import org.cnx.mdml.Organization;
import org.cnx.mdml.Person;

/** The Utils class holds common methods for web view servlets. */
public class Utils {
    public static SoyData convertActorListToSoyData(List<Actor> actors) {
        final SoyListData data = new SoyListData();
        for (Actor actor : actors) {
            if (actor instanceof Person) {
                data.add(convertPersonToSoyData((Person)actor));
            } else if (actor instanceof Organization) {
                data.add(convertOrganizationToSoyData((Organization)actor));
            }
        }
        return data;
    }

    public static SoyData convertPersonToSoyData(Person person) {
        Preconditions.checkNotNull(person);
        return new SoyMapData(
                "type", "person",
                "fullName", person.getFullName(),
                "firstName", person.getFirstName(),
                "lastName", person.getLastName(),
                "emailAddress", person.getEmailAddress(),
                "homepage", person.getHomepage()
        );
    }

    public static SoyData convertOrganizationToSoyData(Organization org) {
        Preconditions.checkNotNull(org);
        return new SoyMapData(
                "type", "organization",
                "fullName", org.getFullName(),
                "shortName", org.getShortName(),
                "emailAddress", org.getEmailAddress(),
                "homepage", org.getHomepage()
        );
    }
}
