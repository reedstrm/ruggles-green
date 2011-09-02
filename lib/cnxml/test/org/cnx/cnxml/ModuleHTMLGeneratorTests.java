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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

import org.cnx.cnxml.CnxmlModule;
import org.cnx.cnxml.ModuleHTMLGenerator;
import org.cnx.mdml.MdmlModule;
import org.cnx.resourcemapping.ObjectFactory;
import org.cnx.util.RenderScope;
import org.cnx.util.UtilModule;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;

public class ModuleHTMLGeneratorTests {
    private static final String moduleId = "m123";
    private static Injector injector;
    private Namespace ns = CnxmlTag.NAMESPACE;

    @BeforeClass public static void createInjector() {
        injector = Guice.createInjector(
                new CnxmlModule(),
                new MdmlModule(),
                new UtilModule()
        );
    }

    private String generate(final Content content) throws Exception {
        final Document doc = new Document(new Element("document", ns)
                .setAttribute("id", moduleId)
                .addContent(new Element("content", ns).addContent(content)));
        final String result = generate(doc);
        content.detach();
        return result;
    }

    private String generate(final Document d) throws Exception {
        final RenderScope scope = injector.getInstance(RenderScope.class);
        scope.enter();
        try {
            ObjectFactory dummyFactory = new ObjectFactory();
            return injector.getInstance(ModuleHTMLGenerator.class).generate(
                    new Module(moduleId, d, dummyFactory.createResources(), null));
        } finally {
            scope.exit();
        }
    }

    @Test public void newDocumentShouldBeEmpty() throws Exception {
        final Document doc = new Document(new Element("document", ns)
                .setAttribute("id", moduleId)
                .addContent(new Element("title", ns).setText("Hello, World!"))
                .addContent(new Element("content", ns)));
        assertEquals("", generate(doc));
    }

    @Test public void fullDocumentShouldBeShown() throws Exception {
        final Document doc = new Document(new Element("document", ns)
                .setAttribute("id", moduleId)
                .addContent(new Element("title", ns).setText("Hello, World!"))
                .addContent(new Element("content", ns).setText("My Content")));
        assertEquals("My Content", generate(doc));
    }

    @Test public void textShouldBeCopied() throws Exception {
        final String s = "Hello, \u4e16\u754c!";
        assertEquals(s, generate(new Text(s)));
    }

    @Test public void textShouldBeEscaped() throws Exception {
        assertEquals("I am a &quot;&lt;b&gt;leet hacker&lt;/b&gt;&quot;",
                generate(new Text("I am a \"<b>leet hacker</b>\"")));
    }

    @Test public void unhandledContentShouldBeReported() throws Exception {
        assertEquals("<!--\n&lt;greeting&gt;...&lt;/greeting&gt;\n-->"
                + "<div class=\"unhandled\">Unrecognized Content: greeting</div>",
                generate(new Element("greeting", ns).setText("Hello, World")));
    }

    @Test public void emptyParagraphTags() throws Exception {
        assertEquals("<p id=\"mypara\"></p>",
                generate(new Element("para", ns).setAttribute("id", "mypara")));
    }

    @Test public void paragraphShouldWrapChildren() throws Exception {
        assertEquals("<p id=\"mypara\">Hello, World!</p>",
                generate(new Element("para", ns)
                        .setAttribute("id", "mypara")
                        .setText("Hello, World!")));
    }

    @Test public void paragraphShouldShowTitle() throws Exception {
        assertEquals("<div class=\"title\">My Paragraph</div><p id=\"mypara\">Hello, World!</p>",
                generate(new Element("para", ns)
                        .setAttribute("id", "mypara")
                        .addContent(new Element("title", ns).setText("My Paragraph"))
                        .addContent(new Text("Hello, World!"))));
    }

    @Test public void emptySectionTags() throws Exception {
        assertEquals("<section id=\"mysect\"><h1>My Section</h1></section>",
                generate(new Element("section", ns)
                        .setAttribute("id", "mysect")
                        .addContent(new Element("title", ns).setText("My Section"))));
    }

    @Test public void sectionShouldWrapChildren() throws Exception {
        assertEquals("<section id=\"xyzzy\"><h1>My Magic Section</h1>Hello, World!</section>",
                generate(new Element("section", ns)
                        .setAttribute("id", "xyzzy")
                        .addContent(new Element("title", ns).setText("My Magic Section"))
                        .addContent(new Text("Hello, World!"))));
    }

    @Test public void sectionTitleShouldBeOptional() throws Exception {
        assertEquals("<section id=\"xyzzy\">Hello, World!</section>",
                generate(new Element("section", ns)
                        .setAttribute("id", "xyzzy")
                        .addContent(new Text("Hello, World!"))));
    }

    @Test public void defaultEmphasisShouldBeStrong() throws Exception {
        assertEquals("<strong>Hello</strong>", generate(new Element("emphasis", ns)
                .setText("Hello")));
        assertEquals("<strong id=\"myid\">Hello</strong>", generate(new Element("emphasis", ns)
                .setAttribute("id", "myid").setText("Hello")));
    }

    @Test public void boldEmphasisShouldBeStrong() throws Exception {
        assertEquals("<strong>Hello</strong>", generate(new Element("emphasis", ns)
                .setAttribute("effect", "bold").setText("Hello")));
        assertEquals("<strong id=\"myid\">Hello</strong>", generate(new Element("emphasis", ns)
                .setAttribute("effect", "bold").setAttribute("id", "myid").setText("Hello")));
    }

    @Test public void italicsEmphasisShouldBeEm() throws Exception {
        assertEquals("<em>Hello</em>", generate(new Element("emphasis", ns)
                .setAttribute("effect", "italics").setText("Hello")));
        assertEquals("<em id=\"myid\">Hello</em>", generate(new Element("emphasis", ns)
                .setAttribute("effect", "italics").setAttribute("id", "myid").setText("Hello")));
    }

    @Test public void underlineEmphasisShouldBeU() throws Exception {
        assertEquals("<u>Hello</u>", generate(new Element("emphasis", ns)
                .setAttribute("effect", "underline").setText("Hello")));
        assertEquals("<u id=\"myid\">Hello</u>", generate(new Element("emphasis", ns)
                .setAttribute("effect", "underline").setAttribute("id", "myid").setText("Hello")));
    }

    @Test public void smallcapsEmphasisShouldBeSpan() throws Exception {
        assertEquals("<span class=\"smallcaps\">Hello</span>", generate(new Element("emphasis", ns)
                .setAttribute("effect", "smallcaps").setText("Hello")));
        assertEquals("<span class=\"smallcaps\" id=\"myid\">Hello</span>",
                generate(new Element("emphasis", ns)
                .setAttribute("effect", "smallcaps").setAttribute("id", "myid").setText("Hello")));
    }

    @Test public void normalEmphasisShouldBeSpan() throws Exception {
        assertEquals("<span class=\"normal\">Hello</span>", generate(new Element("emphasis", ns)
                .setAttribute("effect", "normal").setText("Hello")));
        assertEquals("<span class=\"normal\" id=\"myid\">Hello</span>",
                generate(new Element("emphasis", ns)
                .setAttribute("effect", "normal").setAttribute("id", "myid").setText("Hello")));
    }

    @Test public void linkShouldRenderUrl() throws Exception {
        assertEquals("<a href=\"http://www.example.com/\">Example</a>",
                generate(new Element("link", ns)
                        .setAttribute("url", "http://www.example.com/")
                        .setText("Example")));
    }

    @Test public void linkShouldRenderTargetId() throws Exception {
        assertEquals("<a href=\"#myRefId\">Example</a>", generate(new Element("link", ns)
                .setAttribute("target-id", "myRefId").setText("Example")));
    }

    @Test public void emptyLinkShouldDefaultToLink() throws Exception {
        assertEquals("<a href=\"#myRefId\">link</a>", generate(new Element("link", ns)
                .setAttribute("target-id", "myRefId")));
    }

    @Test public void emptyLinkShouldRecognizeFigure() throws Exception {
        final Element figure = new Element("figure", ns).setAttribute("id", "myRefId");
        final String figureOutput =
                "<figure id=\"myRefId\"><figcaption><span class=\"title\">Figure 1</span>"
                + "</figcaption></figure>";
        assertEquals("<a href=\"#myRefId\">figure</a>" + figureOutput,
                generate(new Document(new Element("document", ns).setAttribute("id", moduleId)
                        .addContent(new Element("content", ns)
                                .addContent(new Element("link", ns)
                                        .setAttribute("target-id", "myRefId"))
                                .addContent(figure)))));
        figure.detach();
        assertEquals("<a href=\"#myRefId\">figure</a>" + figureOutput,
                generate(new Document(new Element("document", ns).setAttribute("id", moduleId)
                        .addContent(new Element("content", ns)
                                .addContent(new Element("link", ns)
                                        .setAttribute("url", "#myRefId"))
                                .addContent(figure)))));
    }

    @Test public void foreignShouldRenderAsSpan() throws Exception {
        assertEquals("<span class=\"foreign\">&iexcl;Hola, mundo!</span>",
                generate(new Element("foreign", ns).setText("\u00a1Hola, mundo!")));
        assertEquals("<span class=\"foreign\" id=\"myid\">&iexcl;Hola, mundo!</span>",
                generate(new Element("foreign", ns)
                        .setAttribute("id", "myid")
                        .setText("\u00a1Hola, mundo!")));
    }

    @Test public void foreignShouldAllowUrlLinks() throws Exception {
        assertEquals(
                "<a class=\"foreign\" href=\"http://www.example.com/\">&iexcl;Hola, mundo!</a>",
                generate(new Element("foreign", ns)
                        .setAttribute("url", "http://www.example.com/")
                        .setText("\u00a1Hola, mundo!")));
    }

    @Test public void foreignShouldAllowAnchorLinks() throws Exception {
        assertEquals("<a class=\"foreign\" href=\"#myRefId\">&iexcl;Hola, mundo!</a>",
                generate(new Element("foreign", ns)
                        .setAttribute("target-id", "myRefId")
                        .setText("\u00a1Hola, mundo!")));
    }

    @Test public void termShouldRenderAsSpan() throws Exception {
        assertEquals("<span class=\"term\">jargon</span>",
                 generate(new Element("term", ns).setText("jargon")));
        assertEquals("<span class=\"term\" id=\"myid\">jargon</span>",
                 generate(new Element("term", ns).setAttribute("id", "myid").setText("jargon")));
    }

    @Test public void termShouldAllowUrlLinks() throws Exception {
        assertEquals("<a class=\"term\" href=\"http://www.example.com/\">jargon</a>",
                generate(new Element("term", ns)
                        .setAttribute("url", "http://www.example.com/")
                        .setText("jargon")));
    }

    @Test public void termShouldAllowAnchorLinks() throws Exception {
        assertEquals("<a class=\"term\" href=\"#myRef\">jargon</a>",
                generate(new Element("term", ns)
                        .setAttribute("target-id", "myRef")
                        .setText("jargon")));
    }

    @Test public void supShouldRenderAsSup() throws Exception {
        assertEquals("<sup>exponent</sup>", generate(new Element("sup", ns).setText("exponent")));
        assertEquals("<sup id=\"myid\">exponent</sup>",
                generate(new Element("sup", ns).setAttribute("id", "myid").setText("exponent")));
    }

    @Test public void subShouldRenderAsSub() throws Exception {
        assertEquals("<sub>index</sub>", generate(new Element("sub", ns).setText("index")));
        assertEquals("<sub id=\"myid\">index</sub>",
                generate(new Element("sub", ns).setAttribute("id", "myid").setText("index")));
    }

    @Test public void preformatShouldRenderAsPre() throws Exception {
        assertEquals("<pre>my\n text</pre>",
                generate(new Element("preformat", ns).setText("my\n text")));
        assertEquals("<pre id=\"myid\">my\n text</pre>",
                generate(new Element("preformat", ns)
                        .setAttribute("id", "myid")
                        .setText("my\n text")));
    }

    @Test public void defaultCodeShouldRenderAsCode() throws Exception {
        assertEquals("<code>print &quot;Hello&quot;</code>",
                generate(new Element("code", ns).setText("print \"Hello\"")));
        assertEquals("<code id=\"py\">print &quot;Hello&quot;</code>",
                generate(new Element("code", ns)
                        .setAttribute("id", "py")
                        .setText("print \"Hello\"")));
    }

    @Test public void inlineCodeShouldRenderAsCode() throws Exception {
        assertEquals("<code>print &quot;Hello&quot;</code>",
                generate(new Element("code", ns).setText("print \"Hello\"")));
        assertEquals("<code id=\"py\">print &quot;Hello&quot;</code>",
                generate(new Element("code", ns)
                        .setAttribute("id", "py")
                        .setText("print \"Hello\"")));
    }

    @Test public void blockCodeShouldRenderAsPre() throws Exception {
        assertEquals("<pre><code>print &quot;Hello&quot;</code></pre>",
                generate(new Element("code", ns)
                        .setAttribute("display", "block")
                        .setText("print \"Hello\"")));

        assertEquals("<pre><code id=\"py\">print &quot;Hello&quot;</code></pre>",
                generate(new Element("code", ns)
                        .setAttribute("display", "block")
                        .setAttribute("id", "py")
                        .setText("print \"Hello\"")));
    }

    @Test public void defaultNoteShouldRenderAsDiv() throws Exception {
        assertEquals("<div class=\"note\" id=\"cake\">"
                + "<div class=\"title\">Note:</div>Huge success</div>",
                generate(new Element("note", ns)
                        .setAttribute("id", "cake")
                        .setText("Huge success")));
    }

    @Test public void blockNoteShouldRenderAsDiv() throws Exception {
        assertEquals("<div class=\"note\" id=\"cake\">"
                + "<div class=\"title\">Note:</div>Huge success</div>",
                generate(new Element("note", ns)
                        .setAttribute("id", "cake")
                        .setAttribute("display", "block")
                        .setText("Huge success")));
    }

    @Test public void inlineNoteShouldRenderAsSpan() throws Exception {
        assertEquals("<span class=\"note\" id=\"cake\">Huge success</span>",
                generate(new Element("note", ns)
                        .setAttribute("id", "cake")
                        .setAttribute("display", "inline")
                        .setText("Huge success")));
    }

    @Test public void noteTypeShouldChangeHeading() throws Exception {
        final Element elem = new Element("note", ns)
                .setAttribute("id", "cake")
                .setText("Neurotoxin");

        elem.setAttribute("type", "note");
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Note:</div>"
                + "Neurotoxin</div>", generate(elem));
        elem.setAttribute("type", "aside");
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Aside:</div>"
                + "Neurotoxin</div>", generate(elem));
        elem.setAttribute("type", "warning");
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Warning:</div>"
                + "Neurotoxin</div>", generate(elem));
        elem.setAttribute("type", "tip");
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Tip:</div>"
                + "Neurotoxin</div>", generate(elem));
        elem.setAttribute("type", "important");
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Important:</div>"
                + "Neurotoxin</div>", generate(elem));
    }

    @Test public void noteLabelShouldChangeHeading() throws Exception {
        assertEquals("<div class=\"note\" id=\"sageadvice\"><div class=\"title\">Pro tip:</div>"
                + "Write tests</div>",
                generate(new Element("note", ns)
                        .setAttribute("id", "sageadvice")
                        .addContent(new Element("label", ns).setText("Pro tip"))
                        .addContent(new Text("Write tests"))));
    }

    @Test public void noteTitleShouldChangeHeading() throws Exception {
        assertEquals("<div class=\"note\" id=\"sageadvice\">"
                + "<div class=\"title\">Note: Beginner Mistake</div>Write tests</div>",
                generate(new Element("note", ns)
                        .setAttribute("id", "sageadvice")
                        .addContent(new Element("title", ns).setText("Beginner Mistake"))
                        .addContent(new Text("Write tests"))));
    }

    @Test public void defaultNewlineShouldRenderBr() throws Exception {
        assertEquals("<br>", generate(new Element("newline", ns)));
    }

    @Test public void defaultNewlineShouldHonorId() throws Exception {
        assertEquals("<a id=\"myId\"><br></a>", generate(
                new Element("newline", ns).setAttribute("id", "myId")));
    }

    @Test public void normalNewlineShouldRenderBr() throws Exception {
        assertEquals("<br>", generate(new Element("newline", ns).setAttribute("effect", "normal")));
    }

    @Test public void underlineNewlineShouldRenderHr() throws Exception {
        assertEquals("<hr>", generate(new Element("newline", ns)
                .setAttribute("effect", "underline")));
    }

    @Test public void newlineShouldHonorCount() throws Exception {
        assertEquals("<br><br><br>", generate(new Element("newline", ns)
                .setAttribute("count", "3")));
    }

    @Test public void underlineNewlineShouldHonorCount() throws Exception {
        assertEquals("<hr><hr><hr>", generate(new Element("newline", ns)
                .setAttribute("effect", "underline")
                .setAttribute("count", "3")));
    }

    @Test public void ruleShouldAllowStatements() throws Exception {
        assertEquals("<div class=\"rule\" id=\"my-rule\"><div class=\"title\">Rule 1</div>"
                + "<div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                generate(new Element("rule", ns)
                        .setAttribute("id", "my-rule")
                        .addContent(new Element("statement", ns)
                                .setAttribute("id", "my-rule-statement")
                                .setText("QED"))));
    }

    @Test public void ruleShouldUseTitles() throws Exception {
        assertEquals("<div class=\"rule\" id=\"my-rule\">"
                + "<div class=\"title\">Rule 1: The Best Rule</div>"
                + "<div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                generate(new Element("rule", ns)
                        .setAttribute("id", "my-rule")
                        .addContent(new Element("title", ns).setText("The Best Rule"))
                        .addContent(new Element("statement", ns)
                                .setAttribute("id", "my-rule-statement")
                                .setText("QED"))));
    }

    @Test public void ruleShouldUseLabels() throws Exception {
        assertEquals("<div class=\"rule\" id=\"my-rule\">"
                + "<div class=\"title\">Ultimate Rule 1</div>"
                + "<div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                generate(new Element("rule", ns)
                        .setAttribute("id", "my-rule")
                        .addContent(new Element("label", ns).setText("Ultimate Rule"))
                        .addContent(new Element("statement", ns)
                                .setAttribute("id", "my-rule-statement")
                                .setText("QED"))));
    }

    @Test public void ruleHeadingShouldChangeForType() throws Exception {
        assertEquals("<div class=\"rule\" id=\"my-rule\"><div class=\"title\">Law 1</div>"
                + "<div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                generate(new Element("rule", ns)
                        .setAttribute("id", "my-rule")
                        .setAttribute("type", "law")
                        .addContent(new Element("statement", ns)
                                .setAttribute("id", "my-rule-statement")
                                .setText("QED"))));
    }

    @Test public void ruleHeadingShouldIncrementCounter() throws Exception {
        assertEquals("<div class=\"rule\" id=\"rule1\"><div class=\"title\">Rule 1</div>"
                + "<div class=\"statement\" id=\"stmt1\">QED</div></div>"
                + "<div class=\"rule\" id=\"rule2\"><div class=\"title\">Rule 2</div>"
                + "<div class=\"statement\" id=\"stmt2\">QED</div></div>",
                generate(new Document(new Element("document", ns).setAttribute("id", moduleId)
                        .addContent(new Element("content", ns)
                                .addContent(new Element("rule", ns)
                                        .setAttribute("id", "rule1")
                                        .addContent(new Element("statement", ns)
                                                .setAttribute("id", "stmt1")
                                                .setText("QED")))
                                .addContent(new Element("rule", ns)
                                        .setAttribute("id", "rule2")
                                        .addContent(new Element("statement", ns)
                                                .setAttribute("id", "stmt2")
                                                .setText("QED")))))));
    }

    @Test public void ruleTypesShouldCountSeparately() throws Exception {
        assertEquals("<div class=\"rule\" id=\"rule1\"><div class=\"title\">Rule 1</div>"
                + "<div class=\"statement\" id=\"stmt1\">QED</div></div>"
                + "<div class=\"rule\" id=\"rule2\"><div class=\"title\">Law 1</div>"
                + "<div class=\"statement\" id=\"stmt2\">QED</div></div>",
                generate(new Document(new Element("document", ns).setAttribute("id", moduleId)
                        .addContent(new Element("content", ns)
                                .addContent(new Element("rule", ns)
                                        .setAttribute("id", "rule1")
                                        .addContent(new Element("statement", ns)
                                                .setAttribute("id", "stmt1")
                                                .setText("QED")))
                                .addContent(new Element("rule", ns)
                                        .setAttribute("id", "rule2")
                                        .setAttribute("type", "law")
                                        .addContent(new Element("statement", ns)
                                                .setAttribute("id", "stmt2")
                                                .setText("QED")))))));
    }

    @Test public void ruleShouldAllowProofs() throws Exception {
        assertEquals("<div class=\"rule\" id=\"my-rule\"><div class=\"title\">Rule 1</div>"
                + "<div class=\"statement\" id=\"my-rule-statement\">I exist.</div>"
                + "<div class=\"proof\" id=\"my-rule-proof\">"
                + "<div class=\"title\">Proof</div>QED</div>"
                + "</div>",
                generate(new Element("rule", ns)
                        .setAttribute("id", "my-rule")
                        .addContent(new Element("statement", ns)
                                .setAttribute("id", "my-rule-statement")
                                .setText("I exist."))
                        .addContent(new Element("proof", ns)
                                .setAttribute("id", "my-rule-proof")
                                .setText("QED"))));
    }

    @Test public void definitionShouldRenderAsDiv() throws Exception {
        assertEquals("<div class=\"definition\" id=\"myDef\">"
                + "<div class=\"title\">Definition 1</div><ol></ol></div>",
                generate(new Element("definition", ns).setAttribute("id", "myDef")));
    }

    @Test public void definitionShouldUseTermAsTitle() throws Exception {
        assertEquals("<div class=\"definition\" id=\"myDef\">"
                + "<div class=\"title\">Definition 1: snarf</div><ol></ol></div>",
                generate(new Element("definition", ns)
                        .setAttribute("id", "myDef")
                        .addContent(new Element("term", ns).setText("snarf"))));
    }

    @Test public void definitionShouldRenderMeaningsInList() throws Exception {
        assertEquals("<div class=\"definition\" id=\"myDef\">"
                + "<div class=\"title\">Definition 1</div><ol>"
                + "<li class=\"meaning\">A noun</li><li class=\"meaning\">A verb</li></ol></div>",
                generate(new Element("definition", ns)
                        .setAttribute("id", "myDef")
                        .addContent(new Element("meaning", ns).setText("A noun"))
                        .addContent(new Element("meaning", ns).setText("A verb"))));
    }

    @Test public void fullDefinitionTest() throws Exception {
        assertEquals("<div class=\"definition\" id=\"myDef\">"
                + "<div class=\"title\">Definition 1: snarf</div><ol>"
                + "<li class=\"meaning\">A noun</li><li class=\"meaning\">A verb</li></ol></div>",
                generate(new Element("definition", ns)
                        .setAttribute("id", "myDef")
                        .addContent(new Element("term", ns).setText("snarf"))
                        .addContent(new Element("meaning", ns).setText("A noun"))
                        .addContent(new Element("meaning", ns).setText("A verb"))));
    }

    @Test public void exampleShouldRenderAsDiv() throws Exception {
        assertEquals("<div class=\"example\" id=\"my-example\"><div class=\"title\">Example 1</div>"
                     + "For example, ...</div>",
                     generate(new Element("example", ns)
                             .setAttribute("id", "my-example")
                             .setText("For example, ...")));
    }

    @Test public void exampleShouldAcceptTitle() throws Exception {
        assertEquals("<div class=\"example\" id=\"my-example\">"
                + "<div class=\"title\">Example 1: The Best One</div>"
                + "For example, ...</div>",
                generate(new Element("example", ns)
                        .setAttribute("id", "my-example")
                        .addContent(new Element("title", ns).setText("The Best One"))
                        .addContent(new Text("For example, ..."))));
    }

    @Test public void exampleShouldAcceptLabel() throws Exception {
        assertEquals("<div class=\"example\" id=\"my-example\">"
                + "<div class=\"title\">Prime Example 1</div>For example, ...</div>",
                generate(new Element("example", ns)
                        .setAttribute("id", "my-example")
                        .addContent(new Element("label", ns).setText("Prime Example"))
                        .addContent(new Text("For example, ..."))));
    }

    @Test public void exerciseShouldUseProblem() throws Exception {
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\">"
                + "<div class=\"title\">Exercise 1</div>"
                + "<div class=\"problem\" id=\"texas-problem\">What is the capital of Texas?"
                + "</div></div>",
                generate(new Element("exercise", ns)
                        .setAttribute("id", "texas-exercise")
                        .addContent(new Element("problem", ns)
                                .setAttribute("id", "texas-problem")
                                .setText("What is the capital of Texas?"))));
    }

    @Test public void exerciseShouldUseSolution() throws Exception {
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\">"
                + "<div class=\"title\">Exercise 1</div>"
                + "<div class=\"problem\" id=\"texas-problem\">What is the capital of Texas?"
                + "</div><div class=\"solution\" id=\"texas-solution\">"
                + "<div class=\"title\">Solution</div>Austin</div></div>",
                generate(new Element("exercise", ns)
                        .setAttribute("id", "texas-exercise")
                        .addContent(new Element("problem", ns)
                                .setAttribute("id", "texas-problem")
                                .setText("What is the capital of Texas?"))
                        .addContent(new Element("solution", ns)
                                .setAttribute("id", "texas-solution")
                                .setText("Austin"))));
    }

    @Test public void exerciseShouldUseCommentary() throws Exception {
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\">"
                + "<div class=\"title\">Exercise 1</div>"
                + "<div class=\"problem\" id=\"texas-problem\">What is the capital of Texas?"
                + "</div><div class=\"commentary\" id=\"texas-commentary\">"
                + "This will be on the final exam.</div></div>",
                generate(new Element("exercise", ns)
                        .setAttribute("id", "texas-exercise")
                        .addContent(new Element("problem", ns)
                                .setAttribute("id", "texas-problem")
                                .setText("What is the capital of Texas?"))
                        .addContent(new Element("commentary", ns)
                                .setAttribute("id", "texas-commentary")
                                .setText("This will be on the final exam."))));
    }

    @Test public void equationShouldWrapContent() throws Exception {
        assertEquals("<div class=\"equation\" id=\"my-equation\">"
                + "<div class=\"title\">Equation 1</div>2 + 2 = 4</div>",
                generate(new Element("equation", ns)
                        .setAttribute("id", "my-equation")
                        .setText("2 + 2 = 4")));
    }

    @Test public void figureShouldWrapContent() throws Exception {
        assertEquals("<figure id=\"go\">Hello"
                + "<figcaption><span class=\"title\">Figure 1</span></figcaption></figure>",
                generate(new Element("figure", ns).setAttribute("id", "go").setText("Hello")));
    }

    @Test public void figureShouldUseLabel() throws Exception {
        assertEquals("<figure id=\"go\">Hello"
                + "<figcaption><span class=\"title\">Fig 1</span></figcaption></figure>",
                generate(new Element("figure", ns).setAttribute("id", "go")
                        .addContent(new Element("label", ns).setText("Fig"))
                        .addContent(new Text("Hello"))));
    }

    @Test public void figureShouldUseTitle() throws Exception {
        assertEquals("<div class=\"title\">The Go Figure</div>"
                + "<figure id=\"go\">Hello"
                + "<figcaption><span class=\"title\">Figure 1</span></figcaption></figure>",
                generate(new Element("figure", ns)
                        .setAttribute("id", "go")
                        .addContent(new Element("title", ns).setText("The Go Figure"))
                        .addContent(new Text("Hello"))));
    }

    @Test public void figureShouldUseCaption() throws Exception {
        assertEquals("<figure id=\"go\">Hello"
                + "<figcaption><span class=\"title\">Figure 1:</span>"
                + " A caption</figcaption></figure>",
                generate(new Element("figure", ns)
                        .setAttribute("id", "go")
                        .addContent(new Element("caption", ns).setText("A caption"))
                        .addContent(new Text("Hello"))));
    }

    @Test public void figureCaptionShouldAcceptLinks() throws Exception {
        assertEquals("<figure id=\"go\">Hello"
                + "<figcaption><span class=\"title\">Figure 1:</span>"
                + " A <a href=\"http://www.example.com/\">caption</a>"
                + "</figcaption></figure>",
                generate(new Element("figure", ns)
                        .setAttribute("id", "go")
                        .addContent(new Element("caption", ns)
                                .addContent(new Text("A "))
                                .addContent(new Element("link", ns)
                                        .setAttribute("url", "http://www.example.com/")
                                        .setText("caption")))
                        .addContent(new Text("Hello"))));
    }

    @Test public void figureShouldUseTitleLabelCaption() throws Exception {
        assertEquals("<div class=\"title\">The Go Figure</div>"
                + "<figure id=\"go\">Hello"
                + "<figcaption><span class=\"title\">Fig 1:</span>"
                + " A greeting</figcaption></figure>",
                generate(new Element("figure", ns)
                        .setAttribute("id", "go")
                        .addContent(new Element("title", ns).setText("The Go Figure"))
                        .addContent(new Element("label", ns).setText("Fig"))
                        .addContent(new Element("caption", ns).setText("A greeting"))
                        .addContent(new Text("Hello"))));
    }

    @Test public void defaultSubfigureTest() throws Exception {
        assertEquals("<figure id=\"go\" class=\"horizontal\">"
                + "<div class=\"subfigure\">Hello</div>"
                + "<div class=\"subfigure\">World</div>"
                + "<figcaption><span class=\"title\">Figure 1</span></figcaption></figure>",
                generate(new Element("figure", ns)
                        .setAttribute("id", "go")
                        .addContent(new Element("subfigure", ns).setText("Hello"))
                        .addContent(new Element("subfigure", ns).setText("World"))));
    }

    @Test public void horizontalSubfigureTest() throws Exception {
        assertEquals("<figure id=\"go\" class=\"horizontal\">"
                + "<div class=\"subfigure\">Hello</div>"
                + "<div class=\"subfigure\">World</div>"
                + "<figcaption><span class=\"title\">Figure 1</span></figcaption></figure>",
                generate(new Element("figure", ns)
                        .setAttribute("id", "go")
                        .setAttribute("orient", "horizontal")
                        .addContent(new Element("subfigure", ns).setText("Hello"))
                        .addContent(new Element("subfigure", ns).setText("World"))));
    }

    @Test public void verticalSubfigureTest() throws Exception {
        assertEquals("<figure id=\"go\" class=\"vertical\">"
                + "<div class=\"subfigure\">Hello</div>"
                + "<div class=\"subfigure\">World</div>"
                + "<figcaption><span class=\"title\">Figure 1</span></figcaption></figure>",
                generate(new Element("figure", ns)
                        .setAttribute("id", "go")
                        .setAttribute("orient", "vertical")
                        .addContent(new Element("subfigure", ns).setText("Hello"))
                        .addContent(new Element("subfigure", ns).setText("World"))));
    }

    @Test public void defaultListShouldRenderAsUl() throws Exception {
        assertEquals("<ul id=\"basicList\"><li>One</li><li>Two</li><li>Three</li></ul>",
                generate(new Element("list", ns)
                        .setAttribute("id", "basicList")
                        .addContent(new Element("item", ns).setText("One"))
                        .addContent(new Element("item", ns).setText("Two"))
                        .addContent(new Element("item", ns).setText("Three"))));
    }

    @Test public void bulletedListShouldRenderAsUl() throws Exception {
        assertEquals("<ul id=\"bulletList\"><li>One</li><li>Two</li><li>Three</li></ul>",
                generate(new Element("list", ns)
                        .setAttribute("id", "bulletList")
                        .setAttribute("list-type", "bulleted")
                        .addContent(new Element("item", ns).setText("One"))
                        .addContent(new Element("item", ns).setText("Two"))
                        .addContent(new Element("item", ns).setText("Three"))));
    }

    @Test public void bulletedListShouldAllowBulletStyleNone() throws Exception {
        assertEquals("<ul id=\"bulletList\" class=\"bulletStyleNone\">"
                + "<li>One</li><li>Two</li><li>Three</li></ul>",
                generate(new Element("list", ns)
                        .setAttribute("id", "bulletList")
                        .setAttribute("list-type", "bulleted")
                        .setAttribute("bullet-style", "none")
                        .addContent(new Element("item", ns).setText("One"))
                        .addContent(new Element("item", ns).setText("Two"))
                        .addContent(new Element("item", ns).setText("Three"))));
    }

    @Test public void enumeratedListShouldRenderAsOl() throws Exception {
        assertEquals("<ol id=\"enumList\"><li>One</li><li>Two</li><li>Three</li></ol>",
                generate(new Element("list", ns)
                        .setAttribute("id", "enumList")
                        .setAttribute("list-type", "enumerated")
                        .addContent(new Element("item", ns).setText("One"))
                        .addContent(new Element("item", ns).setText("Two"))
                        .addContent(new Element("item", ns).setText("Three"))));
    }

    @Test public void enumeratedListShouldAllowStartValue() throws Exception {
        assertEquals("<ol id=\"enumList\" start=\"3\"><li>One</li><li>Two</li><li>Three</li></ol>",
                generate(new Element("list", ns)
                        .setAttribute("id", "enumList")
                        .setAttribute("list-type", "enumerated")
                        .setAttribute("start-value", "3")
                        .addContent(new Element("item", ns).setText("One"))
                        .addContent(new Element("item", ns).setText("Two"))
                        .addContent(new Element("item", ns).setText("Three"))));
    }

    @Test public void bulletedListShouldIgnoreStartValue() throws Exception {
        assertEquals("<ul id=\"bulletList\"><li>One</li><li>Two</li><li>Three</li></ul>",
                generate(new Element("list", ns)
                        .setAttribute("id", "bulletList")
                        .setAttribute("list-type", "bulleted")
                        .setAttribute("start-value", "3")
                        .addContent(new Element("item", ns).setText("One"))
                        .addContent(new Element("item", ns).setText("Two"))
                        .addContent(new Element("item", ns).setText("Three"))));
    }

    @Test public void blockListShouldAllowItemSep() throws Exception {
        assertEquals("<ul id=\"mylist\"><li>One-AND-A</li><li>Two-AND-A</li><li>Three</li></ul>",
                generate(new Element("list", ns)
                        .setAttribute("id", "mylist")
                        .setAttribute("item-sep", "-AND-A")
                        .addContent(new Element("item", ns).setText("One"))
                        .addContent(new Element("item", ns).setText("Two"))
                        .addContent(new Element("item", ns).setText("Three"))));
    }

    @Test public void listItemShouldAllowId() throws Exception {
        assertEquals("<ul id=\"mylist\"><li id=\"it\">One</li><li>Two</li></ul>",
                generate(new Element("list", ns)
                        .setAttribute("id", "mylist")
                        .addContent(new Element("item", ns).setAttribute("id", "it").setText("One"))
                        .addContent(new Element("item", ns).setText("Two"))));
    }

    @Test public void labeledItemListTest() throws Exception {
        assertEquals("<ul class=\"labeled\" id=\"mylist\">"
                + "<li><span class=\"title\">First item</span>One</li>"
                + "<li>Two</li>"
                +"</ul>",
                generate(new Element("list", ns)
                        .setAttribute("id", "mylist")
                        .setAttribute("list-type", "labeled-item")
                        .addContent(new Element("item", ns)
                                .addContent(new Element("label", ns).setText("First item"))
                                .addContent(new Text("One")))
                        .addContent(new Element("item", ns).setText("Two"))));
    }

    @Test public void mediaImageShouldRenderAsImg() throws Exception {
        assertEquals("<img id=\"myImage\" alt=\"A great image\" "
                + "src=\"http://www.example.com/foo.png\" width=\"128\" height=\"42\">",
                generate(new Element("media", ns)
                        .setAttribute("id", "myImage")
                        .setAttribute("alt", "A great image")
                        .addContent(new Element("image", ns)
                                .setAttribute("src", "http://www.example.com/foo.png")
                                .setAttribute("mime-type", "image/png")
                                .setAttribute("height", "42")
                                .setAttribute("width", "128"))));
    }

    @Test public void mediaImageShouldUseThumbnail() throws Exception {
        assertEquals("<a href=\"http://www.example.com/foo.png\">"
                + "<img id=\"myImage\" alt=\"A great image\" "
                + "src=\"thumbnail.png\" width=\"128\" height=\"42\"></a>",
                generate(new Element("media", ns)
                        .setAttribute("id", "myImage")
                        .setAttribute("alt", "A great image")
                        .addContent(new Element("image", ns)
                                .setAttribute("src", "http://www.example.com/foo.png")
                                .setAttribute("thumbnail", "thumbnail.png")
                                .setAttribute("mime-type", "image/png")
                                .setAttribute("height", "42")
                                .setAttribute("width", "128"))));
    }

    @Test public void mediaImageShouldRenderWithoutDimensions() throws Exception {
        assertEquals("<img id=\"myImage\" alt=\"A great image\" "
                + "src=\"http://www.example.com/foo.png\">", 
                generate(new Element("media", ns)
                        .setAttribute("id", "myImage")
                        .setAttribute("alt", "A great image")
                        .addContent(new Element("image", ns)
                                .setAttribute("src", "http://www.example.com/foo.png")
                                .setAttribute("mime-type", "image/png"))));
    }

    @Test public void mediaObjectShouldRenderAsObject() throws Exception {
        assertEquals("<object id=\"thing\" "
                + "data=\"http://www.example.com/my-widget\" "
                + "type=\"application/x-widget\" "
                + "width=\"128\" height=\"42\">"
                + "Epic widget</object>",
                generate(new Element("media", ns)
                        .setAttribute("id", "thing")
                        .setAttribute("alt", "Epic widget")
                        .addContent(new Element("object", ns)
                                .setAttribute("src", "http://www.example.com/my-widget")
                                .setAttribute("mime-type", "application/x-widget")
                                .setAttribute("height", "42")
                                .setAttribute("width", "128"))));
    }

    @Test public void mediaDownloadShouldRenderAsLink() throws Exception {
        assertEquals("<a id=\"thing\" "
                + "href=\"http://www.example.com/my-widget\" "
                + "type=\"application/x-widget\">"
                + "Download Epic widget</a>",
                generate(new Element("media", ns)
                        .setAttribute("id", "thing")
                        .setAttribute("alt", "Epic widget")
                        .addContent(new Element("download", ns)
                                .setAttribute("src", "http://www.example.com/my-widget")
                                .setAttribute("mime-type", "application/x-widget"))));
    }

    @Test public void mediaLabviewShouldRenderAsLink() throws Exception {
        assertEquals("<a id=\"lbv\" "
                + "href=\"http://www.example.com/content.vi\" "
                + "type=\"application/x-labview-vi\">"
                + "Download A LabVIEW Thing</a>",
                generate(new Element("media", ns)
                        .setAttribute("id", "lbv")
                        .setAttribute("alt", "A LabVIEW Thing")
                        .addContent(new Element("labview", ns)
                                .setAttribute("src", "http://www.example.com/content.vi")
                                .setAttribute("mime-type", "application/x-labview-vi"))));
    }

    @Test public void mediaShouldIgnorePdf() throws Exception {
        assertEquals("<object id=\"thing\" data=\"http://www.example.com/my-widget\" "
                + "type=\"application/x-widget\">Epic widget</object>",
                generate(new Element("media", ns)
                        .setAttribute("id", "thing")
                        .setAttribute("alt", "Epic widget")
                        .addContent(new Element("image", ns)
                                .setAttribute("src", "http://www.example.com/foo.png")
                                .setAttribute("mime-type", "image/png")
                                .setAttribute("for", "pdf"))
                        .addContent(new Element("object", ns)
                                .setAttribute("src", "http://www.example.com/my-widget")
                                .setAttribute("mime-type", "application/x-widget"))));
    }

    @Test public void mediaShouldAlwaysUseOverride() throws Exception {
        assertEquals("<object id=\"thing\" data=\"http://www.example.com/my-widget\" "
                + "type=\"application/x-widget\">Epic widget</object>",
                generate(new Element("media", ns)
                        .setAttribute("id", "thing")
                        .setAttribute("alt", "Epic widget")
                        .addContent(new Element("image", ns)
                                .setAttribute("src", "http://www.example.com/foo.png")
                                .setAttribute("mime-type", "image/png"))
                        .addContent(new Element("object", ns)
                                .setAttribute("src", "http://www.example.com/my-widget")
                                .setAttribute("mime-type", "application/x-widget")
                                .setAttribute("for", "webview2.0"))));
    }

    @Test public void mathShouldPassThrough() throws Exception {
        final Namespace mathns = Namespace.getNamespace("http://www.w3.org/1998/Math/MathML");
        assertEquals("<math><mrow><mi>a</mi><mo>+</mo><mi>b</mi></mrow></math>",
                generate(new Element("math", mathns)
                        .addContent(new Element("mrow", mathns)
                                .addContent(new Element("mi", mathns).setText("a"))
                                .addContent(new Element("mo", mathns).setText("+"))
                                .addContent(new Element("mi", mathns).setText("b")))));
    }

    @Test public void mathematicaTest() throws Exception {
        assertEquals("<object id=\"mycdf\" "
                + "data=\"MYFILENAME.cdf\" "
                + "type=\"application/vnd.wolfram.cdf.text\" "
                + "width=\"128\" height=\"42\">"
                + "<param name=\"src\" value=\"MYFILENAME.cdf\">"
                + "<embed src=\"MYFILENAME.cdf\" "
                + "type=\"application/vnd.wolfram.cdf.text\" "
                + "width=\"128\" height=\"42\">"
                + "</object>"
                + "<div class=\"downloadLink\"><a href=\"MYFILENAME.cdf\">Download CDF</a></div>",
                generate(new Element("media", ns)
                        .setAttribute("id", "mycdf")
                        .setAttribute("alt", "Mathematica Test")
                        .addContent(new Element("object", ns)
                                .setAttribute("src", "MYFILENAME.cdf")
                                .setAttribute("mime-type", "application/vnd.wolfram.cdf.text")
                                .setAttribute("height", "42")
                                .setAttribute("width", "128"))));
    }

    @Test public void basicTableTest() throws Exception {
        final Element tgroup = new Element("tgroup", ns)
                .setAttribute("cols", "3")
                .addContent(new Element("thead", ns)
                        .addContent(new Element("row", ns)
                                .addContent(new Element("entry", ns).setText("Name"))
                                .addContent(new Element("entry", ns).setText("Type"))
                                .addContent(new Element("entry", ns).setText("Value"))))
                .addContent(new Element("tbody", ns)
                        .addContent(new Element("row", ns)
                                .addContent(new Element("entry", ns).setText("answer"))
                                .addContent(new Element("entry", ns).setText("int"))
                                .addContent(new Element("entry", ns).setText("42")))
                        .addContent(new Element("row", ns)
                                .addContent(new Element("entry", ns).setText("pi"))
                                .addContent(new Element("entry", ns).setText("float"))
                                .addContent(new Element("entry", ns).setText("3.14"))));
        assertEquals("<table id=\"1000\" class=\"cals calsFrameAll\">"
                + "<thead><tr>"
                + "<th>Name</th><th>Type</th><th>Value</th>"
                + "</tr></thead>"
                + "<tbody><tr>"
                + "<td>answer</td><td>int</td><td>42</td>"
                + "</tr><tr>"
                + "<td>pi</td><td>float</td><td>3.14</td>"
                + "</tr></tbody>"
                + "<caption><span class=\"title\">Table 1:</span> Information</caption>"
                + "</table>",
                generate(new Element("table", ns)
                        .setAttribute("id", "1000")
                        .setAttribute("summary", "A data table")
                        .addContent(new Element("title", ns).setText("Information"))
                        .addContent(tgroup)));
    }

    /**
     *  trickyTableTest is the trial-by-fire test that a full CALS-table compliant generator should
     *  pass.
     *
     *  TODO(light): This does not pass. It is included for completeness.
     */
    @Ignore @Test public void trickyTableTest() throws Exception {
        final Element tgroup = new Element("tgroup", ns)
                .setAttribute("cols", "3")
                .addContent(new Element("colspec", ns)
                        .setAttribute("colnum", "1")
                        .setAttribute("colname", "c1"))
                .addContent(new Element("colspec", ns)
                        .setAttribute("colnum", "2")
                        .setAttribute("colname", "c2"))
                .addContent(new Element("colspec", ns)
                        .setAttribute("colnum", "3")
                        .setAttribute("colname", "c3"))
                .addContent(new Element("thead", ns)
                        .addContent(new Element("row", ns)
                                .addContent(new Element("entry", ns).setText("Course"))
                                .addContent(new Element("entry", ns).setText("Semester"))
                                .addContent(new Element("entry", ns).setText("Grade"))))
                .addContent(new Element("tfoot", ns)
                        .addContent(new Element("row", ns)
                                .addContent(new Element("entry", ns)
                                        .setAttribute("namest", "c1").setAttribute("nameend", "c2")
                                        .setText("Course"))
                                .addContent(new Element("entry", ns).setText("85.5%"))))
                .addContent(new Element("tbody", ns)
                        .addContent(new Element("row", ns)
                                .addContent(new Element("entry", ns)
                                        .setAttribute("morerows", "1")
                                        .setText("Biology"))
                                .addContent(new Element("entry", ns).setText("1"))
                                .addContent(new Element("entry", ns).setText("86%"))))
                        .addContent(new Element("row", ns)
                                .addContent(new Element("entry", ns).setText("2"))
                                .addContent(new Element("entry", ns).setText("91%")))
                        .addContent(new Element("row", ns)
                                .addContent(new Element("entry", ns)
                                        .setAttribute("morerows", "1")
                                        .setText("English"))
                                .addContent(new Element("entry", ns).setText("1"))
                                .addContent(new Element("entry", ns).setText("87%")))
                        .addContent(new Element("row", ns)
                                .addContent(new Element("entry", ns).setText("2"))
                                .addContent(new Element("entry", ns).setText("78%")));
        assertEquals("<table id=\"report_card\">"
                + "<caption><span class=\"title\">Table 1:</span> Report Card</caption>"
                + "<thead><tr><th>Course</th><th>Semester</th><th>Grade</th></tr></thead>"
                + "<tbody>"
                + "<tr><td rowspan=\"2\">Biology</td><td>1</td><td>86%</td></tr>"
                + "<tr><td>2</td><td>91%</td></tr>"
                + "<tr><td rowspan=\"2\">English</td><td>1</td><td>87%</td></tr>"
                + "<tr><td>2</td><td>78%</td></tr>"
                + "</tbody>"
                + "<tfoot><tr><th colspan=\"2\">Average:</th><th>85.5%</th></tr></tfoot>"
                + "</table>",
                generate(new Element("table", ns)
                        .setAttribute("id", "report_card")
                        .setAttribute("summary", "Your grades")
                        .addContent(new Element("title", ns).setText("Report card"))
                        .addContent(new Element("tgroup", ns).setAttribute("cols", "3"))
                        .addContent(tgroup)));
    }
}
