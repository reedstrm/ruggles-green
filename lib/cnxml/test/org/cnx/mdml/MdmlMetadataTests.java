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

import java.util.List;
import org.cnx.util.DocumentBuilderProvider;
import org.cnx.util.testing.DOMBuilder;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import static org.junit.Assert.*;

public class MdmlMetadataTests {
    private static final String MDML_NAMESPACE = "metadata";

    private DOMBuilder builder;

    @Before public void createBuilder() {
        builder = new DOMBuilder(new DocumentBuilderProvider().get().newDocument(),
                MDML_NAMESPACE);
    }

    @Test public void titleTest() throws Exception {
        final Element elem = (Element)builder.element("foo", "metadata").child(
                builder.element("title").text("Hello, World")
        ).build();
        final MdmlMetadata metadata = new MdmlMetadata(elem, MDML_NAMESPACE);
        assertEquals("Hello, World", metadata.getTitle());
    }

    @Test public void authorTest() throws Exception {
        final Element elem = (Element)builder.element("foo", "metadata").child(
                builder.element("title").text("Hello, World"),
                builder.element("actors").child(
                        builder.element("person").attr("userid", "john").child(
                                builder.element("firstname").text("John"),
                                builder.element("surname").text("Doe"),
                                builder.element("fullname").text("John Doe"),
                                builder.element("email").text("john@example.com")
                        ),
                        builder.element("person").attr("userid", "jane").child(
                                builder.element("firstname").text("Jane"),
                                builder.element("surname").text("Doe"),
                                builder.element("fullname").text("Jane Doe"),
                                builder.element("email").text("jane@example.com")
                        ),
                        builder.element("person").attr("userid", "jacob").child(
                                builder.element("firstname").text("Jacob"),
                                builder.element("surname").text("Doe"),
                                builder.element("fullname").text("Jacob")
                        )
                ),
                builder.element("roles").child(
                        builder.element("role").attr("type", "maintainer").text("jane"),
                        builder.element("role").attr("type", "author").text("john jacob")
                )
        ).build();
        final MdmlMetadata metadata = new MdmlMetadata(elem, MDML_NAMESPACE);
        final List<Actor> actors = metadata.getAuthors();
        assertNotNull(actors);
        assertEquals(2, actors.size());
        assertEquals(new Person("John Doe", "John", "Doe", "john@example.com", null), actors.get(0));
        assertEquals(new Person("Jacob", "Jacob", "Doe", null, null), actors.get(1));
    }
}
