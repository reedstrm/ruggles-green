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

package org.cnx.cnxml;

import java.net.URI;

import org.cnx.cnxml.LinkProcessor;
import org.cnx.cnxml.ResourceResolver;
import org.cnx.util.DocumentBuilderProvider;
import org.cnx.util.testing.DOMBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import static org.junit.Assert.*;

public class LinkProcessorTests {
    private LinkProcessor processor;
    private DOMBuilder builder;

    private static class MockResourceResolver implements ResourceResolver {
        @Override public URI resolveURI(URI uri) throws Exception {
            return new URI("test", "uri", uri.toString());
        }

        @Override public URI resolveDocument(String document, String version) throws Exception {
            assert document != null || version != null;
            if (document == null) {
                document = "current";
            }
            return new URI("document", document, version);
        }

        @Override public URI resolveResource(String document, String version, String resource)
                throws Exception {
            assert resource != null;
            if (document == null) {
                document = "current";
            }
            if (version == null) {
                return new URI("resource", document, resource);
            } else {
                return new URI("resource", document + ":" + version, resource);
            }
        }
    }

    @Before public void createLinkProcessor() {
        processor = new LinkProcessor(new MockResourceResolver(), "cnxml");
    }

    @Before public void createBuilder() throws Exception {
        builder = new DOMBuilder(new DocumentBuilderProvider().get().newDocument(), "cnxml");
    }

    private URI processLink(final Node node) throws Exception {
        final Node output = processor.process(node);
        assertSame(node, output);
        assertTrue(output instanceof Element);
        final Element elem = (Element)output;
        assertTrue(elem.hasAttribute("url"));
        assertFalse(elem.hasAttribute("target-id"));
        assertFalse(elem.hasAttribute("document"));
        assertFalse(elem.hasAttribute("resource"));
        assertFalse(elem.hasAttribute("version"));
        return new URI(elem.getAttribute("url"));
    }

    private URI processMedia(final Node node) throws Exception {
        final Node output = processor.process(node);
        assertSame(node, output);
        assertTrue(output instanceof Element);
        final Element elem = (Element)output;
        assertTrue(elem.hasAttribute("src"));
        return new URI(elem.getAttribute("src"));
    }

    private URI processLink(final DOMBuilder builder) throws Exception {
        return processLink(builder.build());
    }

    private URI processMedia(final DOMBuilder builder) throws Exception {
        return processMedia(builder.build());
    }

    @Test public void normalNodeShouldPassThrough() throws Exception {
        final Node node = builder.element("para").attr("id", "foo").text("Hello").build();
        assertSame(node, processor.process(node));
    }

    @Test public void linkNodeShouldPassThrough() throws Exception {
        final Node node = builder.element("link").attr("url", "#foo").text("Bar").build();
        assertSame(node, processor.process(node));
    }

    @Test public void linkURLShouldBeResolved() throws Exception {
        final URI uri = processLink(builder.element("link").attr("url", "http://www.example.com/"));
        assertEquals(new URI("test", "uri", "http://www.example.com/"), uri);
    }

    @Test public void linkTargetIdShouldBeResolved() throws Exception {
        final URI uri = processLink(builder.element("link").attr("target-id", "anchor"));
        assertEquals(new URI("test", "uri", "#anchor"), uri);
    }

    @Test public void linkDocumentShouldBeResolved() throws Exception {
        URI uri;
        uri = processLink(builder.element("link").attr("document", "constitution"));
        assertEquals(new URI("document", "constitution", null), uri);
        uri = processLink(builder.element("link").attr("version", "1776"));
        assertEquals(new URI("document", "current", "1776"), uri);
        uri = processLink(builder.element("link")
                .attr("document", "constitution").attr("version", "1776"));
        assertEquals(new URI("document", "constitution", "1776"), uri);
    }

    @Test public void linkResourceShouldBeResolved() throws Exception {
        URI uri;
        uri = processLink(builder.element("link").attr("resource", "foo.png"));
        assertEquals(new URI("resource", "current", "foo.png"), uri);
        uri = processLink(builder.element("link")
                .attr("document", "mydoc").attr("resource", "foo.png"));
        assertEquals(new URI("resource", "mydoc", "foo.png"), uri);
        uri = processLink(builder.element("link")
                .attr("version", "123").attr("resource", "foo.png"));
        assertEquals(new URI("resource", "current:123", "foo.png"), uri);
        uri = processLink(builder.element("link")
                .attr("document", "mydoc").attr("version", "123").attr("resource", "foo.png"));
        assertEquals(new URI("resource", "mydoc:123", "foo.png"), uri);
    }

    @Test public void relativeMediaSourceShouldBeResolved() throws Exception {
        URI uri;
        uri = processMedia(builder.element("image").attr("src", "foo.png"));
        assertEquals(new URI("test", "uri", "foo.png"), uri);
    }

    @Test public void absoluteMediaSourceShouldBeResolved() throws Exception {
        URI uri;
        uri = processMedia(builder.element("image").attr("src", "http://www.example.com/foo.png"));
        assertEquals(new URI("test", "uri", "http://www.example.com/foo.png"), uri);
    }
}
