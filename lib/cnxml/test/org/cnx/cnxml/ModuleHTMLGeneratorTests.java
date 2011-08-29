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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import javax.xml.parsers.DocumentBuilder;
import org.cnx.cnxml.CnxmlModule;
import org.cnx.cnxml.CnxmlNamespace;
import org.cnx.cnxml.ModuleHTMLGenerator;
import org.cnx.mdml.MdmlModule;
import org.cnx.resourcemapping.ObjectFactory;
import org.cnx.util.RenderScope;
import org.cnx.util.UtilModule;
import org.cnx.util.testing.DOMBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import static org.junit.Assert.*;

public class ModuleHTMLGeneratorTests {
    private static final String moduleId = "m123";
    private static Injector injector;
    private Document doc;
    private DOMBuilder builder;

    @BeforeClass public static void createInjector() {
        injector = Guice.createInjector(
                new CnxmlModule(),
                new MdmlModule(),
                new UtilModule()
        );
    }

    @Before public void init() throws Exception {
        doc = injector.getInstance(DocumentBuilder.class).newDocument();
        final String cnxmlNamespace = injector.getInstance(
                Key.get(String.class, CnxmlNamespace.class));
        builder = new DOMBuilder(doc, cnxmlNamespace);
    }

    private String generate(final Node node) throws Exception {
        if (node instanceof Document) {
            return generate((Document)node);
        }
        builder.child(builder.element("document").child(
                    builder.element("content").child(node)
        ));
        final String result = generate(doc);
        doc.removeChild(doc.getDocumentElement());
        return result;
    }

    private String generate(final Document d) throws Exception {
        final RenderScope scope = injector.getInstance(RenderScope.class);
        scope.enter();
        try {
            final String cnxmlNamespace = injector.getInstance(
                    Key.get(String.class, CnxmlNamespace.class));
            ObjectFactory dummyFactory = new ObjectFactory();
            return injector.getInstance(ModuleHTMLGenerator.class).generate(
                    new Module(moduleId, d, dummyFactory.createResources(), null, cnxmlNamespace));
        } finally {
            scope.exit();
        }
    }

    private String generate(final DOMBuilder builder) throws Exception {
        return generate(builder.build());
    }

    @Test public void newDocumentShouldBeEmpty() throws Exception {
        final Node node = builder.child(
                builder.element("document")
                        .attr("id", "hello")
                        .child(
                                builder.element("title").text("Hello, World!"),
                                builder.element("content")
                        )
        ).build();

        assertEquals("", generate(node));
    }

    @Test public void fullDocumentShouldBeShown() throws Exception {
        final Node node = builder.child(
                builder.element("document")
                        .attr("id", "hello")
                        .child(
                                builder.element("title").text("Hello, World!"),
                                builder.element("content").text("My Content")
                        )
        ).build();

        assertEquals("My Content", generate(doc));
    }

    @Test public void textShouldBeCopied() throws Exception {
        final String s = "Hello, \u4e16\u754c!";
        assertEquals(s, generate(doc.createTextNode(s)));
    }

    @Test public void textShouldBeEscaped() throws Exception {
        final String s = "I am a \"<b>leet hacker</b>\"";
        assertEquals("I am a &quot;&lt;b&gt;leet hacker&lt;/b&gt;&quot;",
                     generate(doc.createTextNode(s)));
    }

    @Test public void unhandledContentShouldBeReported() throws Exception {
        final DOMBuilder b = builder.element("greeting").text("Hello, World");
        assertEquals("<!--\n<greeting>...</greeting>\n-->"
                + "<div class=\"unhandled\">Unrecognized Content: greeting</div>",
                generate(b));
    }

    @Test public void emptyParagraphTags() throws Exception {
        final Node node = builder.element("para")
                .attr("id", "mypara")
                .build();
        assertEquals("<p id=\"mypara\"></p>", generate(node));
    }

    @Test public void paragraphShouldWrapChildren() throws Exception {
        final Node node = builder.element("para")
                .attr("id", "mypara")
                .text("Hello, ")
                .text("World!")
                .build();
        assertEquals("<p id=\"mypara\">Hello, World!</p>", generate(node));
    }

    @Test public void paragraphShouldShowTitle() throws Exception {
        final Node node = builder.element("para")
                .attr("id", "mypara")
                .child(builder.element("title").text("My Paragraph"))
                .text("Hello, World!")
                .build();
        assertEquals("<div class=\"title\">My Paragraph</div><p id=\"mypara\">Hello, World!</p>",
                generate(node));
    }

    @Test public void emptySectionTags() throws Exception {
        final Node node = builder.element("section")
                .attr("id", "mysect")
                .child(
                        builder.element("title").text("My Section")
                )
                .build();
        assertEquals("<section id=\"mysect\"><h1>My Section</h1></section>", generate(node));
    }

    @Test public void sectionShouldWrapChildren() throws Exception {
        final Node node = builder.element("section")
                .attr("id", "xyzzy")
                .child(
                        builder.element("title").text("My Magic Section")
                )
                .text("Hello, ")
                .text("World!")
                .build();
        assertEquals("<section id=\"xyzzy\"><h1>My Magic Section</h1>Hello, World!</section>",
                generate(node));
    }

    @Test public void sectionTitleShouldBeOptional() throws Exception {
        final Node node = builder.element("section")
                .attr("id", "xyzzy").text("Hello, World!").build();
        assertEquals("<section id=\"xyzzy\">Hello, World!</section>",
                     generate(node));
    }

    @Test public void defaultEmphasisShouldBeStrong() throws Exception {
        assertEquals("<strong>Hello</strong>",
                     generate(builder.element("emphasis").text("Hello")));
        assertEquals("<strong id=\"myid\">Hello</strong>",
                     generate(builder.element("emphasis").attr("id", "myid").text("Hello")));
    }

    @Test public void boldEmphasisShouldBeStrong() throws Exception {
        assertEquals("<strong>Hello</strong>",
                     generate(builder.element("emphasis").attr("effect", "bold").text("Hello")));
        assertEquals("<strong id=\"myid\">Hello</strong>",
                     generate(builder.element("emphasis")
                            .attr("id", "myid").attr("effect", "bold").text("Hello")));
    }

    @Test public void italicsEmphasisShouldBeEm() throws Exception {
        assertEquals("<em>Hello</em>",
                     generate(builder.element("emphasis").attr("effect", "italics").text("Hello")));
        assertEquals("<em id=\"myid\">Hello</em>",
                     generate(builder.element("emphasis")
                            .attr("id", "myid").attr("effect", "italics").text("Hello")));
    }

    @Test public void underlineEmphasisShouldBeU() throws Exception {
        assertEquals("<u>Hello</u>",
                     generate(builder.element("emphasis").attr("effect", "underline").text("Hello")));
        assertEquals("<u id=\"myid\">Hello</u>",
                     generate(builder.element("emphasis")
                            .attr("id", "myid").attr("effect", "underline").text("Hello")));
    }

    @Test public void smallcapsEmphasisShouldBeSpan() throws Exception {
        assertEquals("<span class=\"smallcaps\">Hello</span>",
                     generate(builder.element("emphasis").attr("effect", "smallcaps").text("Hello")));
        assertEquals("<span class=\"smallcaps\" id=\"myid\">Hello</span>",
                     generate(builder.element("emphasis")
                            .attr("id", "myid").attr("effect", "smallcaps").text("Hello")));
    }

    @Test public void normalEmphasisShouldBeSpan() throws Exception {
        assertEquals("<span class=\"normal\">Hello</span>",
                     generate(builder.element("emphasis").attr("effect", "normal").text("Hello")));
        assertEquals("<span class=\"normal\" id=\"myid\">Hello</span>",
                     generate(builder.element("emphasis")
                            .attr("id", "myid").attr("effect", "normal").text("Hello")));
    }

    @Test public void linkShouldRenderUrl() throws Exception {
        assertEquals("<a href=\"http://www.example.com/\">Example</a>",
                generate(builder.element("link")
                        .attr("url", "http://www.example.com/")
                        .text("Example")));
    }

    @Test public void linkShouldRenderTargetId() throws Exception {
        assertEquals("<a href=\"#myRefId\">Example</a>",
                generate(builder.element("link").attr("target-id", "myRefId").text("Example")));
    }

    @Test public void foreignShouldRenderAsSpan() throws Exception {
        assertEquals("<span class=\"foreign\">&iexcl;Hola, mundo!</span>",
                generate(builder.element("foreign").text("\u00a1Hola, mundo!")));
        assertEquals("<span class=\"foreign\" id=\"myid\">&iexcl;Hola, mundo!</span>",
                generate(builder.element("foreign").attr("id", "myid")
                        .text("\u00a1Hola, mundo!")));
    }

    @Test public void foreignShouldAllowUrlLinks() throws Exception {
        final Node node = builder.element("foreign")
                .attr("url", "http://www.example.com/")
                .text("\u00a1Hola, mundo!")
                .build();
        assertEquals("<a class=\"foreign\" href=\"http://www.example.com/\">"
                + "&iexcl;Hola, mundo!</a>", generate(node));
    }

    @Test public void foreignShouldAllowAnchorLinks() throws Exception {
        final Node node = builder.element("foreign")
                .attr("target-id", "myRefId")
                .text("\u00a1Hola, mundo!")
                .build();
        assertEquals("<a class=\"foreign\" href=\"#myRefId\">&iexcl;Hola, mundo!</a>",
                     generate(node));
    }

    @Test public void termShouldRenderAsSpan() throws Exception {
        assertEquals("<span class=\"term\">jargon</span>",
                     generate(builder.element("term").text("jargon")));
        assertEquals("<span class=\"term\" id=\"myid\">jargon</span>",
                     generate(builder.element("term").attr("id", "myid").text("jargon")));
    }

    @Test public void termShouldAllowUrlLinks() throws Exception {
        final Node node = builder.element("term")
                .attr("url", "http://www.example.com/")
                .text("jargon")
                .build();
        assertEquals("<a class=\"term\" href=\"http://www.example.com/\">jargon</a>",
                     generate(node));
    }

    @Test public void termShouldAllowAnchorLinks() throws Exception {
        assertEquals("<a class=\"term\" href=\"#myRef\">jargon</a>",
                     generate(builder.element("term").attr("target-id", "myRef").text("jargon")));
    }

    @Test public void supShouldRenderAsSup() throws Exception {
        assertEquals("<sup>exponent</sup>", generate(builder.element("sup").text("exponent")));
        assertEquals("<sup id=\"myid\">exponent</sup>",
                     generate(builder.element("sup").attr("id", "myid").text("exponent")));
    }

    @Test public void subShouldRenderAsSub() throws Exception {
        assertEquals("<sub>index</sub>", generate(builder.element("sub").text("index")));
        assertEquals("<sub id=\"myid\">index</sub>",
                     generate(builder.element("sub").attr("id", "myid").text("index")));
    }

    @Test public void preformatShouldRenderAsPre() throws Exception {
        assertEquals("<pre>my\n text</pre>",
                     generate(builder.element("preformat").text("my\n text")));
        assertEquals("<pre id=\"myid\">my\n text</pre>",
                     generate(builder.element("preformat").attr("id", "myid").text("my\n text")));
    }

    @Test public void defaultCodeShouldRenderAsCode() throws Exception {
        assertEquals("<code>print &quot;Hello&quot;</code>",
                     generate(builder.element("code").text("print \"Hello\"")));
        assertEquals("<code id=\"py\">print &quot;Hello&quot;</code>",
                     generate(builder.element("code").attr("id", "py").text("print \"Hello\"")));
    }

    @Test public void inlineCodeShouldRenderAsCode() throws Exception {
        assertEquals("<code>print &quot;Hello&quot;</code>",
                     generate(builder.element("code").text("print \"Hello\"")));
        assertEquals("<code id=\"py\">print &quot;Hello&quot;</code>",
                     generate(builder.element("code").attr("id", "py").text("print \"Hello\"")));
    }

    @Test public void blockCodeShouldRenderAsPre() throws Exception {
        final Node node1 = builder.element("code")
                .attr("display", "block")
                .text("print \"Hello\"")
                .build();
        assertEquals("<pre><code>print &quot;Hello&quot;</code></pre>", generate(node1));

        final Node node2 = builder.element("code")
                .attr("display", "block")
                .attr("id", "py")
                .text("print \"Hello\"")
                .build();
        assertEquals("<pre><code id=\"py\">print &quot;Hello&quot;</code></pre>", generate(node2));
    }

    @Test public void defaultNoteShouldRenderAsDiv() throws Exception {
        final Node node = builder.element("note")
                .attr("id", "cake")
                .text("Huge success")
                .build();
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Note:</div>"
                + "Huge success</div>", generate(node));
    }

    @Test public void blockNoteShouldRenderAsDiv() throws Exception {
        final Node node = builder.element("note")
                .attr("id", "cake")
                .attr("display", "block")
                .text("Huge success")
                .build();
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Note:</div>"
                + "Huge success</div>", generate(node));
    }

    @Test public void inlineNoteShouldRenderAsSpan() throws Exception {
        final Node node = builder.element("note")
                .attr("id", "cake")
                .attr("display", "inline")
                .text("Huge success")
                .build();
        assertEquals("<span class=\"note\" id=\"cake\">Huge success</span>",
                     generate(node));
    }

    @Test public void noteTypeShouldChangeHeading() throws Exception {
        final DOMBuilder b = builder.element("note")
                .attr("id", "cake")
                .text("Neurotoxin");

        b.attr("type", "note");
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Note:</div>"
                + "Neurotoxin</div>", generate(b));
        b.attr("type", "aside");
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Aside:</div>"
                + "Neurotoxin</div>", generate(b));
        b.attr("type", "warning");
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Warning:</div>"
                + "Neurotoxin</div>", generate(b));
        b.attr("type", "tip");
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Tip:</div>"
                + "Neurotoxin</div>", generate(b));
        b.attr("type", "important");
        assertEquals("<div class=\"note\" id=\"cake\"><div class=\"title\">Important:</div>"
                + "Neurotoxin</div>", generate(b));
    }

    @Test public void noteLabelShouldChangeHeading() throws Exception {
        final Node node = builder.element("note")
                .attr("id", "sageadvice")
                .child(builder.element("label").text("Pro tip"))
                .text("Write tests")
                .build();
        assertEquals("<div class=\"note\" id=\"sageadvice\"><div class=\"title\">Pro tip:</div>"
                + "Write tests</div>", generate(node));
    }

    @Test public void noteTitleShouldChangeHeading() throws Exception {
        final Node node = builder.element("note")
                .attr("id", "sageadvice")
                .child(builder.element("title").text("Beginner Mistake"))
                .text("Write tests")
                .build();
        assertEquals("<div class=\"note\" id=\"sageadvice\">"
                + "<div class=\"title\">Note: Beginner Mistake</div>Write tests</div>",
                generate(node));
    }

    @Test public void defaultNewlineShouldRenderBr() throws Exception {
        assertEquals("<br>", generate(builder.element("newline")));
    }

    @Test public void defaultNewlineShouldHonorId() throws Exception {
        assertEquals("<a id=\"myId\"><br></a>", generate(
                builder.element("newline").attr("id", "myId")));
    }

    @Test public void normalNewlineShouldRenderBr() throws Exception {
        assertEquals("<br>", generate(builder.element("newline").attr("effect", "normal")));
    }

    @Test public void underlineNewlineShouldRenderHr() throws Exception {
        assertEquals("<hr>", generate(builder.element("newline").attr("effect", "underline")));
    }

    @Test public void newlineShouldHonorCount() throws Exception {
        assertEquals("<br><br><br>", generate(builder.element("newline").attr("count", "3")));
    }

    @Test public void underlineNewlineShouldHonorCount() throws Exception {
        assertEquals("<hr><hr><hr>", generate(builder.element("newline")
                .attr("effect", "underline")
                .attr("count", "3")
        ));
    }

    @Test public void ruleShouldAllowStatements() throws Exception {
        final Node node = builder.element("rule").attr("id", "my-rule").child(
                builder.element("statement")
                        .attr("id", "my-rule-statement")
                        .text("QED")
        ).build();
        assertEquals("<div class=\"rule\" id=\"my-rule\"><div class=\"title\">Rule 1</div>"
                + "<div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                generate(node));
    }

    @Test public void ruleShouldUseTitles() throws Exception {
        final Node node = builder.element("rule").attr("id", "my-rule").child(
                builder.element("title").text("The Best Rule"),
                builder.element("statement")
                        .attr("id", "my-rule-statement")
                        .text("QED")
        ).build();
        assertEquals("<div class=\"rule\" id=\"my-rule\">"
                + "<div class=\"title\">Rule 1: The Best Rule</div>"
                + "<div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                generate(node));
    }

    @Test public void ruleShouldUseLabels() throws Exception {
        final Node node = builder.element("rule").attr("id", "my-rule").child(
                builder.element("label").text("Ultimate Rule"),
                builder.element("statement")
                        .attr("id", "my-rule-statement")
                        .text("QED")
        ).build();
        assertEquals("<div class=\"rule\" id=\"my-rule\">"
                + "<div class=\"title\">Ultimate Rule 1</div>"
                + "<div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                generate(node));
    }

    @Test public void ruleHeadingShouldChangeForType() throws Exception {
        final Node node = builder.element("rule").attr("id", "my-rule").attr("type", "law").child(
                builder.element("statement")
                        .attr("id", "my-rule-statement")
                        .text("QED")
        ).build();
        assertEquals("<div class=\"rule\" id=\"my-rule\"><div class=\"title\">Law 1</div>"
                + "<div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                generate(node));
    }

    @Test public void ruleHeadingShouldIncrementCounter() throws Exception {
        final Node node = builder.child(
                builder.element("document").attr("id", "doc").wrapContent(
                        builder.element("rule")
                                .attr("id", "rule1")
                                .child(
                                        builder.element("statement")
                                                .attr("id", "stmt1")
                                                .text("QED")
                        ),
                        builder.element("rule")
                                .attr("id", "rule2")
                                .child(
                                        builder.element("statement")
                                                .attr("id", "stmt2")
                                                .text("QED")
                        )
                )
        ).build();

        assertEquals("<div class=\"rule\" id=\"rule1\"><div class=\"title\">Rule 1</div>"
                + "<div class=\"statement\" id=\"stmt1\">QED</div></div>"
                + "<div class=\"rule\" id=\"rule2\"><div class=\"title\">Rule 2</div>"
                + "<div class=\"statement\" id=\"stmt2\">QED</div></div>",
                generate(node));
    }

    @Test public void ruleTypesShouldCountSeparately() throws Exception {
        final Node node = builder.child(
                builder.element("document").attr("id", "doc").wrapContent(
                        builder.element("rule")
                                .attr("id", "rule1")
                                .attr("type", "rule")
                                .child(
                                        builder.element("statement")
                                                .attr("id", "stmt1")
                                                .text("QED")
                        ),
                        builder.element("rule")
                                .attr("id", "rule2")
                                .attr("type", "law")
                                .child(
                                        builder.element("statement")
                                                .attr("id", "stmt2")
                                                .text("QED")
                        )
                )
        ).build();

        assertEquals("<div class=\"rule\" id=\"rule1\"><div class=\"title\">Rule 1</div>"
                + "<div class=\"statement\" id=\"stmt1\">QED</div></div>"
                + "<div class=\"rule\" id=\"rule2\"><div class=\"title\">Law 1</div>"
                + "<div class=\"statement\" id=\"stmt2\">QED</div></div>", generate(doc));
    }

    @Test public void ruleShouldAllowProofs() throws Exception {
        final Node node = builder.element("rule").attr("id", "my-rule").child(
                builder.element("statement")
                        .attr("id", "my-rule-statement")
                        .text("I exist."),
                builder.element("proof")
                        .attr("id", "my-rule-proof")
                        .text("QED")
        ).build();
        assertEquals("<div class=\"rule\" id=\"my-rule\"><div class=\"title\">Rule 1</div>"
                + "<div class=\"statement\" id=\"my-rule-statement\">I exist.</div>"
                + "<div class=\"proof\" id=\"my-rule-proof\">"
                + "<div class=\"title\">Proof</div>QED</div>"
                + "</div>",
                generate(node));
    }

    @Test public void definitionShouldRenderAsDiv() throws Exception {
        final Node node = builder.element("definition").attr("id", "myDef").build();
        assertEquals("<div class=\"definition\" id=\"myDef\">"
                + "<div class=\"title\">Definition 1</div><ol></ol></div>", generate(node));
    }

    @Test public void definitionShouldUseTermAsTitle() throws Exception {
        final Node node = builder.element("definition")
                .attr("id", "myDef")
                .child(builder.element("term").text("snarf"))
                .build();
        assertEquals("<div class=\"definition\" id=\"myDef\">"
                + "<div class=\"title\">Definition 1: snarf</div><ol></ol></div>", generate(node));
    }

    @Test public void definitionShouldRenderMeaningsInList() throws Exception {
        final Node node = builder.element("definition")
                .attr("id", "myDef")
                .child(builder.element("meaning").text("A noun"))
                .child(builder.element("meaning").text("A verb"))
                .build();
        assertEquals("<div class=\"definition\" id=\"myDef\">"
                + "<div class=\"title\">Definition 1</div><ol>"
                + "<li class=\"meaning\">A noun</li><li class=\"meaning\">A verb</li></ol></div>",
                generate(node));
    }

    @Test public void fullDefinitionTest() throws Exception {
        final Node node = builder.element("definition")
                .attr("id", "myDef")
                .child(builder.element("term").text("snarf"))
                .child(builder.element("meaning").text("A noun"))
                .child(builder.element("meaning").text("A verb"))
                .build();
        assertEquals("<div class=\"definition\" id=\"myDef\">"
                + "<div class=\"title\">Definition 1: snarf</div><ol>"
                + "<li class=\"meaning\">A noun</li><li class=\"meaning\">A verb</li></ol></div>",
                generate(node));
    }

    @Test public void exampleShouldRenderAsDiv() throws Exception {
        final Node node = builder.element("example")
                .attr("id", "my-example")
                .text("For example, ...")
                .build();
        assertEquals("<div class=\"example\" id=\"my-example\"><div class=\"title\">Example 1</div>"
                     + "For example, ...</div>",
                     generate(node));
    }

    @Test public void exampleShouldAcceptTitle() throws Exception {
        final Node node = builder.element("example")
                .attr("id", "my-example")
                .child(builder.element("title").text("The Best One"))
                .text("For example, ...")
                .build();
        assertEquals("<div class=\"example\" id=\"my-example\">"
                + "<div class=\"title\">Example 1: The Best One</div>"
                + "For example, ...</div>", generate(node));
    }

    @Test public void exampleShouldAcceptLabel() throws Exception {
        final Node node = builder.element("example")
                .attr("id", "my-example")
                .child(builder.element("label").text("Prime Example"))
                .text("For example, ...")
                .build();
        assertEquals("<div class=\"example\" id=\"my-example\">"
                + "<div class=\"title\">Prime Example 1</div>For example, ...</div>",
                generate(node));
    }

    @Test public void exerciseShouldUseProblem() throws Exception {
        final Node node = builder.element("exercise").attr("id", "texas-exercise").child(
                builder.element("problem")
                        .attr("id", "texas-problem")
                        .text("What is the capital of Texas?")
        ).build();
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\">"
                + "<div class=\"title\">Exercise 1</div>"
                + "<div class=\"problem\" id=\"texas-problem\">What is the capital of Texas?"
                + "</div></div>",
                generate(node));
    }

    @Test public void exerciseShouldUseSolution() throws Exception {
        final Node node = builder.element("exercise").attr("id", "texas-exercise").child(
                builder.element("problem")
                        .attr("id", "texas-problem")
                        .text("What is the capital of Texas?"),
                builder.element("solution")
                        .attr("id", "texas-solution")
                        .text("Austin")
        ).build();
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\">"
                + "<div class=\"title\">Exercise 1</div>"
                + "<div class=\"problem\" id=\"texas-problem\">What is the capital of Texas?"
                + "</div><div class=\"solution\" id=\"texas-solution\">Austin</div></div>",
                generate(node));
    }

    @Test public void exerciseShouldUseCommentary() throws Exception {
        final Node node = builder.element("exercise").attr("id", "texas-exercise").child(
                builder.element("problem")
                        .attr("id", "texas-problem")
                        .text("What is the capital of Texas?"),
                builder.element("commentary")
                        .attr("id", "texas-commentary")
                        .text("This will be on the final exam.")
        ).build();
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\">"
                + "<div class=\"title\">Exercise 1</div>"
                + "<div class=\"problem\" id=\"texas-problem\">What is the capital of Texas?"
                + "</div><div class=\"commentary\" id=\"texas-commentary\">"
                + "This will be on the final exam.</div></div>",
                generate(node));
    }

    @Test public void equationShouldWrapContent() throws Exception {
        final Node node =
                builder.element("equation").attr("id", "my-equation").text("2 + 2 = 4").build();
        assertEquals("<div class=\"equation\" id=\"my-equation\">"
                + "<div class=\"title\">Equation 1</div>2 + 2 = 4</div>", generate(node));
    }

    @Test public void figureShouldWrapContent() throws Exception {
        final Node node = builder.element("figure").attr("id", "go").text("Hello").build();
        assertEquals("<figure id=\"go\">Hello<figcaption>Figure 1</figcaption></figure>",
                generate(node));
    }

    @Test public void figureShouldUseLabel() throws Exception {
        final Node node = builder.element("figure").attr("id", "go")
                .child(builder.element("label").text("Fig"))
                .text("Hello").build();
        assertEquals("<figure id=\"go\">Hello<figcaption>Fig 1</figcaption></figure>",
                generate(node));
    }

    @Test public void figureShouldUseTitle() throws Exception {
        final Node node = builder.element("figure").attr("id", "go")
                .child(builder.element("title").text("The Go Figure"))
                .text("Hello").build();
        assertEquals("<div class=\"title\">The Go Figure</div>"
                + "<figure id=\"go\">Hello<figcaption>Figure 1</figcaption></figure>",
                generate(node));
    }

    @Test public void figureShouldUseCaption() throws Exception {
        final Node node = builder.element("figure").attr("id", "go")
                .child(builder.element("caption").text("A caption"))
                .text("Hello").build();
        assertEquals("<figure id=\"go\">Hello<figcaption>Figure 1: A caption</figcaption></figure>",
                generate(node));
    }

    @Test public void figureShouldUseTitleLabelCaption() throws Exception {
        final Node node = builder.element("figure").attr("id", "go")
                .child(builder.element("title").text("The Go Figure"))
                .child(builder.element("label").text("Fig"))
                .child(builder.element("caption").text("A greeting"))
                .text("Hello").build();
        assertEquals("<div class=\"title\">The Go Figure</div>"
                + "<figure id=\"go\">Hello<figcaption>Fig 1: A greeting</figcaption></figure>",
                generate(node));
    }

    @Test public void defaultSubfigureTest() throws Exception {
        final Node node = builder.element("figure").attr("id", "go")
                .child(builder.element("subfigure").text("Hello"))
                .child(builder.element("subfigure").text("World"))
                .build();
        assertEquals("<figure id=\"go\" class=\"horizontal\">"
                + "<div class=\"subfigure\">Hello</div>"
                + "<div class=\"subfigure\">World</div>"
                + "<figcaption>Figure 1</figcaption></figure>",
                generate(node));
    }

    @Test public void horizontalSubfigureTest() throws Exception {
        final Node node = builder.element("figure").attr("id", "go").attr("orient", "horizontal")
                .child(builder.element("subfigure").text("Hello"))
                .child(builder.element("subfigure").text("World"))
                .build();
        assertEquals("<figure id=\"go\" class=\"horizontal\">"
                + "<div class=\"subfigure\">Hello</div>"
                + "<div class=\"subfigure\">World</div>"
                + "<figcaption>Figure 1</figcaption></figure>",
                generate(node));
    }

    @Test public void verticalSubfigureTest() throws Exception {
        final Node node = builder.element("figure").attr("id", "go").attr("orient", "vertical")
                .child(builder.element("subfigure").text("Hello"))
                .child(builder.element("subfigure").text("World"))
                .build();
        assertEquals("<figure id=\"go\" class=\"vertical\">"
                + "<div class=\"subfigure\">Hello</div>"
                + "<div class=\"subfigure\">World</div>"
                + "<figcaption>Figure 1</figcaption></figure>",
                generate(node));
    }

    @Test public void defaultListShouldRenderAsUl() throws Exception {
        final Node node = builder.element("list").attr("id", "basicList").child(
                builder.element("item").text("One"),
                builder.element("item").text("Two"),
                builder.element("item").text("Three")
        ).build();
        assertEquals("<ul id=\"basicList\"><li>One</li><li>Two</li><li>Three</li></ul>",
                     generate(node));
    }

    @Test public void bulletedListShouldRenderAsUl() throws Exception {
        final Node node = builder.element("list")
                .attr("id", "bulletList")
                .attr("list-type", "bulleted")
                .child(
                        builder.element("item").text("One"),
                        builder.element("item").text("Two"),
                        builder.element("item").text("Three")
                )
        .build();
        assertEquals("<ul id=\"bulletList\"><li>One</li><li>Two</li><li>Three</li></ul>",
                     generate(node));
    }

    @Test public void enumeratedListShouldRenderAsOl() throws Exception {
        final Node node = builder.element("list")
                .attr("id", "enumList")
                .attr("list-type", "enumerated")
                .child(
                        builder.element("item").text("One"),
                        builder.element("item").text("Two"),
                        builder.element("item").text("Three")
                )
        .build();
        assertEquals("<ol id=\"enumList\"><li>One</li><li>Two</li><li>Three</li></ol>",
                     generate(node));
    }

    @Test public void enumeratedListShouldAllowStartValue() throws Exception {
        final Node node = builder.element("list")
                .attr("id", "enumList")
                .attr("list-type", "enumerated")
                .attr("start-value", "3")
                .child(
                        builder.element("item").text("One"),
                        builder.element("item").text("Two"),
                        builder.element("item").text("Three")
                )
        .build();
        assertEquals("<ol id=\"enumList\" start=\"3\"><li>One</li><li>Two</li><li>Three</li></ol>",
                     generate(node));
    }

    @Test public void bulletedListShouldIgnoreStartValue() throws Exception {
        final Node node = builder.element("list")
                .attr("id", "bulletList")
                .attr("list-type", "bulleted")
                .attr("start-value", "3")
                .child(
                        builder.element("item").text("One"),
                        builder.element("item").text("Two"),
                        builder.element("item").text("Three")
                )
        .build();
        assertEquals("<ul id=\"bulletList\"><li>One</li><li>Two</li><li>Three</li></ul>",
                     generate(node));
    }

    @Test public void blockListShouldAllowItemSep() throws Exception {
        final Node node = builder.element("list")
                .attr("id", "mylist")
                .attr("item-sep", "-AND-A")
                .child(
                        builder.element("item").text("One"),
                        builder.element("item").text("Two"),
                        builder.element("item").text("Three")
                )
        .build();
        assertEquals("<ul id=\"mylist\"><li>One-AND-A</li><li>Two-AND-A</li><li>Three</li></ul>",
                     generate(node));
    }

    @Test public void listItemShouldAllowId() throws Exception {
        final Node node = builder.element("list")
                .attr("id", "mylist")
                .child(
                        builder.element("item").attr("id", "anItem").text("One"),
                        builder.element("item").text("Two")
                )
        .build();
        assertEquals("<ul id=\"mylist\"><li id=\"anItem\">One</li><li>Two</li></ul>",
                     generate(node));
    }

    @Test public void mediaImageShouldRenderAsImg() throws Exception {
        final Node node = builder.element("media")
                .attr("id", "myImage")
                .attr("alt", "A great image")
                .child(builder.element("image")
                        .attr("src", "http://www.example.com/foo.png")
                        .attr("mime-type", "image/png")
                        .attr("height", "42")
                        .attr("width", "128")
                )
                .build();
        assertEquals("<img id=\"myImage\" alt=\"A great image\" "
                     + "src=\"http://www.example.com/foo.png\" width=\"128\" height=\"42\">",
                     generate(node));
    }

    @Test public void mediaImageShouldRenderWithoutDimensions() throws Exception {
        final Node node = builder.element("media")
                .attr("id", "myImage")
                .attr("alt", "A great image")
                .child(builder.element("image")
                        .attr("src", "http://www.example.com/foo.png")
                        .attr("mime-type", "image/png")
                )
                .build();
        assertEquals("<img id=\"myImage\" alt=\"A great image\" "
                     + "src=\"http://www.example.com/foo.png\">", generate(node));
    }

    @Test public void mediaObjectShouldRenderAsObject() throws Exception {
        final Node node = builder.element("media")
                .attr("id", "thing")
                .attr("alt", "Epic widget")
                .child(builder.element("object")
                        .attr("src", "http://www.example.com/my-widget")
                        .attr("mime-type", "application/x-widget")
                        .attr("height", "42")
                        .attr("width", "128")
                )
                .build();
        assertEquals("<object id=\"thing\" "
                     + "data=\"http://www.example.com/my-widget\" "
                     + "type=\"application/x-widget\" "
                     + "width=\"128\" height=\"42\">"
                     + "Epic widget</object>",
                     generate(node));
    }

    @Test public void mediaShouldIgnorePdf() throws Exception {
        final Node node = builder.element("media")
                .attr("id", "thing")
                .attr("alt", "Epic widget")
                .child(
                        builder.element("image")
                                .attr("src", "http://www.example.com/foo.png")
                                .attr("mime-type", "image/png")
                                .attr("for", "pdf"),
                        builder.element("object")
                                .attr("src", "http://www.example.com/my-widget")
                                .attr("mime-type", "application/x-widget")
                )
                .build();
        assertEquals("<object id=\"thing\" data=\"http://www.example.com/my-widget\" "
                + "type=\"application/x-widget\">Epic widget</object>",
                generate(node));
    }

    @Test public void mediaShouldAlwaysUseOverride() throws Exception {
        final Node node = builder.element("media")
                .attr("id", "thing")
                .attr("alt", "Epic widget")
                .child(
                        builder.element("image")
                                .attr("src", "http://www.example.com/foo.png")
                                .attr("mime-type", "image/png"),
                        builder.element("object")
                                .attr("src", "http://www.example.com/my-widget")
                                .attr("mime-type", "application/x-widget")
                                .attr("for", "webview2.0")
                )
                .build();
        assertEquals("<object id=\"thing\" data=\"http://www.example.com/my-widget\" "
                + "type=\"application/x-widget\">Epic widget</object>",
                generate(node));
    }

    @Test public void mathShouldPassThrough() throws Exception {
        final String mathmlNamespace = "http://www.w3.org/1998/Math/MathML";
        final Node node = builder.element(mathmlNamespace, "math").child(
                builder.element(mathmlNamespace, "mrow").child(
                        builder.element(mathmlNamespace, "mi").text("a"),
                        builder.element(mathmlNamespace, "mo").text("+"),
                        builder.element(mathmlNamespace, "mi").text("b")
                )).build();
        assertEquals("<math><mrow><mi>a</mi><mo>+</mo><mi>b</mi></mrow></math>", generate(node));
    }

    @Test public void mathematicaTest() throws Exception {
        final Node node = builder.element("media")
                .attr("id", "mycdf")
                .attr("alt", "Mathematica Test")
                .child(builder.element("object")
                        .attr("src", "MYFILENAME.cdf")
                        .attr("mime-type", "application/vnd.wolfram.cdf.text")
                        .attr("height", "42")
                        .attr("width", "128")
                )
                .build();
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
                generate(node));
    }

    @Test public void basicTableTest() throws Exception {
        final Node node = builder.element("table")
                .attr("id", "1000")
                .attr("summary", "A data table")
                .child(
                        builder.element("title").text("Information"),
                        builder.element("tgroup").attr("cols", "3").child(
                                builder.element("thead").child(
                                        builder.element("row").child(
                                                builder.element("entry").text("Name"),
                                                builder.element("entry").text("Type"),
                                                builder.element("entry").text("Value")
                                        )
                                ),
                                builder.element("tbody").child(
                                        builder.element("row").child(
                                                builder.element("entry").text("answer"),
                                                builder.element("entry").text("int"),
                                                builder.element("entry").text("42")
                                        ),
                                        builder.element("row").child(
                                                builder.element("entry").text("pi"),
                                                builder.element("entry").text("float"),
                                                builder.element("entry").text("3.14")
                                        )
                                )
                        )
                )
        .build();
        assertEquals("<table id=\"1000\" class=\"cals calsFrameAll\">"
                + "<thead><tr>"
                + "<th>Name</th><th>Type</th><th>Value</th>"
                + "</tr></thead>"
                + "<tbody><tr>"
                + "<td>answer</td><td>int</td><td>42</td>"
                + "</tr><tr>"
                + "<td>pi</td><td>float</td><td>3.14</td>"
                + "</tr></tbody>"
                + "<caption><div class=\"title\">Table 1: Information</div>A data table</caption>"
                + "</table>",
                generate(node));
    }

    /**
     *  trickyTableTest is the trial-by-fire test that a full CALS-table compliant generator should
     *  pass.
     *
     *  TODO(light): This does not pass. It is included for completeness.
     */
    @Ignore @Test public void trickyTableTest() throws Exception {
        final Node node = builder.element("table")
                .attr("id", "report_card")
                .attr("summary", "Your grades")
                .child(builder.element("title").text("Report card"))
                .child(builder.element("tgroup").attr("cols", "3").child(
                        builder.element("colspec")
                                .attr("colnum", "1").attr("colname", "c1"),
                        builder.element("colspec")
                                .attr("colnum", "2").attr("colname", "c2"),
                        builder.element("colspec")
                                .attr("colnum", "3").attr("colname", "c3"),
                        builder.element("thead").child(builder.element("row").child(
                                builder.element("entry").text("Course"),
                                builder.element("entry").text("Semester"),
                                builder.element("entry").text("Grade")
                        )),
                        builder.element("tfoot").child(builder.element("row").child(
                                builder.element("entry")
                                        .attr("namest", "c1").attr("nameend", "c2")
                                        .text("Course"),
                                builder.element("entry").text("85.5%")
                        )),
                        builder.element("tbody").child(
                                builder.element("row").child(
                                        builder.element("entry")
                                                .attr("morerows", "1").text("Biology"),
                                        builder.element("entry").text("1"),
                                        builder.element("entry").text("86%")
                                ),
                                builder.element("row").child(
                                        builder.element("entry").text("2"),
                                        builder.element("entry").text("91%")
                                ),
                                builder.element("row").child(
                                        builder.element("entry")
                                                .attr("morerows", "1").text("English"),
                                        builder.element("entry").text("1"),
                                        builder.element("entry").text("87%")
                                ),
                                builder.element("row").child(
                                        builder.element("entry").text("2"),
                                        builder.element("entry").text("78%")
                                )
                        )
                ))
                .build();
        assertEquals("<table id=\"report_card\">"
                     + "<caption>Table 1: Report Card<details>Your grades</details></caption>"
                     + "<thead><tr><th>Course</th><th>Semester</th><th>Grade</th></tr></thead>"
                     + "<tbody>"
                     + "<tr><td rowspan=\"2\">Biology</td><td>1</td><td>86%</td></tr>"
                     + "<tr><td>2</td><td>91%</td></tr>"
                     + "<tr><td rowspan=\"2\">English</td><td>1</td><td>87%</td></tr>"
                     + "<tr><td>2</td><td>78%</td></tr>"
                     + "</tbody>"
                     + "<tfoot><tr><th colspan=\"2\">Average:</th><th>85.5%</th></tr></tfoot>"
                     + "</table>",
                     generate(node));
    }
}
