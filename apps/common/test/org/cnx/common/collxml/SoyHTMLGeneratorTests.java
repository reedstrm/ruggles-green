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

package org.cnx.common.collxml;

import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import java.net.URI;
import org.cnx.cnxml.ResourceResolver;
import org.cnx.util.DocumentBuilderProvider;
import org.cnx.util.testing.DOMBuilder;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import static org.junit.Assert.*;

/**
 *  SoyHTMLGeneratorTests tests the {@link SoyHTMLGenerator} implementation.
 */
public class SoyHTMLGeneratorTests {
    private static final String COLLECTION_NAMESPACE = "collection";
    private static final String METADATA_NAMESPACE = "metadata";

    private DOMBuilder builder;
    private SoyHTMLGenerator generator;

    private static class MockResourceResolver implements ResourceResolver {
        @Override public URI resolveURI(URI uri) throws Exception {
            assertNotNull(uri);
            return uri;
        }

        @Override public URI resolveDocument(String document, String version) throws Exception {
            assertNotNull(document);
            assertNotNull(version);
            return new URI("document", document, version);
        }

        @Override public URI resolveResource(String document, String version, String resource)
                throws Exception {
            fail("Generator should not call resolveResource");
            return null;
        }
    }

    @Before public void createBuilder() {
        builder = new DOMBuilder(new DocumentBuilderProvider().get().newDocument(),
                COLLECTION_NAMESPACE);
    }

    @Before public void createGenerator() {
        generator = new SoyHTMLGenerator(null, new MockResourceResolver(),
                COLLECTION_NAMESPACE, METADATA_NAMESPACE);
    }

    @Test public void itemExtractionShouldFindModule() throws Exception {
        final Document doc = (Document)builder.child(builder.element("collection").wrapContent(
                builder.element("module")
                        .attr("url", "http://www.example.com/")
                        .child(builder.element(METADATA_NAMESPACE, "title").text("My Module"))
        )).build();
        final SoyListData itemList = generator.extractItemsFromCollection(doc);

        assertEquals(1, itemList.length());
        assertTrue(itemList.get(0) instanceof SoyMapData);
        assertEquals("module", itemList.getMapData(0).getString("type"));
        assertEquals("My Module", itemList.getMapData(0).getString("title"));
        assertEquals("http://www.example.com/", itemList.getMapData(0).getString("uri"));
    }

    @Test public void itemExtractionShouldResolveDocuments() throws Exception {
        final Document doc = (Document)builder.child(builder.element("collection").wrapContent(
                builder.element("module")
                        .attr("document", "mymod")
                        .attr("version", "abc123")
                        .child(builder.element(METADATA_NAMESPACE, "title").text("My Module"))
        )).build();
        final SoyListData itemList = generator.extractItemsFromCollection(doc);

        assertEquals(1, itemList.length());
        assertTrue(itemList.get(0) instanceof SoyMapData);
        assertEquals("module", itemList.getMapData(0).getString("type"));
        assertEquals("My Module", itemList.getMapData(0).getString("title"));
        assertEquals("document:mymod#abc123", itemList.getMapData(0).getString("uri"));
    }

    @Test public void itemExtractionShouldFindFlatList() throws Exception {
        final Document doc = (Document)builder.child(builder.element("collection").wrapContent(
                builder.element("module")
                        .attr("url", "one.cnxml")
                        .child(builder.element(METADATA_NAMESPACE, "title").text("First")),
                builder.element("module")
                        .attr("url", "two.cnxml")
                        .child(builder.element(METADATA_NAMESPACE, "title").text("Second")),
                builder.element("module")
                        .attr("url", "three.cnxml")
                        .child(builder.element(METADATA_NAMESPACE, "title").text("Third"))
        )).build();
        final SoyListData itemList = generator.extractItemsFromCollection(doc);

        assertEquals(3, itemList.length());
        for (SoyData data : itemList) {
            assertTrue(data instanceof SoyMapData);
            assertEquals("module", ((SoyMapData)data).getString("type"));
            assertNotNull(((SoyMapData)data).get("title"));
            assertNotNull(((SoyMapData)data).get("uri"));
        }
        assertEquals("First", itemList.getMapData(0).getString("title"));
        assertEquals("one.cnxml", itemList.getMapData(0).getString("uri"));
        assertEquals("Second", itemList.getMapData(1).getString("title"));
        assertEquals("two.cnxml", itemList.getMapData(1).getString("uri"));
        assertEquals("Third", itemList.getMapData(2).getString("title"));
        assertEquals("three.cnxml", itemList.getMapData(2).getString("uri"));
    }

    @Test public void itemExtractionShouldFindFlatSubcollections() throws Exception {
        final Document doc = (Document)builder.child(builder.element("collection").wrapContent(
                builder.element("subcollection")
                        .child(builder.element(METADATA_NAMESPACE, "title").text("First"))
                        .child(builder.element("content")),
                builder.element("subcollection")
                        .child(builder.element(METADATA_NAMESPACE, "title").text("Second"))
                        .child(builder.element("content")),
                builder.element("subcollection")
                        .child(builder.element(METADATA_NAMESPACE, "title").text("Third"))
                        .child(builder.element("content"))
        )).build();
        final SoyListData itemList = generator.extractItemsFromCollection(doc);

        assertEquals(3, itemList.length());
        for (SoyData data : itemList) {
            assertTrue(data instanceof SoyMapData);
            assertEquals("subcollection", ((SoyMapData)data).getString("type"));
            assertNotNull(((SoyMapData)data).get("title"));
            assertNotNull(((SoyMapData)data).get("items"));
            assertTrue(((SoyMapData)data).get("items") instanceof SoyListData);
            assertEquals(0, ((SoyMapData)data).getListData("items").length());
        }
        assertEquals("First", itemList.getMapData(0).getString("title"));
        assertEquals("Second", itemList.getMapData(1).getString("title"));
        assertEquals("Third", itemList.getMapData(2).getString("title"));
    }

    @Test public void itemExtractionShouldFindTree() throws Exception {
        final Document doc = (Document)builder.child(builder.element("collection").wrapContent(
                builder.element("subcollection")
                        .child(builder.element(METADATA_NAMESPACE, "title").text("First"))
                        .wrapContent(
                                builder.element("module")
                                        .attr("url", "sub.cnxml")
                                        .child(builder.element(METADATA_NAMESPACE, "title")
                                                .text("Submodule"))
                        ),
                builder.element("module")
                        .attr("url", "two.cnxml")
                        .child(builder.element(METADATA_NAMESPACE, "title").text("Second"))
        )).build();
        final SoyListData itemList = generator.extractItemsFromCollection(doc);

        assertEquals(2, itemList.length());
        for (SoyData data : itemList) {
            assertTrue(data instanceof SoyMapData);
            assertNotNull(((SoyMapData)data).get("title"));
        }

        assertEquals("subcollection", itemList.getMapData(0).getString("type"));
        assertEquals("First", itemList.getMapData(0).getString("title"));
        assertTrue(itemList.getMapData(0).get("items") instanceof SoyListData);
        assertEquals(1, itemList.getMapData(0).getListData("items").length());

        assertTrue(itemList.getMapData(0).getListData("items").get(0) instanceof SoyMapData);
        final SoyMapData submodule = itemList.getMapData(0).getListData("items").getMapData(0);
        assertEquals("module", submodule.getString("type"));
        assertEquals("Submodule", submodule.getString("title"));
        assertEquals("sub.cnxml", submodule.getString("uri"));

        assertEquals("module", itemList.getMapData(1).getString("type"));
        assertEquals("Second", itemList.getMapData(1).getString("title"));
    }
}
