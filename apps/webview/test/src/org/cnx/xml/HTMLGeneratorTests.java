/*
    Copyright 2011 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.cnx.xml;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import static org.junit.Assert.*;

public class HTMLGeneratorTests {
  private Document doc;

  @Before
  public void createDoc() throws Exception {
    doc = CNXML.getBuilder().newDocument();
  }

  private String generate(final Node node) throws IOException {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw);
    try {
      HTMLGenerator.generate(node, pw);
    } finally {
      pw.close();
      sw.close();
    }
    return sw.toString();
  }

  @Test
  public void newDocumentShouldBeEmpty() throws IOException {
    final Element root = doc.createElementNS(CNXML.NAMESPACE, "document");
    root.setAttribute("id", "hello");
    doc.appendChild(root);

    final Element title = doc.createElementNS(CNXML.NAMESPACE, "title");
    title.appendChild(doc.createTextNode("Hello, World!"));
    root.appendChild(title);
    root.appendChild(doc.createElementNS(CNXML.NAMESPACE, "content"));

    assertEquals("", generate(doc));
  }

  @Test
  public void fullDocumentShouldBeShown() throws IOException {
    final Element root = doc.createElementNS(CNXML.NAMESPACE, "document");
    root.setAttribute("id", "hello");
    doc.appendChild(root);

    final Element title = doc.createElementNS(CNXML.NAMESPACE, "title");
    title.appendChild(doc.createTextNode("Hello, World!"));
    root.appendChild(title);

    final Element content = doc.createElementNS(CNXML.NAMESPACE, "content");
    content.appendChild(doc.createTextNode("My Content"));
    root.appendChild(content);

    assertEquals("My Content", generate(doc));
  }

  @Test
  public void textShouldBeCopied() throws IOException {
    final String s = "Hello, 世界!";
    assertEquals(s, generate(doc.createTextNode(s)));
  }

  @Test
  public void textShouldBeEscaped() throws IOException {
    final String s = "I am a \"<b>leet hacker</b>\"";
    assertEquals("I am a &quot;&lt;b&gt;leet hacker&lt;/b&gt;&quot;", generate(doc.createTextNode(s)));
  }

  @Test
  public void emptyParagraphTags() throws IOException {
    Element para = doc.createElementNS(CNXML.NAMESPACE, "para");
    para.setAttribute("id", "mypara");
    assertEquals("<p id=\"mypara\"></p>", generate(para));
  }

  @Test
  public void paragraphShouldWrapChildren() throws IOException {
    final Element para = doc.createElementNS(CNXML.NAMESPACE, "para");
    final String s1 = "Hello, ";
    final String s2 = "World!";
    para.setAttribute("id", "mypara");
    para.appendChild(doc.createTextNode(s1));
    para.appendChild(doc.createTextNode(s2));
    assertEquals("<p id=\"mypara\">Hello, World!</p>", generate(para));
  }

  @Test
  public void emptySectionTags() throws IOException {
    final Element sect = doc.createElementNS(CNXML.NAMESPACE, "section");
    final Element title = doc.createElementNS(CNXML.NAMESPACE, "title");
    sect.setAttribute("id", "mysect");
    title.appendChild(doc.createTextNode("My Section"));
    sect.appendChild(title);
    assertEquals("<section id=\"mysect\"><h1>My Section</h1></section>", generate(sect));
  }

  @Test
  public void sectionShouldWrapChildren() throws IOException {
    final Element sect = doc.createElementNS(CNXML.NAMESPACE, "section");
    final Element title = doc.createElementNS(CNXML.NAMESPACE, "title");
    final String s1 = "Hello, ";
    final String s2 = "World!";
    sect.setAttribute("id", "xyzzy");
    title.appendChild(doc.createTextNode("My Magic Section"));
    sect.appendChild(title);
    sect.appendChild(doc.createTextNode(s1));
    sect.appendChild(doc.createTextNode(s2));
    assertEquals("<section id=\"xyzzy\"><h1>My Magic Section</h1>Hello, World!</section>", generate(sect));
  }

  @Test
  public void cleanAttributeNameShouldNotModifyIdentifiers() {
    final String name = "fooBar_42";
    assertEquals(name, HTMLGenerator.xmlAttributeNameToSoyIdentifier(name));
  }

  @Test
  public void cleanAttributeNameShouldConvertHyphensToUnderscores() {
    assertEquals("target_id", HTMLGenerator.xmlAttributeNameToSoyIdentifier("target-id"));
  }

  @Test
  public void cleanAttributeNameShouldRemoveSpecials() {
    assertEquals("xmlnsbib", HTMLGenerator.xmlAttributeNameToSoyIdentifier("xmlns:bib"));
  }
}
