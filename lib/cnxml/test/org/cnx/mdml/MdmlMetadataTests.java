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
}
