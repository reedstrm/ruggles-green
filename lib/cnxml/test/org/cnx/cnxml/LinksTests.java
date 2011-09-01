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
import static org.junit.Assert.*;

import org.junit.Test;

public class LinksTests {
    @Test
    public void linkShouldPassThroughURI() throws Exception {
        assertEquals(new URI("http://www.example.com/?foo=bar#baz"),
                Links.convertLinkAttributeToUri("http://www.example.com/?foo=bar#baz"));
    }

    @Test
    public void linkShouldNotEscapePercents() throws Exception {
        assertEquals(new URI("http://www.example.com/foo%20bar"),
                Links.convertLinkAttributeToUri("http://www.example.com/foo%20bar"));
    }

    @Test
    public void linkShouldEscapeSpaces() throws Exception {
        assertEquals(new URI("http://www.example.com/foo%20bar.png"),
                Links.convertLinkAttributeToUri("http://www.example.com/foo bar.png"));
    }

    @Test
    public void linkShouldEscapeUnicode() throws Exception {
        assertEquals(new URI("http://www.example.com/%C3%A1bc"),
                Links.convertLinkAttributeToUri("http://www.example.com/\u00e1bc"));
        assertEquals(new URI("http://www.example.com/%E4%B8%96%E7%95%8C"),
                Links.convertLinkAttributeToUri("http://www.example.com/\u4e16\u754c"));
    }

    @Test
    public void linkShouldAllowRelativeURIs() throws Exception {
        assertEquals(new URI("a/b/c"),
                Links.convertLinkAttributeToUri("a/b/c"));
    }

    @Test
    public void basicFileNameShouldNotBeEscaped() throws Exception {
        assertEquals(new URI("a.png"),
                Links.convertFileNameToUri("a.png"));
    }

    @Test
    public void fileNameShouldEscapeSpaces() throws Exception {
        assertEquals(new URI("foo%20bar%20%20baz.png"),
                Links.convertFileNameToUri("foo bar  baz.png"));
    }

    @Test
    public void fileNameShouldEscapeSlashes() throws Exception {
        assertEquals(new URI("foo%2Fbar.png"),
                Links.convertFileNameToUri("foo/bar.png"));
    }

    @Test
    public void fileNameShouldEscapeUnicode() throws Exception {
        assertEquals(new URI("%C3%A1bc"),
                Links.convertLinkAttributeToUri("\u00e1bc"));
        assertEquals(new URI("%E4%B8%96%E7%95%8C"),
                Links.convertLinkAttributeToUri("\u4e16\u754c"));
    }
}
