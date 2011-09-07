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

package org.cnx.util;

import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class JdomHtmlSerializerTests {
    private JdomHtmlSerializer serializer;

    @Before
    public void createSerializer() {
        serializer = new JdomHtmlSerializer();
    }

    @Test
    public void textShouldBeText() {
        assertEquals("Hello, World!", serializer.serialize(new Text("Hello, World!")));
    }

    @Test
    public void textShouldBeEscaped() {
        assertEquals("I am a &quot;&lt;b&gt;leet hacker&lt;/b&gt;&quot;",
                serializer.serialize(new Text("I am a \"<b>leet hacker</b>\"")));
    }

    @Test
    public void textShouldAllowUnicode() {
        final String s = "Hello, \u4e16\u754c!";
        assertEquals(s, serializer.serialize(new Text(s)));
    }

    @Test
    public void emptyParagraphShouldRender() {
        assertEquals("<p></p>", serializer.serialize(new Element("p")));
    }

    @Test
    public void paragraphShouldAllowText() {
        assertEquals("<p>Hello, World!</p>",
                serializer.serialize(new Element("p").setText("Hello, World!")));
    }

    @Test
    public void paragraphShouldAllowElements() {
        assertEquals("<p>Hello, <b>World</b>!</p>", serializer.serialize(new Element("p")
                .addContent("Hello, ")
                .addContent(new Element("b").setText("World"))
                .addContent("!")));
    }

    @Test
    public void paragraphShouldRenderAttributes() {
        assertEquals("<p id=\"greet\">Hello, World!</p>",
                serializer.serialize(new Element("p")
                        .setAttribute("id", "greet")
                        .setText("Hello, World!")));
    }

    @Test
    public void paragraphAttributesShouldBeEscaped() {
        assertEquals("<p id=\"greet&lt;\">Hello, World!</p>",
                serializer.serialize(new Element("p")
                        .setAttribute("id", "greet<")
                        .setText("Hello, World!")));
    }

    @Test
    public void namespacesShouldBeIgnored() {
        final Namespace ns = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");
        assertEquals("<p>Hello, World!</p>",
                serializer.serialize(new Element("p", ns).setText("Hello, World!")));
    }

    @Test
    public void emptyNonHtmlTagsShouldRender() {
        assertEquals("<abc></abc>", serializer.serialize(new Element("abc")));
    }

    @Test
    public void nonHtmlTagsShouldAllowText() {
        assertEquals("<abc>Hello, World!</abc>",
                serializer.serialize(new Element("abc").setText("Hello, World!")));
    }

    @Test
    public void nonHtmlTagsShouldAllowElements() {
        assertEquals("<abc>Hello, <b>World</b>!</abc>", serializer.serialize(new Element("abc")
                .addContent("Hello, ")
                .addContent(new Element("b").setText("World"))
                .addContent("!")));
    }

    @Test
    public void nonHtmlTagsShouldAllowAttributes() {
        assertEquals("<abc def=\"ghi\"></abc>",
                serializer.serialize(new Element("abc").setAttribute("def", "ghi")));
    }

    @Test
    public void voidTagsShouldNotHaveEndTag() {
        assertEquals("<img src=\"example.png\">",
                serializer.serialize(new Element("img").setAttribute("src", "example.png")));
    }

    @Test
    public void voidTagsShouldNotAllowContent() {
        assertEquals("<img src=\"example.png\">",
                serializer.serialize(new Element("img")
                        .setAttribute("src", "example.png")
                        .setText("BAD")));
    }

    @Test
    public void commentsShouldRender() {
        assertEquals("<!--\nhello\n-->", serializer.serialize(new Comment("hello")));
    }
}
