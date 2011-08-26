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

import static com.google.common.base.Preconditions.*;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.cnx.util.RenderTime;
import org.cnx.util.JdomHtmlSerializer;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.filter.Filter;
import org.jdom.input.DOMBuilder;

/**
 *  JdomHtmlGenerator is an implementation of ModuleHTMLGenerator that generates HTML from a DOM
 *  structure.
 *
 *  It is iterative, but it is not thread-safe.
 */
@RenderTime public class JdomHtmlGenerator implements ModuleHTMLGenerator {
    private final static String MATHML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";

    private final static String TITLE_TAG = "title";
    private final static String LABEL_TAG = "label";
    private final static String ID_ATTR = "id";
    private final static String TYPE_ATTR = "type";

    private final static String PARAGRAPH_TAG = "para";
    private final static String SECTION_TAG = "section";
    private final static String EMPHASIS_TAG = "emphasis";
    private final static String LINK_TAG = "link";
    private final static String FOREIGN_TAG = "foreign";
    private final static String TERM_TAG = "term";
    private final static String SUP_TAG = "sup";
    private final static String SUB_TAG = "sub";
    private final static String PREFORMAT_TAG = "preformat";
    private final static String CODE_TAG = "code";
    private final static String NOTE_TAG = "note";
    private final static String DEFINITION_TAG = "definition";
    private final static String MEANING_TAG = "meaning";
    private final static String EXERCISE_TAG = "exercise";
    private final static String COMMENTARY_TAG = "commentary";
    private final static String PROBLEM_TAG = "problem";
    private final static String SOLUTION_TAG = "solution";
    private final static String EQUATION_TAG = "equation";
    private final static String RULE_TAG = "rule";
    private final static String STATEMENT_TAG = "statement";
    private final static String PROOF_TAG = "proof";
    private final static String EXAMPLE_TAG = "example";
    
    private final static String LINK_URL_ATTR = "url";
    private final static String LINK_TARGET_ID_ATTR = "target-id";
    private final static String LINK_DOCUMENT_ATTR = "document";

    private final static String EFFECT_ATTR = "effect";
    private final static String EFFECT_ITALICS = "italics";
    private final static String EFFECT_UNDERLINE = "underline";
    private final static String EFFECT_SMALLCAPS = "smallcaps";
    private final static String EFFECT_NORMAL = "normal";

    private final static String DISPLAY_ATTR = "display";
    private final static String DISPLAY_NONE = "none";
    private final static String DISPLAY_BLOCK = "block";
    private final static String DISPLAY_INLINE = "inline";

    private final static String NOTE_TYPE_ASIDE = "aside";
    private final static String NOTE_TYPE_WARNING = "warning";
    private final static String NOTE_TYPE_TIP = "tip";
    private final static String NOTE_TYPE_IMPORTANT = "important";
    private final static String NOTE_TYPE_NOTE = "note";

    private final static String NOTE_LABEL_ASIDE = "Aside";
    private final static String NOTE_LABEL_WARNING = "Warning";
    private final static String NOTE_LABEL_TIP = "Tip";
    private final static String NOTE_LABEL_IMPORTANT = "Important";
    private final static String NOTE_LABEL_NOTE = "Note";

    private final static String EXERCISE_LABEL = "Exercise";
    private final static String EQUATION_LABEL = "Equation";
    private final static String PROOF_LABEL = "Proof";
    private final static String DEFINITION_LABEL = "Definition";
    private final static String EXAMPLE_LABEL = "Example";
    private final static String FIGURE_LABEL = "Figure";
    private final static String CALS_TABLE_LABEL = "Table";

    private final static String RULE_TYPE_THEOREM = "theorem";
    private final static String RULE_TYPE_LEMMA = "lemma";
    private final static String RULE_TYPE_COROLLARY = "corollary";
    private final static String RULE_TYPE_LAW = "law";
    private final static String RULE_TYPE_PROPOSITION = "proposition";
    private final static String RULE_TYPE_RULE = "rule";

    private final static String RULE_LABEL_THEOREM = "Theorem";
    private final static String RULE_LABEL_LEMMA = "Lemma";
    private final static String RULE_LABEL_COROLLARY = "Corollary";
    private final static String RULE_LABEL_LAW = "Law";
    private final static String RULE_LABEL_PROPOSITION = "Proposition";
    private final static String RULE_LABEL_RULE = "Rule";

    private final static String LIST_TAG = "list";
    private final static String LIST_ITEM_TAG = "item";
    private final static String LIST_ITEM_SEP_ATTR = "item-sep";
    private final static String LIST_TYPE_ATTR = "list-type";
    private final static String LIST_NUMBER_STYLE_ATTR = "number-style";
    private final static String LIST_START_VALUE_ATTR = "start-value";
    private final static String LIST_TYPE_BULLETED = "bulleted";
    private final static String LIST_TYPE_ENUMERATED = "enumerated";
    private final static String LIST_TYPE_LABELED_ITEM = "labeled-item";

    private final static String NEWLINE_TAG = "newline";
    private final static String NEWLINE_COUNT_ATTR = "count";
    private final static String NEWLINE_EFFECT_ATTR = "effect";
    private final static String NEWLINE_EFFECT_NORMAL = "normal";
    private final static String NEWLINE_EFFECT_UNDERLINE = "underline";

    private final static String MEDIA_TAG = "media";
    private final static String MEDIA_ALT_ATTR = "alt";
    private final static String FLASH_TAG = "flash";

    private final static String IMAGE_TAG = "image";
    private final static String IMAGE_SOURCE_ATTR = "src";
    private final static String IMAGE_WIDTH_ATTR = "width";
    private final static String IMAGE_HEIGHT_ATTR = "height";

    private final static String OBJECT_TAG = "object";
    private final static String OBJECT_SOURCE_ATTR = "src";
    private final static String OBJECT_TYPE_ATTR = "mime-type";
    private final static String OBJECT_WIDTH_ATTR = "width";
    private final static String OBJECT_HEIGHT_ATTR = "height";

    private final static ImmutableSet<String> MEDIA_CHILD_TAGS = ImmutableSet.of(
            FLASH_TAG,
            IMAGE_TAG,
            OBJECT_TAG);

    private final static String MEDIA_CHILD_FOR_ATTR = "for";
    private final static String MEDIA_CHILD_FOR_DEFAULT = "default";
    private final static String MEDIA_CHILD_FOR_PDF = "pdf";
    private final static String MEDIA_CHILD_FOR_OVERRIDE = "webview2.0";

    private final static String FIGURE_TAG = "figure";
    private final static String FIGURE_CAPTION_TAG = "caption";
    private final static String SUBFIGURE_TAG = "subfigure";
    private final static String FIGURE_ORIENT_ATTR = "orient";
    private final static String FIGURE_ORIENT_HORIZONTAL = "horizontal";
    private final static String FIGURE_ORIENT_VERTICAL = "vertical";

    private final static String CALS_TABLE_TAG = "table";
    private final static String CALS_TABLE_SUMMARY_ATTR = "summary";
    private final static String CALS_TABLE_GROUP_TAG = "tgroup";
    private final static String CALS_TABLE_ROW_TAG = "row";
    private final static String CALS_TABLE_CELL_TAG = "entry";
    private final static String CALS_TABLE_HEAD_TAG = "thead";
    private final static String CALS_TABLE_BODY_TAG = "tbody";
    private final static String CALS_TABLE_FOOT_TAG = "tfoot";

    private final static String CALS_ALIGN_ATTR = "align";
    private final static String CALS_ALIGN_LEFT = "left";
    private final static String CALS_ALIGN_RIGHT = "right";
    private final static String CALS_ALIGN_CENTER = "center";
    private final static String CALS_ALIGN_JUSTIFY = "justify";

    private final static String CALS_VALIGN_ATTR = "valign";
    private final static String CALS_VALIGN_TOP = "top";
    private final static String CALS_VALIGN_MIDDLE = "middle";
    private final static String CALS_VALIGN_BOTTOM = "bottom";

    private final static String CALS_COLSEP_ATTR = "colsep";
    private final static String CALS_ROWSEP_ATTR = "rowsep";
    private final static String CALS_NOSEP = "0";

    private final static String CALS_FRAME_ATTR = "frame";
    private final static String CALS_FRAME_ALL = "all";
    private final static String CALS_FRAME_SIDES = "sides";
    private final static String CALS_FRAME_TOP = "top";
    private final static String CALS_FRAME_BOTTOM = "bottom";
    private final static String CALS_FRAME_TOPBOTTOM = "topbot";

    private final static String HTML_ID_ATTR = "id";
    private final static String HTML_CLASS_ATTR = "class";
    private final static String HTML_DIV_TAG = "div";
    private final static String HTML_SPAN_TAG = "span";
    private final static String HTML_EMPHASIS_TAG = "em";
    private final static String HTML_STRONG_TAG = "strong";
    private final static String HTML_UNDERLINE_TAG = "u";
    private final static String HTML_PARAGRAPH_TAG = "p";
    private final static String HTML_SECTION_TAG = "section";
    private final static String HTML_HEADING_TAG = "h1";
    private final static String HTML_LINK_TAG = "a";
    private final static String HTML_ANCHOR_TAG = "a";
    private final static String HTML_LINK_URL_ATTR = "href";
    private final static String HTML_SUP_TAG = "sup";
    private final static String HTML_SUB_TAG = "sub";
    private final static String HTML_CODE_TAG = "code";
    private final static String HTML_PREFORMAT_TAG = "pre";
    private final static String HTML_LINE_BREAK_TAG = "br";
    private final static String HTML_HORIZONTAL_RULE_TAG = "hr";
    private final static String HTML_FIGURE_TAG = "figure";
    private final static String HTML_FIGURE_CAPTION_TAG = "figcaption";
    private final static String HTML_TABLE_TAG = "table";
    private final static String HTML_TABLE_ROW_TAG = "tr";
    private final static String HTML_TABLE_CELL_TAG = "td";
    private final static String HTML_TABLE_HEAD_CELL_TAG = "th";
    private final static String HTML_TABLE_HEAD_TAG = "thead";
    private final static String HTML_TABLE_BODY_TAG = "tbody";
    private final static String HTML_TABLE_FOOT_TAG = "tfoot";
    private final static String HTML_TABLE_CAPTION_TAG = "caption";

    private final static String HTML_ORDERED_LIST_TAG = "ol";
    private final static String HTML_UNORDERED_LIST_TAG = "ul";
    private final static String HTML_LIST_ITEM_TAG = "li";
    private final static String HTML_LIST_NUMBER_STYLE_ATTR = "type";
    private final static String HTML_LIST_START_VALUE_ATTR = "start";

    private final static String HTML_IMAGE_TAG = "img";
    private final static String HTML_IMAGE_ALT_ATTR = "alt";
    private final static String HTML_IMAGE_SOURCE_ATTR = "src";
    private final static String HTML_IMAGE_WIDTH_ATTR = "width";
    private final static String HTML_IMAGE_HEIGHT_ATTR = "height";

    private final static String HTML_OBJECT_TAG = "object";
    private final static String HTML_OBJECT_SOURCE_ATTR = "data";
    private final static String HTML_OBJECT_TYPE_ATTR = "type";
    private final static String HTML_OBJECT_WIDTH_ATTR = "width";
    private final static String HTML_OBJECT_HEIGHT_ATTR = "height";
    private final static String HTML_PARAM_TAG = "param";
    private final static String HTML_PARAM_NAME_ATTR = "name";
    private final static String HTML_PARAM_VALUE_ATTR = "value";
    private final static String HTML_PARAM_SOURCE = "src";

    private final static String HTML_EMBED_TAG = "embed";
    private final static String HTML_EMBED_SOURCE_ATTR = "src";
    private final static String HTML_EMBED_TYPE_ATTR = "type";
    private final static String HTML_EMBED_WIDTH_ATTR = "width";
    private final static String HTML_EMBED_HEIGHT_ATTR = "height";

    private final static String HTML_STYLE_ATTR = "style";
    private final static String CSS_DISPLAY_NONE = "display:none;";

    private final static String HTML_TITLE_CLASS = "title";
    private final static String HTML_FOREIGN_CLASS = "foreign";
    private final static String HTML_TERM_CLASS = "term";
    private final static String HTML_SMALLCAPS_CLASS = "smallcaps";
    private final static String HTML_NORMAL_CLASS = "normal";
    private final static String HTML_NOTE_CLASS = "note";
    private final static String HTML_EXERCISE_CLASS = "exercise";
    private final static String HTML_COMMENTARY_CLASS = "commentary";
    private final static String HTML_PROBLEM_CLASS = "problem";
    private final static String HTML_SOLUTION_CLASS = "solution";
    private final static String HTML_DEFINITION_CLASS = "definition";
    private final static String HTML_MEANING_CLASS = "meaning";
    private final static String HTML_EQUATION_CLASS = "equation";
    private final static String HTML_RULE_CLASS = "rule";
    private final static String HTML_STATEMENT_CLASS = "statement";
    private final static String HTML_PROOF_CLASS = "proof";
    private final static String HTML_EXAMPLE_CLASS = "example";
    private final static String HTML_CDF_DOWNLOAD_CLASS = "downloadLink";
    private final static String HTML_SUBFIGURE_CLASS = "subfigure";
    private final static String HTML_FIGURE_VERTICAL_CLASS = "vertical";
    private final static String HTML_FIGURE_HORIZONTAL_CLASS = "horizontal";

    private final static String CDF_MIME_TYPE = "application/vnd.wolfram.cdf";
    private final static String CDF_TEXT_MIME_TYPE = "application/vnd.wolfram.cdf.text";
    private final static String HTML_CDF_DOWNLOAD_LABEL = "Download CDF";

    private final static String HTML_CALS_TABLE_CLASS = "cals";
    private final static String HTML_CALS_ALIGN_LEFT_CLASS = "calsAlignLeft";
    private final static String HTML_CALS_ALIGN_RIGHT_CLASS = "calsAlignRight";
    private final static String HTML_CALS_ALIGN_CENTER_CLASS = "calsAlignCenter";
    private final static String HTML_CALS_ALIGN_JUSTIFY_CLASS = "calsAlignJustify";

    private final static String HTML_CALS_VALIGN_TOP_CLASS = "calsValignTop";
    private final static String HTML_CALS_VALIGN_MIDDLE_CLASS = "calsValignMiddle";
    private final static String HTML_CALS_VALIGN_BOTTOM_CLASS = "calsValignBottom";

    private final static String HTML_CALS_ROWSEP_CLASS = "calsRowsep";
    private final static String HTML_CALS_NO_ROWSEP_CLASS = "calsNoRowsep";

    private final static String HTML_CALS_COLSEP_CLASS = "calsColsep";
    private final static String HTML_CALS_NO_COLSEP_CLASS = "calsNoColsep";

    private final static String HTML_CALS_FRAME_ALL_CLASS = "calsFrameAll";
    private final static String HTML_CALS_FRAME_SIDES_CLASS = "calsFrameSides";
    private final static String HTML_CALS_FRAME_TOP_CLASS = "calsFrameTop";
    private final static String HTML_CALS_FRAME_BOTTOM_CLASS = "calsFrameBottom";
    private final static String HTML_CALS_FRAME_TOPBOTTOM_CLASS = "calsFrameTopBottom";

    private final static ImmutableMap<String, String> CALS_FRAME_HTML_CLASS_MAP = ImmutableMap.of(
            CALS_FRAME_ALL, HTML_CALS_FRAME_ALL_CLASS,
            CALS_FRAME_SIDES, HTML_CALS_FRAME_SIDES_CLASS,
            CALS_FRAME_TOP, HTML_CALS_FRAME_TOP_CLASS,
            CALS_FRAME_BOTTOM, HTML_CALS_FRAME_BOTTOM_CLASS,
            CALS_FRAME_TOPBOTTOM, HTML_CALS_FRAME_TOPBOTTOM_CLASS);

    private final static ImmutableMap<String, String> CALS_ALIGN_HTML_CLASS_MAP = ImmutableMap.of(
            CALS_ALIGN_LEFT, HTML_CALS_ALIGN_LEFT_CLASS,
            CALS_ALIGN_RIGHT, HTML_CALS_ALIGN_RIGHT_CLASS,
            CALS_ALIGN_CENTER, HTML_CALS_ALIGN_CENTER_CLASS,
            CALS_ALIGN_JUSTIFY, HTML_CALS_ALIGN_JUSTIFY_CLASS);

    private final static ImmutableMap<String, String> CALS_VALIGN_HTML_CLASS_MAP = ImmutableMap.of(
            CALS_VALIGN_TOP, HTML_CALS_VALIGN_TOP_CLASS,
            CALS_VALIGN_MIDDLE, HTML_CALS_VALIGN_MIDDLE_CLASS,
            CALS_VALIGN_BOTTOM, HTML_CALS_VALIGN_BOTTOM_CLASS);

    private final ImmutableSet<Processor> processors;
    private final JdomHtmlSerializer jdomHtmlSerializer;
    private final Namespace cnxmlNamespace;
    private Stack<GeneratorFrame> stack;
    private Counter counter;
    private final MediaElementFilter mediaFilter;

    /**
     *  GeneratorFrame holds one stack frame of the HTML tree generation process.
     */
    private static class GeneratorFrame {
        public final Iterator<Content> iterator;
        public final Element htmlParent;
        public final boolean unwrapContent;

        public GeneratorFrame(Element contentElement, Element htmlParent) {
            this(contentElement, htmlParent, false);
        }

        public GeneratorFrame(Element contentElement, Element htmlParent, boolean unwrapContent) {
            this((Iterator<Content>)contentElement.getContent().iterator(),
                    htmlParent, unwrapContent);
        }

        public GeneratorFrame(Iterator<Content> iterator, Element htmlParent,
                boolean unwrapContent) {
            this.iterator = checkNotNull(iterator);
            this.htmlParent = checkNotNull(htmlParent);
            this.unwrapContent = unwrapContent;
        }
    }

    /**
     *  MediaElementFilter only yields elements that are media children.
     *  <p>
     *  Examples of elements yielded are image, flash, and object.
     */
    private static class MediaElementFilter implements Filter {
        private final Namespace cnxmlNamespace;

        public MediaElementFilter(final Namespace cnxmlNamespace) {
            this.cnxmlNamespace = checkNotNull(cnxmlNamespace);
        }

        @Override public boolean matches(Object obj) {
            if (obj instanceof Element) {
                final Element elem = (Element)obj;
                return cnxmlNamespace.getURI().equals(elem.getNamespaceURI())
                        && MEDIA_CHILD_TAGS.contains(elem.getName());
            }
            return false;
        }
    }

    @Inject public JdomHtmlGenerator(Set<Processor> processors,
            JdomHtmlSerializer jdomHtmlSerializer, @CnxmlNamespace String cnxmlNamespace) {
        this.processors = ImmutableSet.copyOf(processors);
        this.jdomHtmlSerializer = jdomHtmlSerializer;
        this.cnxmlNamespace = Namespace.getNamespace(cnxmlNamespace);
        this.mediaFilter = new MediaElementFilter(this.cnxmlNamespace);
    }

    @Override public String generate(Module module) throws Exception {
        // Apply processors
        for (Processor processor : processors) {
            module = processor.process(module);
        }

        // Convert to JDOM
        // TODO(light): don't use DOMBuilder: it uses recursion
        final Document jdomDocument = new DOMBuilder().build(module.getCnxml());
        final Element contentElem = jdomDocument.getRootElement()
                .getChild("content", cnxmlNamespace);

        // Render to HTML
        counter = new Counter();
        StringBuilder sb = new StringBuilder();
        final List<Content> contentList = generateHtmlTree(contentElem);
        for (Content content : contentList) {
            jdomHtmlSerializer.serialize(sb, content);
        }
        counter = null;
        return sb.toString();
    }

    /**
     *  This method creates an intermediary HTML JDOM tree that represents the content.
     *
     *  @param contentRoot The content element from the CNXML
     */
    protected List<Content> generateHtmlTree(Element contentRoot) {
        stack = new Stack<GeneratorFrame>();

        // Create a dummy element so that top-level elements can still add to htmlParent.
        Element dummy = new Element("dummy");
        stack.push(new GeneratorFrame(contentRoot, dummy));

        while (!stack.empty()) {
            final GeneratorFrame frame = stack.peek();
            if (!frame.iterator.hasNext()) {
                stack.pop();
                if (frame.unwrapContent) {
                    final Element grandparent = (Element)frame.htmlParent.getParent();
                    grandparent.addContent(grandparent.indexOf(frame.htmlParent),
                            frame.htmlParent.removeContent());
                    frame.htmlParent.detach();
                }
                continue;
            }

            final Content child = frame.iterator.next();
            if (child instanceof Text) {
                frame.htmlParent.addContent((Text)child.clone());
            } else if (child instanceof Element) {
                generateElement((Element)child);
            }
        }

        // Detach content from dummy element and return the list of content nodes.
        return (List<Content>)dummy.removeContent();
    }

    protected void generateElement(final Element elem) {
        final GeneratorFrame frame = stack.peek();
        final String name = elem.getName();

        if (cnxmlNamespace.getURI().equals(elem.getNamespaceURI())) {
            if (PARAGRAPH_TAG.equals(name)) {
                generateParagraph(elem);
            } else if (SECTION_TAG.equals(name)) {
                generateSection(elem);
            } else if (EMPHASIS_TAG.equals(name)) {
                generateEmphasis(elem);
            } else if (LINK_TAG.equals(name)
                    || FOREIGN_TAG.equals(name)
                    || TERM_TAG.equals(name)) {
                generateLink(elem);
            } else if (SUB_TAG.equals(name)) {
                generateSub(elem);
            } else if (SUP_TAG.equals(name)) {
                generateSup(elem);
            } else if (PREFORMAT_TAG.equals(name)) {
                generatePreformat(elem);
            } else if (CODE_TAG.equals(name)) {
                generateCode(elem);
            } else if (NOTE_TAG.equals(name)) {
                generateNote(elem);
            } else if (DEFINITION_TAG.equals(name)) {
                generateDefinition(elem);
            } else if (MEANING_TAG.equals(name)) {
                generateMeaning(elem);
            } else if (EXERCISE_TAG.equals(name)) {
                generateExercise(elem);
            } else if (COMMENTARY_TAG.equals(name)) {
                generateCommentary(elem);
            } else if (PROBLEM_TAG.equals(name)) {
                generateProblem(elem);
            } else if (SOLUTION_TAG.equals(name)) {
                generateSolution(elem);
            } else if (EQUATION_TAG.equals(name)) {
                generateEquation(elem);
            } else if (FIGURE_TAG.equals(name)) {
                generateFigure(elem);
            } else if (RULE_TAG.equals(name)) {
                generateRule(elem);
            } else if (STATEMENT_TAG.equals(name)) {
                generateStatement(elem);
            } else if (PROOF_TAG.equals(name)) {
                generateProof(elem);
            } else if (EXAMPLE_TAG.equals(name)) {
                generateExample(elem);
            } else if (LIST_TAG.equals(name)) {
                generateList(elem);
            } else if (LIST_ITEM_TAG.equals(name)) {
                generateListItem(elem);
            } else if (NEWLINE_TAG.equals(name)) {
                generateNewline(elem);
            } else if (MEDIA_TAG.equals(name)) {
                generateMedia(elem);
            } else if (SUBFIGURE_TAG.equals(name)) {
                generateSubfigure(elem);
            } else if (CALS_TABLE_TAG.equals(name)) {
                generateTable(elem);
            } else if (CALS_TABLE_ROW_TAG.equals(name)) {
                generateTableRow(elem);
            } else if (CALS_TABLE_CELL_TAG.equals(name)) {
                generateTableCell(elem);
            } else if (!TITLE_TAG.equals(name) && !LABEL_TAG.equals(name)
                    && !FIGURE_CAPTION_TAG.equals(name)) {
                addHtmlElement(new Element(HTML_DIV_TAG)
                        .setAttribute(HTML_CLASS_ATTR, "unhandled")
                        .setText("Unrecognized Content: " + name)
                );
            }
        } else if (MATHML_NAMESPACE.equals(elem.getNamespaceURI())) {
            // Copy math without modification.  If we add it directly, it detaches from the original
            // CNXML tree.
            // TODO(light): Don't use clone, it's recursive.
            addHtmlElement((Element)elem.clone());
        }
    }

    /**
     *  This method adds the HTML element to the top parent in the stack.
     */
    protected void addHtmlElement(final Element htmlElem) {
        stack.peek().htmlParent.addContent(htmlElem);
    }

    /**
     *  This method adds the HTML element to the top parent then pushes the HTML element onto the
     *  stack, using the given element for children.
     */
    protected void pushHtmlElement(final Element elem, final Element htmlElem) {
        addHtmlElement(htmlElem);
        stack.push(new GeneratorFrame(elem, htmlElem));
    }

    protected static Element copyId(final Element elem, final Element htmlElem) {
        final String id = elem.getAttributeValue(ID_ATTR);
        if (id != null) {
            htmlElem.setAttribute(HTML_ID_ATTR, id);
        }
        return htmlElem;
    }

    protected void generateParagraph(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HTML_PARAGRAPH_TAG));
        final String title = elem.getChildText(TITLE_TAG, cnxmlNamespace);
        if (title != null) {
            addHtmlElement(new Element(HTML_DIV_TAG)
                    .setAttribute(HTML_CLASS_ATTR, HTML_TITLE_CLASS)
                    .setText(title));
        }
        pushHtmlElement(elem, htmlElem);
    }

    protected void generateSection(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HTML_SECTION_TAG));
        final String title = elem.getChildText(TITLE_TAG, cnxmlNamespace);
        if (title != null) {
            htmlElem.addContent(new Element(HTML_HEADING_TAG).setText(title));
        }
        pushHtmlElement(elem, htmlElem);
    }

    protected void generateEmphasis(final Element elem) {
        final Element htmlElem = new Element(HTML_SPAN_TAG);
        final String effect = elem.getAttributeValue(EFFECT_ATTR);

        if (EFFECT_ITALICS.equals(effect)) {
            htmlElem.setName(HTML_EMPHASIS_TAG);
        } else if (EFFECT_UNDERLINE.equals(effect)) {
            htmlElem.setName(HTML_UNDERLINE_TAG);
        } else if (EFFECT_SMALLCAPS.equals(effect)) {
            htmlElem.setAttribute(HTML_CLASS_ATTR, HTML_SMALLCAPS_CLASS);
        } else if (EFFECT_NORMAL.equals(effect)) {
            htmlElem.setAttribute(HTML_CLASS_ATTR, HTML_NORMAL_CLASS);
        } else {
            htmlElem.setName(HTML_STRONG_TAG);
        }

        copyId(elem, htmlElem);
        pushHtmlElement(elem, htmlElem);
    }

    protected void generateLink(final Element elem) {
        final Element htmlElem = new Element(HTML_LINK_TAG);
        final String name = elem.getName();
        final String url = elem.getAttributeValue(LINK_URL_ATTR);
        final String targetId = elem.getAttributeValue(LINK_TARGET_ID_ATTR);

        if (FOREIGN_TAG.equals(name)) {
            htmlElem.setAttribute(HTML_CLASS_ATTR, HTML_FOREIGN_CLASS);
        } else if (TERM_TAG.equals(name)) {
            htmlElem.setAttribute(HTML_CLASS_ATTR, HTML_TERM_CLASS);
        }

        copyId(elem, htmlElem);

        if (url != null) {
            htmlElem.setAttribute(HTML_LINK_URL_ATTR, url);
            pushHtmlElement(elem, htmlElem);
        } else if (targetId != null && elem.getAttribute(LINK_DOCUMENT_ATTR) == null) {
            htmlElem.setAttribute(HTML_LINK_URL_ATTR, "#" + targetId);
            pushHtmlElement(elem, htmlElem);
        } else if (FOREIGN_TAG.equals(name) || TERM_TAG.equals(name)) {
            htmlElem.setName(HTML_SPAN_TAG);
            pushHtmlElement(elem, htmlElem);
        } else {
            stack.push(new GeneratorFrame(elem, stack.peek().htmlParent));
        }
    }

    protected void generateSup(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HTML_SUP_TAG)));
    }

    protected void generateSub(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HTML_SUB_TAG)));
    }

    protected void generatePreformat(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HTML_PREFORMAT_TAG)));
    }

    protected void generateCode(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HTML_CODE_TAG));
        final String display = elem.getAttributeValue(DISPLAY_ATTR);

        if (DISPLAY_NONE.equals(display)) {
            pushHtmlElement(elem, htmlElem.setAttribute(HTML_STYLE_ATTR, CSS_DISPLAY_NONE));
        } else if (DISPLAY_BLOCK.equals(display)) {
            addHtmlElement(
                    new Element(HTML_PREFORMAT_TAG).addContent(htmlElem));
            stack.push(new GeneratorFrame(elem, htmlElem));
        } else {
            pushHtmlElement(elem, htmlElem);
        }
    }

    protected void generateNote(final Element elem) {
        final String display = elem.getAttributeValue(DISPLAY_ATTR);
        if (DISPLAY_INLINE.equals(display)) {
            pushHtmlElement(elem, copyId(elem, new Element(HTML_SPAN_TAG)
                        .setAttribute(HTML_CLASS_ATTR, HTML_NOTE_CLASS)));
            return;
        }

        final Element htmlElem = new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_NOTE_CLASS);
        copyId(elem, htmlElem);

        String label = elem.getChildText(LABEL_TAG, cnxmlNamespace);
        if (label == null) {
            final String type = elem.getAttributeValue(TYPE_ATTR);
            if (NOTE_TYPE_ASIDE.equals(type)) {
                label = NOTE_LABEL_ASIDE;
            } else if (NOTE_TYPE_WARNING.equals(type)) {
                label = NOTE_LABEL_WARNING;
            } else if (NOTE_TYPE_TIP.equals(type)) {
                label = NOTE_LABEL_TIP;
            } else if (NOTE_TYPE_IMPORTANT.equals(type)) {
                label = NOTE_LABEL_IMPORTANT;
            } else {
                label = NOTE_LABEL_NOTE;
            }
        }

        final Element htmlTitleElem = new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_TITLE_CLASS);
        final String title = elem.getChildText(TITLE_TAG, cnxmlNamespace);
        if (title != null) {
            htmlTitleElem.setText(label + ": " + title);
        } else {
            htmlTitleElem.setText(label + ":");
        }
        htmlElem.addContent(htmlTitleElem);

        pushHtmlElement(elem, htmlElem);
    }

    protected int getNumber(final Element elem) {
        final String type = elem.getAttributeValue(TYPE_ATTR);
        return counter.getNextNumber(elem.getName(), (type != null ? type : ""));
    }

    protected Element numberedContainer(final Element elem,
            final String className, final String defaultLabel) {
        final Element htmlElem = copyId(elem, new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, className));

        String label = elem.getChildText(LABEL_TAG, cnxmlNamespace);
        if (label == null) {
            label = defaultLabel;
        }

        final Element htmlTitleElem = new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_TITLE_CLASS);
        final String title = elem.getChildText(TITLE_TAG, cnxmlNamespace);
        final int number = getNumber(elem);
        if (title != null) {
            htmlTitleElem.setText(label + " " + number + ": " + title);
        } else {
            htmlTitleElem.setText(label + " " + number);
        }
        htmlElem.addContent(htmlTitleElem);

        return htmlElem;
    }

    protected void generateDefinition(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_DEFINITION_CLASS));

        final Element term = elem.getChild(TERM_TAG, cnxmlNamespace);
        final int number = getNumber(elem);
        String label = elem.getChildText(LABEL_TAG, cnxmlNamespace);
        if (label == null) {
            label = DEFINITION_LABEL;
        }
        final Element htmlTitleElem = new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_TITLE_CLASS);
        htmlTitleElem.addContent(label + " " + number);
        // TODO(light): This doesn't handle links.
        if (term != null) {
            htmlTitleElem.addContent(": " + term.getText());
        }

        // TODO(light): Allow interspersed examples with meanings
        final Element htmlMeaningList = new Element(HTML_ORDERED_LIST_TAG);

        addHtmlElement(htmlElem);
        final List<Content> children = (List<Content>)elem.getContent(new Filter() {
            @Override public boolean matches(Object obj) {
                if (obj instanceof Element) {
                    final Element elem = (Element)obj;
                    return !(cnxmlNamespace.getURI().equals(elem.getNamespaceURI())
                            && TERM_TAG.equals(elem.getName()));
                }
                return true;
            }
        });
        stack.push(new GeneratorFrame(children.iterator(), htmlMeaningList, false));
        htmlElem.addContent(htmlTitleElem).addContent(htmlMeaningList);
    }

    protected void generateMeaning(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HTML_LIST_ITEM_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_MEANING_CLASS)));
    }

    protected void generateExercise(final Element elem) {
        pushHtmlElement(elem, numberedContainer(elem, HTML_EXERCISE_CLASS, EXERCISE_LABEL));
    }

    protected void generateCommentary(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_COMMENTARY_CLASS)));
    }

    protected void generateProblem(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_PROBLEM_CLASS)));
    }

    protected void generateSolution(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_SOLUTION_CLASS)));
    }

    protected void generateEquation(final Element elem) {
        pushHtmlElement(elem, numberedContainer(elem, HTML_EQUATION_CLASS, EQUATION_LABEL));
    }

    protected void generateFigure(final Element elem) {
        final String title = elem.getChildText(TITLE_TAG, cnxmlNamespace);
        if (title != null) {
            addHtmlElement(new Element(HTML_DIV_TAG)
                    .setAttribute(HTML_CLASS_ATTR, HTML_TITLE_CLASS)
                    .setText(title));
        }

        final Element htmlElem = copyId(elem, new Element(HTML_FIGURE_TAG));
        if (elem.getChild(SUBFIGURE_TAG, cnxmlNamespace) != null) {
            final String orient = elem.getAttributeValue(FIGURE_ORIENT_ATTR);
            if (FIGURE_ORIENT_VERTICAL.equals(orient)) {
                htmlElem.setAttribute(HTML_CLASS_ATTR, HTML_FIGURE_VERTICAL_CLASS);
            } else {
                htmlElem.setAttribute(HTML_CLASS_ATTR, HTML_FIGURE_HORIZONTAL_CLASS);
            }
        }

        final Element htmlCaptionElem = new Element(HTML_FIGURE_CAPTION_TAG);
        final String label = elem.getChildText(LABEL_TAG, cnxmlNamespace);
        final String caption = elem.getChildText(FIGURE_CAPTION_TAG, cnxmlNamespace);
        final int number = getNumber(elem);
        htmlCaptionElem.addContent((label != null ? label : FIGURE_LABEL) + " " + number);
        if (caption != null) {
            htmlCaptionElem.addContent(": " + caption);
        }

        final Element tempSpan = new Element(HTML_SPAN_TAG);
        addHtmlElement(htmlElem.addContent(tempSpan).addContent(htmlCaptionElem));
        stack.push(new GeneratorFrame(elem, tempSpan, true));
    }

    protected void generateSubfigure(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_SUBFIGURE_CLASS)));
    }

    protected void generateRule(final Element elem) {
        final String type = elem.getAttributeValue(TYPE_ATTR);
        String defaultLabel = RULE_LABEL_RULE;
        if (RULE_TYPE_THEOREM.equals(type)) {
            defaultLabel = RULE_LABEL_THEOREM;
        } else if (RULE_TYPE_LEMMA.equals(type)) {
            defaultLabel = RULE_LABEL_LEMMA;
        } else if (RULE_TYPE_COROLLARY.equals(type)) {
            defaultLabel = RULE_LABEL_COROLLARY;
        } else if (RULE_TYPE_LAW.equals(type)) {
            defaultLabel = RULE_LABEL_LAW;
        } else if (RULE_TYPE_PROPOSITION.equals(type)) {
            defaultLabel = RULE_LABEL_PROPOSITION;
        } else if (RULE_TYPE_RULE.equals(type)) {
            defaultLabel = RULE_LABEL_RULE;
        }
        pushHtmlElement(elem, numberedContainer(elem, HTML_RULE_CLASS, defaultLabel));
    }

    protected void generateStatement(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HTML_DIV_TAG)
                    .setAttribute(HTML_CLASS_ATTR, HTML_STATEMENT_CLASS)));
    }

    protected void generateProof(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HTML_DIV_TAG)
                    .setAttribute(HTML_CLASS_ATTR, HTML_PROOF_CLASS)
                    .addContent(new Element(HTML_DIV_TAG)
                            .setAttribute(HTML_CLASS_ATTR, HTML_TITLE_CLASS)
                            .setText(PROOF_LABEL))));
    }

    protected void generateExample(final Element elem) {
        pushHtmlElement(elem, numberedContainer(elem, HTML_EXAMPLE_CLASS, EXAMPLE_LABEL));
    }

    protected void generateList(final Element elem) {
        final String display = elem.getAttributeValue(DISPLAY_ATTR);
        final String type = elem.getAttributeValue(LIST_TYPE_ATTR);
        Element htmlElem;

        // TODO(light): handle inline lists
        if (DISPLAY_INLINE.equals(display)) {
            return;
        }

        if (type == null || LIST_TYPE_BULLETED.equals(type)) {
            htmlElem = copyId(elem, new Element(HTML_UNORDERED_LIST_TAG));
        } else if (LIST_TYPE_ENUMERATED.equals(type)) {
            htmlElem = copyId(elem, new Element(HTML_ORDERED_LIST_TAG));
            final String numberStyle = elem.getAttributeValue(LIST_NUMBER_STYLE_ATTR);
            if (numberStyle != null) {
                htmlElem.setAttribute(HTML_LIST_NUMBER_STYLE_ATTR, numberStyle);
            }
            final String startValue = elem.getAttributeValue(LIST_START_VALUE_ATTR);
            if (startValue != null) {
                htmlElem.setAttribute(HTML_LIST_START_VALUE_ATTR, startValue);
            }
        } else {
            // TODO(light): gracefully handle other list types
            return;
        }

        if (DISPLAY_NONE.equals(display)) {
            htmlElem.setAttribute(HTML_STYLE_ATTR, CSS_DISPLAY_NONE);
        }

        pushHtmlElement(elem, htmlElem);
    }

    protected void generateListItem(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HTML_LIST_ITEM_TAG));
        final GeneratorFrame parentFrame = stack.peek();

        // If the list has an item separator and this is not the last item, then add the separator.
        if (parentFrame.iterator.hasNext()) {
            final String itemSep = ((Element)elem.getParent()).getAttributeValue(LIST_ITEM_SEP_ATTR);
            if (itemSep != null) {
                final Element tempSpan = new Element(HTML_SPAN_TAG);
                parentFrame.htmlParent.addContent(htmlElem
                        .addContent(tempSpan)
                        .addContent(itemSep));
                stack.push(new GeneratorFrame(elem, tempSpan, true));
                return;
            }
        }

        // Otherwise, the process is significantly simpler. :)
        pushHtmlElement(elem, htmlElem);
    }

    protected void generateNewline(final Element elem) {
        Element parent = stack.peek().htmlParent;
        final String id = elem.getAttributeValue(ID_ATTR);
        if (id != null) {
            final Element anchor = new Element(HTML_ANCHOR_TAG).setAttribute(HTML_ID_ATTR, id);
            parent.addContent(anchor);
            parent = anchor;
        }

        int count = 1;
        final String countAttr = elem.getAttributeValue(NEWLINE_COUNT_ATTR);
        if (countAttr != null) {
            try {
                count = Integer.valueOf(countAttr);
            } catch (NumberFormatException e) {
                // TODO(light): Log invalid CNXML
                return;
            }
        }
        final String effect = elem.getAttributeValue(NEWLINE_EFFECT_ATTR);
        if (NEWLINE_EFFECT_UNDERLINE.equals(effect)) {
            for (int i = 0; i < count; i++) {
                parent.addContent(new Element(HTML_HORIZONTAL_RULE_TAG));
            }
        } else {
            for (int i = 0; i < count; i++) {
                parent.addContent(new Element(HTML_LINE_BREAK_TAG));
            }
        }
    }

    protected void generateMedia(final Element elem) {
        Element child = null;

        // Select child
        for (Element e : (List<Element>)elem.getContent(mediaFilter)) {
            final String mediaFor = e.getAttributeValue(MEDIA_CHILD_FOR_ATTR);
            if (MEDIA_CHILD_FOR_OVERRIDE.equals(mediaFor)) {
                // XXX(light): A special for="webview2.0" attribute is used to force the media child
                // to be used.  This is necessary to provide backward compatibility with cnx.org and
                // still allow Mathematica to be embedded (the <object> element does not work as
                // documented on cnx.org).
                //
                // This hack should be removed when cnx.org is fixed or when cnx.org is migrated to
                // this renderer, whichever comes first.
                child = e;
                break;
            } else if (!MEDIA_CHILD_FOR_PDF.equals(mediaFor) && child == null) {
                child = e;
            }
        }

        // If no child is found, then don't render anything.
        if (child == null) {
            return;
        }

        if (IMAGE_TAG.equals(child.getName())) {
            generateImage(child);
        } else if (OBJECT_TAG.equals(child.getName())) {
            final String type = child.getAttributeValue(OBJECT_TYPE_ATTR);
            if (CDF_MIME_TYPE.equals(type) || CDF_TEXT_MIME_TYPE.equals(type)) {
                generateMathematica(child);
            } else {
                generateObject(child);
            }
        }
    }

    protected void generateImage(final Element elem) {
        final Element mediaElem = (Element)elem.getParent();
        final Element htmlElem = copyId(mediaElem, new Element(HTML_IMAGE_TAG))
                .setAttribute(HTML_IMAGE_ALT_ATTR, mediaElem.getAttributeValue(MEDIA_ALT_ATTR))
                .setAttribute(HTML_IMAGE_SOURCE_ATTR, elem.getAttributeValue(IMAGE_SOURCE_ATTR));
        final String width = elem.getAttributeValue(IMAGE_WIDTH_ATTR);
        if (width != null) {
            htmlElem.setAttribute(HTML_IMAGE_WIDTH_ATTR, width);
        }
        final String height = elem.getAttributeValue(IMAGE_HEIGHT_ATTR);
        if (height != null) {
            htmlElem.setAttribute(HTML_IMAGE_HEIGHT_ATTR, height);
        }
        addHtmlElement(htmlElem);
    }

    protected void generateObject(final Element elem) {
        final Element mediaElem = (Element)elem.getParent();
        final Element htmlElem = copyId(mediaElem, new Element(HTML_OBJECT_TAG))
                .setAttribute(HTML_OBJECT_SOURCE_ATTR, elem.getAttributeValue(OBJECT_SOURCE_ATTR))
                .setAttribute(HTML_OBJECT_TYPE_ATTR, elem.getAttributeValue(OBJECT_TYPE_ATTR));
        final String width = elem.getAttributeValue(OBJECT_WIDTH_ATTR);
        if (width != null) {
            htmlElem.setAttribute(HTML_OBJECT_WIDTH_ATTR, width);
        }
        final String height = elem.getAttributeValue(OBJECT_HEIGHT_ATTR);
        if (height != null) {
            htmlElem.setAttribute(HTML_OBJECT_HEIGHT_ATTR, height);
        }
        htmlElem.setText(mediaElem.getAttributeValue(MEDIA_ALT_ATTR));
        addHtmlElement(htmlElem);
    }

    protected void generateMathematica(final Element elem) {
        final Element mediaElem = (Element)elem.getParent();
        final String type = elem.getAttributeValue(OBJECT_TYPE_ATTR);
        final String source = elem.getAttributeValue(OBJECT_SOURCE_ATTR);
        final String width = elem.getAttributeValue(OBJECT_WIDTH_ATTR);
        final String height = elem.getAttributeValue(OBJECT_HEIGHT_ATTR);

        final Element htmlEmbedElem = new Element(HTML_EMBED_TAG)
                .setAttribute(HTML_EMBED_SOURCE_ATTR, source)
                .setAttribute(HTML_EMBED_TYPE_ATTR, type);
        final Element htmlObjectElem = copyId(mediaElem, new Element(HTML_OBJECT_TAG))
                .setAttribute(HTML_OBJECT_SOURCE_ATTR, source)
                .setAttribute(HTML_OBJECT_TYPE_ATTR, type)
                .addContent(new Element(HTML_PARAM_TAG)
                        .setAttribute(HTML_PARAM_NAME_ATTR, HTML_PARAM_SOURCE)
                        .setAttribute(HTML_PARAM_VALUE_ATTR, source))
                .addContent(htmlEmbedElem);
        if (width != null) {
            htmlObjectElem.setAttribute(HTML_OBJECT_WIDTH_ATTR, width);
            htmlEmbedElem.setAttribute(HTML_EMBED_WIDTH_ATTR, width);
        }
        if (height != null) {
            htmlObjectElem.setAttribute(HTML_OBJECT_HEIGHT_ATTR, height);
            htmlEmbedElem.setAttribute(HTML_EMBED_HEIGHT_ATTR, height);
        }
        stack.peek().htmlParent
                .addContent(htmlObjectElem)
                .addContent(new Element(HTML_DIV_TAG)
                        .setAttribute(HTML_CLASS_ATTR, HTML_CDF_DOWNLOAD_CLASS)
                        .addContent(new Element(HTML_LINK_TAG)
                                .setAttribute(HTML_LINK_URL_ATTR, source)
                                .setText(HTML_CDF_DOWNLOAD_LABEL)));
    }

    // TODO(light): allow multiple tgroups
    protected void generateTable(final Element elem) {
        final Element tgroup = elem.getChild(CALS_TABLE_GROUP_TAG, cnxmlNamespace);
        final Element htmlElem = copyId(elem, new Element(HTML_TABLE_TAG));

        final List<String> classList = computeTableClasses(tgroup);
        classList.add(0, HTML_CALS_TABLE_CLASS);

        String frameClass = CALS_FRAME_HTML_CLASS_MAP.get(elem.getAttributeValue(CALS_FRAME_ATTR));
        if (frameClass == null) {
            frameClass = HTML_CALS_FRAME_ALL_CLASS;
        }
        classList.add(1, frameClass);

        htmlElem.setAttribute(HTML_CLASS_ATTR, Joiner.on(' ').join(classList));

        addHtmlElement(htmlElem);

        // This must be done in reverse order (for the stack).
        pushTablePart(tgroup, CALS_TABLE_FOOT_TAG, htmlElem, HTML_TABLE_FOOT_TAG);
        pushTablePart(tgroup, CALS_TABLE_BODY_TAG, htmlElem, HTML_TABLE_BODY_TAG);
        pushTablePart(tgroup, CALS_TABLE_HEAD_TAG, htmlElem, HTML_TABLE_HEAD_TAG);

        final Element htmlCaptionElem = new Element(HTML_TABLE_CAPTION_TAG);
        final String title = elem.getChildText(TITLE_TAG, cnxmlNamespace);
        final String summary = elem.getAttributeValue(CALS_TABLE_SUMMARY_ATTR);
        final int number = getNumber(elem);

        final Element htmlTitleElem = new Element(HTML_DIV_TAG)
                .setAttribute(HTML_CLASS_ATTR, HTML_TITLE_CLASS)
                .addContent(CALS_TABLE_LABEL + " " + number);
        if (title != null) {
            htmlTitleElem.addContent(": " + title);
        }
        
        htmlCaptionElem.addContent(htmlTitleElem);
        if (summary != null) {
            htmlCaptionElem.addContent(summary);
        }

        htmlElem.addContent(htmlCaptionElem);
    }

    protected void pushTablePart(final Element tableGroupElem, final String name,
            final Element htmlTableElem, final String htmlName) {
        final Element elem = tableGroupElem.getChild(name, cnxmlNamespace);
        if (elem == null) {
            return;
        }

        final Element htmlElem = new Element(htmlName);
        final List<String> classList = computeTableClasses(elem);
        if (!classList.isEmpty()) {
            htmlElem.setAttribute(HTML_CLASS_ATTR, Joiner.on(' ').join(classList));
        }
        htmlTableElem.addContent(0, htmlElem);
        stack.push(new GeneratorFrame(elem, htmlElem));
    }

    protected void generateTableRow(final Element elem) {
        final Element htmlElem = new Element(HTML_TABLE_ROW_TAG);
        final List<String> classList = computeTableClasses(elem);
        if (!classList.isEmpty()) {
            htmlElem.setAttribute(HTML_CLASS_ATTR, Joiner.on(' ').join(classList));
        }
        pushHtmlElement(elem, htmlElem);
    }

    protected void generateTableCell(final Element elem) {
        final String grandparentName = ((Element)elem.getParent().getParent()).getName();
        final String htmlName = CALS_TABLE_HEAD_TAG.equals(grandparentName)
                ? HTML_TABLE_HEAD_CELL_TAG : HTML_TABLE_CELL_TAG;
        final Element htmlElem = new Element(htmlName);

        final List<String> classList = computeTableClasses(elem);
        if (!classList.isEmpty()) {
            htmlElem.setAttribute(HTML_CLASS_ATTR, Joiner.on(' ').join(classList));
        }
        pushHtmlElement(elem, htmlElem);
    }

    /**
     *  Determine the HTML classes to be applied for a CALS table element.
     */
    protected List<String> computeTableClasses(final Element elem) {
        final ArrayList<String> classList = new ArrayList<String>(4);
        final String align = elem.getAttributeValue(CALS_ALIGN_ATTR);
        final String valign = elem.getAttributeValue(CALS_VALIGN_ATTR);
        final String colsep = elem.getAttributeValue(CALS_COLSEP_ATTR);
        final String rowsep = elem.getAttributeValue(CALS_ROWSEP_ATTR);

        if (align != null) {
            final String alignClass = CALS_ALIGN_HTML_CLASS_MAP.get(align);
            if (alignClass != null) {
                classList.add(alignClass);
            }
        }

        if (valign != null) {
            final String valignClass = CALS_VALIGN_HTML_CLASS_MAP.get(valign);
            if (valignClass != null) {
                classList.add(valignClass);
            }
        }

        if (colsep != null) {
            if (CALS_NOSEP.equals(colsep)) {
                classList.add(HTML_CALS_NO_COLSEP_CLASS);
            } else {
                classList.add(HTML_CALS_COLSEP_CLASS);
            }
        }

        if (rowsep != null) {
            if (CALS_NOSEP.equals(rowsep)) {
                classList.add(HTML_CALS_NO_ROWSEP_CLASS);
            } else {
                classList.add(HTML_CALS_ROWSEP_CLASS);
            }
        }

        return classList;
    }
}
