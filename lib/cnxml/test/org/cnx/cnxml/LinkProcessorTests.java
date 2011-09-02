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

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableSet;

import java.net.URI;

import org.cnx.cnxml.LinkProcessor;
import org.cnx.cnxml.LinkResolver;

import org.junit.Before;
import org.junit.Test;

import org.jdom.Element;
import org.jdom.Namespace;

public class LinkProcessorTests {
    private static final Namespace ns = CnxmlTag.NAMESPACE;

    private LinkProcessor processor;

    private static class MockLinkResolver implements LinkResolver {
        @Override
        public URI resolveUri(URI uri) throws Exception {
            return new URI("test", "uri", uri.toString());
        }

        @Override
        public URI resolveDocument(String document, String version) throws Exception {
            assert document != null || version != null;
            if (document == null) {
                document = "current";
            }
            return new URI("document", document, version);
        }

        @Override
        public URI resolveResource(String document, String version, URI resource) throws Exception {
            assert resource != null;
            if (document == null) {
                document = "current";
            }
            if (version == null) {
                return new URI("resource", document, resource.toString());
            } else {
                return new URI("resource", document + ":" + version, resource.toString());
            }
        }
    }

    @Before
    public void createLinkProcessor() {
        processor = new LinkProcessor(new MockLinkResolver());
    }

    private URI processLink(final Element elem) throws Exception {
        processor.resolveLink(elem);
        assertNotNull(elem.getAttributeValue("url"));
        assertNull(elem.getAttributeValue("target-id"));
        assertNull(elem.getAttributeValue("document"));
        assertNull(elem.getAttributeValue("resource"));
        assertNull(elem.getAttributeValue("version"));
        return new URI(elem.getAttributeValue("url"));
    }

    private URI processMedia(final Element elem) throws Exception {
        processor.resolveMedia(elem);
        assertNotNull(elem.getAttributeValue("src"));
        return new URI(elem.getAttributeValue("src"));
    }

    @Test
    public void linkTagsShouldMatchCnxmlSpec() {
        assertEquals(ImmutableSet.of(CnxmlTag.FOREIGN, CnxmlTag.LINK, CnxmlTag.TERM),
                LinkProcessor.LINK_TAGS);
    }

    @Test
    public void linkURLShouldBeResolved() throws Exception {
        final URI uri = processLink(new Element("link", ns)
                .setAttribute("url", "http://www.example.com/"));
        assertEquals(new URI("test", "uri", "http://www.example.com/"), uri);
    }

    @Test
    public void linkTargetIdShouldBeResolved() throws Exception {
        final URI uri = processLink(new Element("link", ns)
                .setAttribute("target-id", "anchor"));
        assertEquals(new URI("test", "uri", "#anchor"), uri);
    }

    @Test
    public void linkDocumentShouldBeResolved() throws Exception {
        URI uri;
        uri = processLink(new Element("link", ns).setAttribute("document", "constitution"));
        assertEquals(new URI("document", "constitution", null), uri);
        uri = processLink(new Element("link", ns).setAttribute("version", "1776"));
        assertEquals(new URI("document", "current", "1776"), uri);
        uri = processLink(new Element("link", ns)
                .setAttribute("document", "constitution")
                .setAttribute("version", "1776"));
        assertEquals(new URI("document", "constitution", "1776"), uri);
    }

    @Test
    public void linkResourceShouldBeResolved() throws Exception {
        URI uri;
        uri = processLink(new Element("link", ns).setAttribute("resource", "foo.png"));
        assertEquals(new URI("resource", "current", "foo.png"), uri);
        uri = processLink(new Element("link", ns)
                .setAttribute("document", "mydoc")
                .setAttribute("resource", "foo.png"));
        assertEquals(new URI("resource", "mydoc", "foo.png"), uri);
        uri = processLink(new Element("link", ns)
                .setAttribute("version", "123")
                .setAttribute("resource", "foo.png"));
        assertEquals(new URI("resource", "current:123", "foo.png"), uri);
        uri = processLink(new Element("link", ns)
                .setAttribute("document", "mydoc")
                .setAttribute("version", "123")
                .setAttribute("resource", "foo.png"));
        assertEquals(new URI("resource", "mydoc:123", "foo.png"), uri);
    }

    @Test
    public void relativeMediaSourceShouldBeResolved() throws Exception {
        URI uri;
        uri = processMedia(new Element("image", ns).setAttribute("src", "foo.png"));
        assertEquals(new URI("test", "uri", "foo.png"), uri);
    }

    @Test
    public void absoluteMediaSourceShouldBeResolved() throws Exception {
        URI uri;
        uri = processMedia(new Element("image", ns)
                .setAttribute("src", "http://www.example.com/foo.png"));
        assertEquals(new URI("test", "uri", "http://www.example.com/foo.png"), uri);
    }
}
