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

package org.cnx.html;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.SoyModule;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.cnx.util.UtilModule;
import org.cnx.util.testing.DOMBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import static org.junit.Assert.*;

public class HTMLGeneratorTests {
    private static Injector injector;
    private Document doc;
    private DOMBuilder builder;

    @BeforeClass public static void createInjector() {
        injector = Guice.createInjector(
                new DefaultModule(),
                new SoyModule(),
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
        final RenderScope scope = injector.getInstance(RenderScope.class);
        scope.enter();
        try {
            return injector.getInstance(HTMLGenerator.class).generate(node);
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
        final String s = "Hello, 世界!";
        assertEquals(s, generate(doc.createTextNode(s)));
    }

    @Test public void textShouldBeEscaped() throws Exception {
        final String s = "I am a \"<b>leet hacker</b>\"";
        assertEquals("I am a &quot;&lt;b&gt;leet hacker&lt;/b&gt;&quot;",
                     generate(doc.createTextNode(s)));
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

    @Test public void foreignShouldRenderAsSpan() throws Exception {
        assertEquals("<span class=\"foreign\">¡Hola, mundo!</span>",
                     generate(builder.element("foreign").text("¡Hola, mundo!")));
        assertEquals("<span class=\"foreign\" id=\"myid\">¡Hola, mundo!</span>",
                     generate(builder.element("foreign").attr("id", "myid").text("¡Hola, mundo!")));
    }

    @Test public void foreignShouldAllowUrlLinks() throws Exception {
        final Node node = builder.element("foreign")
            .attr("url", "http://www.example.com/")
            .text("¡Hola, mundo!")
            .build();
        assertEquals("<span class=\"foreign\"><a href=\"http://www.example.com/\">¡Hola, mundo!</a></span>",
                     generate(node));
    }

    @Test public void foreignShouldAllowAnchorLinks() throws Exception {
        final Node node = builder.element("foreign")
                .attr("target-id", "myRefId")
                .text("¡Hola, mundo!")
                .build();
        assertEquals("<span class=\"foreign\"><a href=\"#myRefId\">¡Hola, mundo!</a></span>",
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
        assertEquals("<span class=\"term\"><a href=\"http://www.example.com/\">jargon</a></span>",
                     generate(node));
    }

    @Test public void termShouldAllowAnchorLinks() throws Exception {
        assertEquals("<span class=\"term\"><a href=\"#myRef\">jargon</a></span>",
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
        assertEquals("<div class=\"note\" id=\"cake\"><h2>Note:</h2>Huge success</div>",
                     generate(node));
    }

    @Test public void blockNoteShouldRenderAsDiv() throws Exception {
        final Node node = builder.element("note")
                .attr("id", "cake")
                .attr("display", "block")
                .text("Huge success")
                .build();
        assertEquals("<div class=\"note\" id=\"cake\"><h2>Note:</h2>Huge success</div>",
                     generate(node));
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
        assertEquals("<div class=\"note\" id=\"cake\"><h2>Note:</h2>Neurotoxin</div>",
                     generate(b));
        b.attr("type", "aside");
        assertEquals("<div class=\"note\" id=\"cake\"><h2>Aside:</h2>Neurotoxin</div>",
                     generate(b));
        b.attr("type", "warning");
        assertEquals("<div class=\"note\" id=\"cake\"><h2>Warning:</h2>Neurotoxin</div>",
                     generate(b));
        b.attr("type", "tip");
        assertEquals("<div class=\"note\" id=\"cake\"><h2>Tip:</h2>Neurotoxin</div>",
                     generate(b));
        b.attr("type", "important");
        assertEquals("<div class=\"note\" id=\"cake\"><h2>Important:</h2>Neurotoxin</div>",
                     generate(b));
    }

    @Test public void noteLabelShouldChangeHeading() throws Exception {
        final Node node = builder.element("note")
                .attr("id", "sageadvice")
                .child(builder.element("label").text("Pro tip"))
                .text("Write tests")
                .build();
        assertEquals("<div class=\"note\" id=\"sageadvice\"><h2>Pro tip:</h2>Write tests</div>",
                     generate(node));
    }

    @Test public void noteTitleShouldChangeHeading() throws Exception {
        final Node node = builder.element("note")
                .attr("id", "sageadvice")
                .child(builder.element("title").text("Beginner Mistake"))
                .text("Write tests")
                .build();
        assertEquals("<div class=\"note\" id=\"sageadvice\">"
                     + "<h2>Note: Beginner Mistake</h2>Write tests</div>",
                     generate(node));
    }

    @Test public void defaultNewlineShouldRenderBr() throws Exception {
        assertEquals("<br>", generate(builder.element("newline")));
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
        assertEquals("<div class=\"rule\" id=\"my-rule\"><h2>Rule 1</h2><div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                     generate(node));
    }

    @Test public void ruleShouldUseTitles() throws Exception {
        final Node node = builder.element("rule").attr("id", "my-rule").child(
                builder.element("title").text("The Best Rule"),
                builder.element("statement")
                        .attr("id", "my-rule-statement")
                        .text("QED")
        ).build();
        assertEquals("<div class=\"rule\" id=\"my-rule\"><h2>Rule 1: The Best Rule</h2><div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                     generate(node));
    }

    @Test public void ruleShouldUseLabels() throws Exception {
        final Node node = builder.element("rule").attr("id", "my-rule").child(
                builder.element("label").text("Ultimate Rule"),
                builder.element("statement")
                        .attr("id", "my-rule-statement")
                        .text("QED")
        ).build();
        assertEquals("<div class=\"rule\" id=\"my-rule\"><h2>Ultimate Rule 1</h2><div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
                     generate(node));
    }

    @Test public void ruleHeadingShouldChangeForType() throws Exception {
        final Node node = builder.element("rule").attr("id", "my-rule").attr("type", "law").child(
                builder.element("statement")
                        .attr("id", "my-rule-statement")
                        .text("QED")
        ).build();
        assertEquals("<div class=\"rule\" id=\"my-rule\"><h2>Law 1</h2><div class=\"statement\" id=\"my-rule-statement\">QED</div></div>",
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

        assertEquals("<div class=\"rule\" id=\"rule1\"><h2>Rule 1</h2>"
                     + "<div class=\"statement\" id=\"stmt1\">QED</div></div>"
                     + "<div class=\"rule\" id=\"rule2\"><h2>Rule 2</h2>"
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

        assertEquals("<div class=\"rule\" id=\"rule1\"><h2>Rule 1</h2>"
                     + "<div class=\"statement\" id=\"stmt1\">QED</div></div>"
                     + "<div class=\"rule\" id=\"rule2\"><h2>Law 1</h2>"
                     + "<div class=\"statement\" id=\"stmt2\">QED</div></div>",
                     generate(doc));
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
        assertEquals("<div class=\"rule\" id=\"my-rule\"><h2>Rule 1</h2>"
                     + "<div class=\"statement\" id=\"my-rule-statement\">I exist.</div>"
                     + "<div class=\"proof\" id=\"my-rule-proof\"><h2>Proof</h2>QED</div>"
                     + "</div>",
                     generate(node));
    }

    @Test public void exampleShouldRenderAsDiv() throws Exception {
        final Node node = builder.element("example")
                .attr("id", "my-example")
                .text("For example, ...")
                .build();
        assertEquals("<div class=\"example\" id=\"my-example\"><h2>Example 1</h2>"
                     + "For example, ...</div>",
                     generate(node));
    }

    @Test public void exampleShouldAcceptTitle() throws Exception {
        final Node node = builder.element("example")
                .attr("id", "my-example")
                .child(builder.element("title").text("The Best One"))
                .text("For example, ...")
                .build();
        assertEquals("<div class=\"example\" id=\"my-example\"><h2>Example 1: The Best One</h2>"
                     + "For example, ...</div>",
                     generate(node));
    }

    @Test public void exampleShouldAcceptLabel() throws Exception {
        final Node node = builder.element("example")
                .attr("id", "my-example")
                .child(builder.element("label").text("Prime Example"))
                .text("For example, ...")
                .build();
        assertEquals("<div class=\"example\" id=\"my-example\"><h2>Prime Example 1</h2>"
                     + "For example, ...</div>",
                     generate(node));
    }

    @Test public void exerciseShouldUseProblem() throws Exception {
        final Node node = builder.element("exercise").attr("id", "texas-exercise").child(
                builder.element("problem")
                        .attr("id", "texas-problem")
                        .text("What is the capital of Texas?")
        ).build();
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\"><h2>Exercise 1</h2>"
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
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\"><h2>Exercise 1</h2>"
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
        assertEquals("<div class=\"exercise\" id=\"texas-exercise\"><h2>Exercise 1</h2>"
                     + "<div class=\"problem\" id=\"texas-problem\">What is the capital of Texas?"
                     + "</div><div class=\"commentary\" id=\"texas-commentary\">"
                     + "This will be on the final exam.</div></div>",
                     generate(node));
    }

    @Test public void equationShouldWrapContent() throws Exception {
        final Node node =
                builder.element("equation").attr("id", "my-equation").text("2 + 2 = 4").build();
        assertEquals("<div class=\"equation\" id=\"my-equation\">"
                     + "<h2>Equation 1</h2>2 + 2 = 4</div>",
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
                     + "data=\"http://www.example.com/my-widget\" width=\"128\" height=\"42\">"
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
        assertEquals("<object id=\"thing\" data=\"http://www.example.com/my-widget\">"
                     + "Epic widget</object>",
                     generate(node));
    }
}
