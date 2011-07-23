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

package org.cnx.html;

import com.google.inject.internal.Nullable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import static org.junit.Assert.*;

public class HTMLGeneratorTests {
    private Document doc;
    private static HTMLGenerator generator;

    @Before public void createDoc() throws Exception {
        doc = CNXML.getBuilder().newDocument();
    }

    @BeforeClass public static void createGenerator() {
        generator = new HTMLGenerator();
    }

    private String generate(final Node node) {
        return generator.generate(node);
    }

    @Test public void newDocumentShouldBeEmpty() {
        final Element root = doc.createElementNS(CNXML.NAMESPACE, "document");
        root.setAttribute("id", "hello");
        doc.appendChild(root);

        final Element title = doc.createElementNS(CNXML.NAMESPACE, "title");
        title.appendChild(doc.createTextNode("Hello, World!"));
        root.appendChild(title);
        root.appendChild(doc.createElementNS(CNXML.NAMESPACE, "content"));

        assertEquals("", generate(doc));
    }

    @Test public void fullDocumentShouldBeShown() {
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

    @Test public void textShouldBeCopied() {
        final String s = "Hello, 世界!";
        assertEquals(s, generate(doc.createTextNode(s)));
    }

    @Test public void textShouldBeEscaped() {
        final String s = "I am a \"<b>leet hacker</b>\"";
        assertEquals("I am a &quot;&lt;b&gt;leet hacker&lt;/b&gt;&quot;",
                     generate(doc.createTextNode(s)));
    }

    @Test public void emptyParagraphTags() {
        Element para = doc.createElementNS(CNXML.NAMESPACE, "para");
        para.setAttribute("id", "mypara");
        assertEquals("<p id=\"mypara\"></p>", generate(para));
    }

    @Test public void paragraphShouldWrapChildren() {
        final Element para = doc.createElementNS(CNXML.NAMESPACE, "para");
        final String s1 = "Hello, ";
        final String s2 = "World!";
        para.setAttribute("id", "mypara");
        para.appendChild(doc.createTextNode(s1));
        para.appendChild(doc.createTextNode(s2));
        assertEquals("<p id=\"mypara\">Hello, World!</p>", generate(para));
    }

    @Test public void emptySectionTags() {
        final Element sect = doc.createElementNS(CNXML.NAMESPACE, "section");
        final Element title = doc.createElementNS(CNXML.NAMESPACE, "title");
        sect.setAttribute("id", "mysect");
        title.appendChild(doc.createTextNode("My Section"));
        sect.appendChild(title);
        assertEquals("<section id=\"mysect\"><h1>My Section</h1></section>", generate(sect));
    }

    @Test public void sectionShouldWrapChildren() {
        final Element sect = doc.createElementNS(CNXML.NAMESPACE, "section");
        final Element title = doc.createElementNS(CNXML.NAMESPACE, "title");
        final String s1 = "Hello, ";
        final String s2 = "World!";
        sect.setAttribute("id", "xyzzy");
        title.appendChild(doc.createTextNode("My Magic Section"));
        sect.appendChild(title);
        sect.appendChild(doc.createTextNode(s1));
        sect.appendChild(doc.createTextNode(s2));
        assertEquals("<section id=\"xyzzy\"><h1>My Magic Section</h1>Hello, World!</section>",
                     generate(sect));
    }

    @Test public void cleanAttributeNameShouldNotModifyIdentifiers() {
        final String name = "fooBar_42";
        assertEquals(name, HTMLGenerator.xmlAttributeNameToSoyIdentifier(name));
    }

    @Test public void cleanAttributeNameShouldConvertHyphensToUnderscores() {
        assertEquals("target_id", HTMLGenerator.xmlAttributeNameToSoyIdentifier("target-id"));
    }

    @Test public void cleanAttributeNameShouldRemoveSpecials() {
        assertEquals("xmlnsbib", HTMLGenerator.xmlAttributeNameToSoyIdentifier("xmlns:bib"));
    }

    /**
        createSpan will generate a DOM element for a simple CNXML span-style
        element.    Examples of span elements include <code>foreign</code>,
        <code>term</code>, etc.

        @param tag The tag to test
        @param id ID to attach to CNXML element
        @param text Inner text to place in the element.
        @return The corresponding XML DOM node.
    */
    private Element createSpan(final String tag, @Nullable final String id, final String text) {
        final Element elem = doc.createElementNS(CNXML.NAMESPACE, tag);
        if (id != null) {
            elem.setAttribute("id", id);
        }
        elem.appendChild(doc.createTextNode(text));
        return elem;
    }

    /**
        createEmphasis will generate a DOM element for a CNXML emphasis element.

        @param effect The effect to test
        @param id ID to attach to CNXML element
        @param text Inner text to place in the element.
        @return The corresponding XML DOM node.
    */
    private Element createEmphasis(@Nullable final String effect, @Nullable final String id,
                                   final String text) {
        final Element elem = createSpan("emphasis", id, text);
        if (effect != null) {
            elem.setAttribute("effect", effect);
        }
        return elem;
    }

    @Test public void defaultEmphasisShouldBeStrong() {
        assertEquals("<strong>Hello</strong>", generate(createEmphasis(null, null, "Hello")));
        assertEquals("<strong id=\"myid\">Hello</strong>",
                     generate(createEmphasis(null, "myid", "Hello")));
    }

    @Test public void boldEmphasisShouldBeStrong() {
        assertEquals("<strong>Hello</strong>", generate(createEmphasis("bold", null, "Hello")));
        assertEquals("<strong id=\"myid\">Hello</strong>",
                     generate(createEmphasis("bold", "myid", "Hello")));
    }

    @Test public void italicsEmphasisShouldBeEm() {
        assertEquals("<em>Hello</em>", generate(createEmphasis("italics", null, "Hello")));
        assertEquals("<em id=\"myid\">Hello</em>",
                     generate(createEmphasis("italics", "myid", "Hello")));
    }

    @Test public void underlineEmphasisShouldBeU() {
        assertEquals("<u>Hello</u>", generate(createEmphasis("underline", null, "Hello")));
        assertEquals("<u id=\"myid\">Hello</u>",
                     generate(createEmphasis("underline", "myid", "Hello")));
    }

    @Test public void smallcapsEmphasisShouldBeSpan() {
        assertEquals("<span class=\"smallcaps\">Hello</span>",
                     generate(createEmphasis("smallcaps", null, "Hello")));
        assertEquals("<span class=\"smallcaps\" id=\"myid\">Hello</span>",
                     generate(createEmphasis("smallcaps", "myid", "Hello")));
    }

    @Test public void normalEmphasisShouldBeSpan() {
        assertEquals("<span class=\"normal\">Hello</span>",
                     generate(createEmphasis("normal", null, "Hello")));
        assertEquals("<span class=\"normal\" id=\"myid\">Hello</span>",
                     generate(createEmphasis("normal", "myid", "Hello")));
    }

    @Test public void foreignShouldRenderAsSpan() {
        assertEquals("<span class=\"foreign\">¡Hola, mundo!</span>",
                     generate(createSpan("foreign", null, "¡Hola, mundo!")));
        assertEquals("<span class=\"foreign\" id=\"myid\">¡Hola, mundo!</span>",
                     generate(createSpan("foreign", "myid", "¡Hola, mundo!")));
    }

    @Test public void foreignShouldAllowUrlLinks() {
        final Element elem = createSpan("foreign", null, "¡Hola, mundo!");
        elem.setAttribute("url", "http://www.example.com/");
        assertEquals("<span class=\"foreign\"><a href=\"http://www.example.com/\">¡Hola, mundo!</a></span>",
                     generate(elem));
    }

    @Test public void foreignShouldAllowAnchorLinks() {
        final Element elem = createSpan("foreign", null, "¡Hola, mundo!");
        elem.setAttribute("target-id", "myRefId");
        assertEquals("<span class=\"foreign\"><a href=\"#myRefId\">¡Hola, mundo!</a></span>",
                     generate(elem));
    }

    @Test public void termShouldRenderAsSpan() {
        assertEquals("<span class=\"term\">jargon</span>",
                     generate(createSpan("term", null, "jargon")));
        assertEquals("<span class=\"term\" id=\"myid\">jargon</span>",
                     generate(createSpan("term", "myid", "jargon")));
    }

    @Test public void termShouldAllowUrlLinks() {
        final Element elem = createSpan("term", null, "jargon");
        elem.setAttribute("url", "http://www.example.com/");
        assertEquals("<span class=\"term\"><a href=\"http://www.example.com/\">jargon</a></span>",
                     generate(elem));
    }

    @Test public void termShouldAllowAnchorLinks() {
        final Element elem = createSpan("term", null, "jargon");
        elem.setAttribute("target-id", "myRefId");
        assertEquals("<span class=\"term\"><a href=\"#myRefId\">jargon</a></span>",
                     generate(elem));
    }

    @Test public void supShouldRenderAsSup() {
        assertEquals("<sup>exponent</sup>", generate(createSpan("sup", null, "exponent")));
        assertEquals("<sup id=\"myid\">exponent</sup>",
                     generate(createSpan("sup", "myid", "exponent")));
    }

    @Test public void subShouldRenderAsSub() {
        assertEquals("<sub>index</sub>", generate(createSpan("sub", null, "index")));
        assertEquals("<sub id=\"myid\">index</sub>",
                     generate(createSpan("sub", "myid", "index")));
    }

    @Test public void preformatShouldRenderAsPre() {
        assertEquals("<pre>my\n text</pre>", generate(createSpan("preformat", null, "my\n text")));
        assertEquals("<pre id=\"myid\">my\n text</pre>",
                     generate(createSpan("preformat", "myid", "my\n text")));
    }

    @Test public void defaultNewlineShouldRenderBr() {
        final Element elem = doc.createElementNS(CNXML.NAMESPACE, "newline");
        assertEquals("<br>", generate(elem));
    }

    @Test public void normalNewlineShouldRenderBr() {
        final Element elem = doc.createElementNS(CNXML.NAMESPACE, "newline");
        elem.setAttribute("effect", "normal");
        assertEquals("<br>", generate(elem));
    }

    @Test public void underlineNewlineShouldRenderHr() {
        final Element elem = doc.createElementNS(CNXML.NAMESPACE, "newline");
        elem.setAttribute("effect", "underline");
        assertEquals("<hr>", generate(elem));
    }

    @Test public void newlineShouldHonorCount() {
        final Element elem = doc.createElementNS(CNXML.NAMESPACE, "newline");
        elem.setAttribute("count", "3");
        assertEquals("<br><br><br>", generate(elem));
    }

    @Test public void underlineNewlineShouldHonorCount() {
        final Element elem = doc.createElementNS(CNXML.NAMESPACE, "newline");
        elem.setAttribute("effect", "underline");
        elem.setAttribute("count", "3");
        assertEquals("<hr><hr><hr>", generate(elem));
    }

    @Test public void ruleShouldAllowStatements() {
        final Element rule = doc.createElementNS(CNXML.NAMESPACE, "rule");
        rule.setAttribute("id", "my-rule");
        final Element stmt = doc.createElementNS(CNXML.NAMESPACE, "statement");
        stmt.setAttribute("id", "my-rule-statement");
        stmt.appendChild(doc.createTextNode("QED"));
        rule.appendChild(stmt);
        assertEquals("<div class=\"rule\" id=\"my-rule\"><h2>Rule 1</h2><div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                     generate(rule));
    }

    @Test public void ruleShouldUseTitles() {
        final Element rule = doc.createElementNS(CNXML.NAMESPACE, "rule");
        rule.setAttribute("id", "my-rule");
        final Element title = doc.createElementNS(CNXML.NAMESPACE, "title");
        title.appendChild(doc.createTextNode("The Best Rule"));
        rule.appendChild(title);
        final Element stmt = doc.createElementNS(CNXML.NAMESPACE, "statement");
        stmt.setAttribute("id", "my-rule-statement");
        stmt.appendChild(doc.createTextNode("QED"));
        rule.appendChild(stmt);
        assertEquals("<div class=\"rule\" id=\"my-rule\"><h2>Rule 1: The Best Rule</h2><div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                     generate(rule));
    }

    @Test public void ruleShouldUseLabels() {
        final Element rule = doc.createElementNS(CNXML.NAMESPACE, "rule");
        rule.setAttribute("id", "my-rule");
        final Element label = doc.createElementNS(CNXML.NAMESPACE, "label");
        label.appendChild(doc.createTextNode("Ultimate Rule"));
        rule.appendChild(label);
        final Element stmt = doc.createElementNS(CNXML.NAMESPACE, "statement");
        stmt.setAttribute("id", "my-rule-statement");
        stmt.appendChild(doc.createTextNode("QED"));
        rule.appendChild(stmt);
        assertEquals("<div class=\"rule\" id=\"my-rule\"><h2>Ultimate Rule 1</h2><div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                     generate(rule));
    }

    @Test public void ruleHeadingShouldChangeForType() {
        final Element rule = doc.createElementNS(CNXML.NAMESPACE, "rule");
        rule.setAttribute("id", "my-rule");
        rule.setAttribute("type", "law");
        final Element stmt = doc.createElementNS(CNXML.NAMESPACE, "statement");
        stmt.setAttribute("id", "my-rule-statement");
        stmt.appendChild(doc.createTextNode("QED"));
        rule.appendChild(stmt);
        assertEquals("<div class=\"rule\" id=\"my-rule\"><h2>Law 1</h2><div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                     generate(rule));
    }

    @Test public void ruleHeadingShouldIncrementCounter() {
        Element rule1, rule2;
        Element stmt;
        Element content;

        rule1 = doc.createElementNS(CNXML.NAMESPACE, "rule");
        rule1.setAttribute("id", "rule1");
        stmt = doc.createElementNS(CNXML.NAMESPACE, "statement");
        stmt.setAttribute("id", "stmt1");
        stmt.appendChild(doc.createTextNode("QED"));
        rule1.appendChild(stmt);

        rule2 = doc.createElementNS(CNXML.NAMESPACE, "rule");
        rule2.setAttribute("id", "rule2");
        stmt = doc.createElementNS(CNXML.NAMESPACE, "statement");
        stmt.setAttribute("id", "stmt2");
        stmt.appendChild(doc.createTextNode("QED"));
        rule2.appendChild(stmt);

        content = doc.createElementNS(CNXML.NAMESPACE, "content");
        content.appendChild(rule1);
        content.appendChild(rule2);

        doc.appendChild(doc.createElementNS(CNXML.NAMESPACE, "document"));
        doc.getDocumentElement().appendChild(content);

        assertEquals("<div class=\"rule\" id=\"rule1\"><h2>Rule 1</h2>"
                     + "<div class=\"statement\" id=\"stmt1\">QED</div></div>"
                     + "<div class=\"rule\" id=\"rule2\"><h2>Rule 2</h2>"
                     + "<div class=\"statement\" id=\"stmt2\">QED</div></div>",
                     generate(doc));
    }

    @Test public void ruleTypesShouldCountSeparately() {
        Element rule1, rule2;
        Element stmt;
        Element content;

        rule1 = doc.createElementNS(CNXML.NAMESPACE, "rule");
        rule1.setAttribute("id", "rule1");
        rule1.setAttribute("type", "rule");
        stmt = doc.createElementNS(CNXML.NAMESPACE, "statement");
        stmt.setAttribute("id", "stmt1");
        stmt.appendChild(doc.createTextNode("QED"));
        rule1.appendChild(stmt);

        rule2 = doc.createElementNS(CNXML.NAMESPACE, "rule");
        rule2.setAttribute("id", "rule2");
        rule2.setAttribute("type", "law");
        stmt = doc.createElementNS(CNXML.NAMESPACE, "statement");
        stmt.setAttribute("id", "stmt2");
        stmt.appendChild(doc.createTextNode("QED"));
        rule2.appendChild(stmt);

        content = doc.createElementNS(CNXML.NAMESPACE, "content");
        content.appendChild(rule1);
        content.appendChild(rule2);

        doc.appendChild(doc.createElementNS(CNXML.NAMESPACE, "document"));
        doc.getDocumentElement().appendChild(content);

        assertEquals("<div class=\"rule\" id=\"rule1\"><h2>Rule 1</h2>"
                     + "<div class=\"statement\" id=\"stmt1\">QED</div></div>"
                     + "<div class=\"rule\" id=\"rule2\"><h2>Law 1</h2>"
                     + "<div class=\"statement\" id=\"stmt2\">QED</div></div>",
                     generate(doc));
    }

    @Test public void ruleShouldAllowProofs() {
        final Element rule = doc.createElementNS(CNXML.NAMESPACE, "rule");
        rule.setAttribute("id", "my-rule");
        final Element stmt = doc.createElementNS(CNXML.NAMESPACE, "statement");
        stmt.setAttribute("id", "my-rule-statement");
        stmt.appendChild(doc.createTextNode("I exist."));
        rule.appendChild(stmt);
        final Element proof = doc.createElementNS(CNXML.NAMESPACE, "proof");
        proof.setAttribute("id", "my-rule-proof");
        proof.appendChild(doc.createTextNode("QED"));
        rule.appendChild(proof);
        assertEquals("<div class=\"rule\" id=\"my-rule\"><h2>Rule 1</h2>"
                     + "<div class=\"statement\" id=\"my-rule-statement\">I exist.</div>"
                     + "<div class=\"proof\" id=\"my-rule-proof\"><h2>Proof</h2>QED</div>"
                     + "</div>",
                     generate(rule));
    }

    @Test public void exampleShouldRenderAsDiv() {
        final Element elem = doc.createElementNS(CNXML.NAMESPACE, "example");
        elem.setAttribute("id", "my-example");
        elem.appendChild(doc.createTextNode("For example, ..."));
        assertEquals("<div class=\"example\" id=\"my-example\"><h2>Example 1</h2>"
                     + "For example, ...</div>",
                     generate(elem));
    }

    @Test public void exampleShouldAcceptTitle() {
        final Element elem = doc.createElementNS(CNXML.NAMESPACE, "example");
        elem.setAttribute("id", "my-example");
        final Element title = doc.createElementNS(CNXML.NAMESPACE, "title");
        title.appendChild(doc.createTextNode("The Best One"));
        elem.appendChild(title);
        elem.appendChild(doc.createTextNode("For example, ..."));
        assertEquals("<div class=\"example\" id=\"my-example\"><h2>Example 1: The Best One</h2>"
                     + "For example, ...</div>",
                     generate(elem));
    }

    @Test public void exampleShouldAcceptLabel() {
        final Element elem = doc.createElementNS(CNXML.NAMESPACE, "example");
        elem.setAttribute("id", "my-example");
        final Element label = doc.createElementNS(CNXML.NAMESPACE, "label");
        label.appendChild(doc.createTextNode("Prime Example"));
        elem.appendChild(label);
        elem.appendChild(doc.createTextNode("For example, ..."));
        assertEquals("<div class=\"example\" id=\"my-example\"><h2>Prime Example 1</h2>"
                     + "For example, ...</div>",
                     generate(elem));
    }

    @Test public void exerciseShouldUseProblem() {
        final Element exercise = doc.createElementNS(CNXML.NAMESPACE, "exercise");
        exercise.setAttribute("id", "texas-exercise");
        final Element problem = doc.createElementNS(CNXML.NAMESPACE, "problem");
        problem.setAttribute("id", "texas-problem");
        problem.appendChild(doc.createTextNode("What is the capital of Texas?"));
        exercise.appendChild(problem);
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\"><h2>Exercise 1</h2>"
                     + "<div class=\"problem\" id=\"texas-problem\">What is the capital of Texas?"
                     + "</div></div>",
                     generate(exercise));
    }

    @Test public void exerciseShouldUseSolution() {
        final Element exercise = doc.createElementNS(CNXML.NAMESPACE, "exercise");
        exercise.setAttribute("id", "texas-exercise");
        final Element problem = doc.createElementNS(CNXML.NAMESPACE, "problem");
        problem.setAttribute("id", "texas-problem");
        problem.appendChild(doc.createTextNode("What is the capital of Texas?"));
        exercise.appendChild(problem);
        final Element solution = doc.createElementNS(CNXML.NAMESPACE, "solution");
        solution.setAttribute("id", "texas-solution");
        solution.appendChild(doc.createTextNode("Austin"));
        exercise.appendChild(solution);
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\"><h2>Exercise 1</h2>"
                     + "<div class=\"problem\" id=\"texas-problem\">What is the capital of Texas?"
                     + "</div><div class=\"solution\" id=\"texas-solution\">Austin</div></div>",
                     generate(exercise));
    }

    @Test public void exerciseShouldUseCommentary() {
        final Element exercise = doc.createElementNS(CNXML.NAMESPACE, "exercise");
        exercise.setAttribute("id", "texas-exercise");
        final Element problem = doc.createElementNS(CNXML.NAMESPACE, "problem");
        problem.setAttribute("id", "texas-problem");
        problem.appendChild(doc.createTextNode("What is the capital of Texas?"));
        exercise.appendChild(problem);
        final Element commentary = doc.createElementNS(CNXML.NAMESPACE, "commentary");
        commentary.setAttribute("id", "texas-commentary");
        commentary.appendChild(doc.createTextNode("This will be on the final exam."));
        exercise.appendChild(commentary);
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\"><h2>Exercise 1</h2>"
                     + "<div class=\"problem\" id=\"texas-problem\">What is the capital of Texas?"
                     + "</div><div class=\"commentary\" id=\"texas-commentary\">"
                     + "This will be on the final exam.</div></div>",
                     generate(exercise));
    }

    @Test public void equationShouldWrapContent() {
        final Element elem = doc.createElementNS(CNXML.NAMESPACE, "equation");
        elem.setAttribute("id", "my-equation");
        elem.appendChild(doc.createTextNode("2 + 2 = 4"));
        assertEquals("<div class=\"equation\" id=\"my-equation\">"
                     + "<h2>Equation 1</h2>2 + 2 = 4</div>",
                     generate(elem));
    }
}
