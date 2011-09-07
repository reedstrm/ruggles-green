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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.jdom.Element;
import org.jdom.Namespace;

public class MdmlMetadataTests {
    private static final Namespace ns = MdmlTag.NAMESPACE;

    @Test public void titleTest() throws Exception {
        final Element elem = new Element("foo")
                .addContent(new Element("title", ns).setText("Hello, World"));
        final MdmlMetadata metadata = new MdmlMetadata(elem);
        assertEquals("Hello, World", metadata.getTitle());
    }

    @Test public void authorTest() throws Exception {
        final Element elem = new Element("foo")
                .addContent(new Element("title", ns).setText("Hello, World"))
                .addContent(new Element("actors", ns)
                        .addContent(new Element("person", ns)
                                .setAttribute("userid", "john")
                                .addContent(new Element("firstname", ns).setText("John"))
                                .addContent(new Element("surname", ns).setText("Doe"))
                                .addContent(new Element("fullname", ns).setText("John Doe"))
                                .addContent(new Element("email", ns).setText("john@example.com")))
                        .addContent(new Element("person", ns)
                                .setAttribute("userid", "jane")
                                .addContent(new Element("firstname", ns).setText("Jane"))
                                .addContent(new Element("surname", ns).setText("Doe"))
                                .addContent(new Element("fullname", ns).setText("Jane Doe"))
                                .addContent(new Element("email", ns).setText("jane@example.com")))
                        .addContent(new Element("person", ns)
                                .setAttribute("userid", "jacob")
                                .addContent(new Element("firstname", ns).setText("Jacob"))
                                .addContent(new Element("surname", ns).setText("Doe"))
                                .addContent(new Element("fullname", ns).setText("Jacob"))))
                .addContent(new Element("roles", ns)
                        .addContent(new Element("role", ns)
                                .setAttribute("type", "maintainer").setText("jane"))
                        .addContent(new Element("role", ns)
                                .setAttribute("type", "author").setText("john jacob")));
        final MdmlMetadata metadata = new MdmlMetadata(elem);
        final List<Actor> actors = metadata.getAuthors();
        assertNotNull(actors);
        assertEquals(2, actors.size());
        assertEquals(new Person("John Doe", "John", "Doe", "john@example.com", null),
                actors.get(0));
        assertEquals(new Person("Jacob", "Jacob", "Doe", null, null), actors.get(1));
    }
}
