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

package org.cnx.cnxml;

import static com.google.common.base.Preconditions.*;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;
import org.cnx.util.ElementSetFilter;

import org.cnx.util.RenderTime;
import org.cnx.util.HtmlTag;
import org.cnx.util.HtmlAttributes;
import org.cnx.util.IdFilter;
import org.cnx.util.JdomHtmlSerializer;
import org.cnx.util.MathmlAttributes;
import org.cnx.util.MathmlTag;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Parent;
import org.jdom.Text;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.jdom.input.DOMBuilder;

/**
 *  JdomHtmlGenerator is an implementation of ModuleHtmlGenerator that generates HTML from a DOM
 *  structure.
 *
 *  It is iterative, but it is not thread-safe.
 */
@RenderTime public class JdomHtmlGenerator implements ModuleHtmlGenerator {
    private final static String NOTE_LABEL_ASIDE = "Aside";
    private final static String NOTE_LABEL_WARNING = "Warning";
    private final static String NOTE_LABEL_TIP = "Tip";
    private final static String NOTE_LABEL_IMPORTANT = "Important";
    private final static String NOTE_LABEL_NOTE = "Note";

    private final static String EXERCISE_LABEL = "Exercise";
    private final static String EQUATION_LABEL = "Equation";
    private final static String PROOF_LABEL = "Proof";
    private final static String SOLUTION_LABEL = "Solution";
    private final static String DEFINITION_LABEL = "Definition";
    private final static String EXAMPLE_LABEL = "Example";
    private final static String FIGURE_LABEL = "Figure";
    private final static String CALS_TABLE_LABEL = "Table";

    private final static String RULE_LABEL_THEOREM = "Theorem";
    private final static String RULE_LABEL_LEMMA = "Lemma";
    private final static String RULE_LABEL_COROLLARY = "Corollary";
    private final static String RULE_LABEL_LAW = "Law";
    private final static String RULE_LABEL_PROPOSITION = "Proposition";
    private final static String RULE_LABEL_RULE = "Rule";

    private final static String CSS_DISPLAY_NONE = "display:none;";

    private final static String HTML_TITLE_CLASS = "title";
    private final static String HTML_PARAGRAPH_TITLE_CLASS = "title paraTitle";
    private final static String HTML_EQUATION_TITLE_CLASS = "title equationTitle";
    private final static String HTML_FIGURE_TITLE_CLASS = "title figureTitle";
    private final static String HTML_PREFIX_CLASS = "prefix";

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
    private final static String HTML_EQUATION_CONTENT_CLASS = "equationContent";
    private final static String HTML_EQUATION_NUMBER_CLASS = "equationNumber";
    private final static String HTML_RULE_CLASS = "rule";
    private final static String HTML_STATEMENT_CLASS = "statement";
    private final static String HTML_PROOF_CLASS = "proof";
    private final static String HTML_EXAMPLE_CLASS = "example";
    private final static String HTML_DOWNLOAD_CLASS = "download";
    private final static String HTML_CDF_DOWNLOAD_CLASS = "downloadLink";
    private final static String HTML_SUBFIGURE_CLASS = "subfigure";
    private final static String HTML_SUBFIGURE_CONTENT_CLASS = "subfigureContent";
    private final static String HTML_SUBFIGURE_CAPTION_CLASS = "subfigureCaption";
    private final static String HTML_SUBFIGURE_CONTAINER_VERTICAL_CLASS =
            "subfigureContainer vertical";
    private final static String HTML_SUBFIGURE_CONTAINER_HORIZONTAL_CLASS =
            "subfigureContainer horizontal";
    private final static String HTML_LABELED_ITEM_LIST_CLASS = "labeled";

    private final static String SUBFIGURE_COUNTER_PREFIX = "(";
    private final static char SUBFIGURE_ALPHABET_START = 'a';
    private final static char SUBFIGURE_ALPHABET_SIZE = 26;
    private final static String SUBFIGURE_COUNTER_SUFFIX = ")";

    private final static String CDF_MIME_TYPE = "application/vnd.wolfram.cdf";
    private final static String CDF_TEXT_MIME_TYPE = "application/vnd.wolfram.cdf.text";

    private final static String HTML_DOWNLOAD_LABEL = "Download ";

    private final static String HTML_DEFAULT_LINK_TEXT = "Link";
    private final static String HTML_DEFAULT_FIGURE_LINK_TEXT = "Figure";
    private final static String HTML_DEFAULT_EQUATION_LINK_TEXT = "Equation";

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

    private final static String HTML_BULLET_STYLE_NONE_CLASS = "bulletStyleNone";

    private final static String UNRECOGNIZED_CONTENT_INNER_TEXT = "...";
    private final static String UNRECOGNIZED_CONTENT_MESSAGE = "Unrecognized Content: ";

    private final static ImmutableSet<CnxmlTag> NUMBERED_TAGS = ImmutableSet.of(
            CnxmlTag.DEFINITION,
            CnxmlTag.EQUATION,
            CnxmlTag.EXAMPLE,
            CnxmlTag.EXERCISE,
            CnxmlTag.FIGURE,
            CnxmlTag.RULE,
            CnxmlTag.TABLE
    );

    private static final Logger log = Logger.getLogger(JdomHtmlGenerator.class.getName());

    private static final Filter mediaFilter = cnxmlSetFilter(CnxmlTag.MEDIA_CHILDREN);
    private static final Filter numberedElementFilter = cnxmlSetFilter(NUMBERED_TAGS);

    private final ImmutableSet<Processor> processors;
    private final JdomHtmlSerializer jdomHtmlSerializer;

    private Module module;
    private Stack<GeneratorFrame> stack;
    private Map<String, Element> idElementMap;
    private Map<Element, Integer> elementNumberMap;

    private static final Filter cnxmlSetFilter(final ImmutableSet<CnxmlTag> tags) {
        final ArrayList<String> names = new ArrayList<String>(tags.size());
        for (CnxmlTag tag : tags) {
            names.add(tag.getTag());
        }
        return new ElementSetFilter(names, CnxmlTag.NAMESPACE);
    }

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

        @SuppressWarnings("unchecked")
        public GeneratorFrame(Element contentElement, Element htmlParent, boolean unwrapContent) {
            this((Iterator<Content>)checkNotNull(contentElement).getContent().iterator(),
                    htmlParent, unwrapContent);
        }

        public GeneratorFrame(Iterator<Content> iterator, Element htmlParent,
                boolean unwrapContent) {
            this.iterator = checkNotNull(iterator);
            this.htmlParent = checkNotNull(htmlParent);
            this.unwrapContent = unwrapContent;
        }
    }

    @Inject public JdomHtmlGenerator(Set<Processor> processors,
            JdomHtmlSerializer jdomHtmlSerializer) {
        this.processors = ImmutableSet.copyOf(processors);
        this.jdomHtmlSerializer = jdomHtmlSerializer;
    }

    @Override public String generate(Module module) throws Exception {
        long startTime, endTime;

        this.module = module;

        // Apply processors
        startTime = System.currentTimeMillis();
        for (Processor processor : processors) {
            module = processor.process(module);
        }
        endTime = System.currentTimeMillis();
        log.fine("Processors took " + (endTime - startTime) + " ms");
        
        // Get content element
        final Element contentElem = module.getCnxml().getRootElement()
                .getChild(CnxmlTag.CONTENT.getTag(), CnxmlTag.NAMESPACE);
        if (contentElem == null) {
            return "";
        }

        // Generate HTML
        startTime = System.currentTimeMillis();
        idElementMap = IdFilter.getIdMap(contentElem);
        elementNumberMap = enumerateElements(contentElem);
        final List<Content> contentList = generateHtmlTree(contentElem);
        final StringBuilder sb = new StringBuilder();
        for (Content content : contentList) {
            jdomHtmlSerializer.serialize(sb, content);
        }
        endTime = System.currentTimeMillis();
        log.fine("Rendered in " + (endTime - startTime) + " ms");

        return sb.toString();
    }

    /**
     *  Get numbers for elements.
     */
    @SuppressWarnings("unchecked")
    private static Map<Element, Integer> enumerateElements(final Parent contentRoot) {
        final Map<Element, Integer> map = new HashMap<Element, Integer>();
        final Iterator<Element> iter = contentRoot.getDescendants(numberedElementFilter);
        final Counter counter = new Counter();

        while (iter.hasNext()) {
            final Element elem = iter.next();
            final String type = elem.getAttributeValue(CnxmlAttributes.TYPE);
            map.put(elem, counter.getNextNumber(elem.getName(), Strings.nullToEmpty(type)));
        }
        return map;
    }

    /**
     *  This method creates an intermediary HTML JDOM tree that represents the content.
     *
     *  @param contentRoot The content element from the CNXML
     */
    @SuppressWarnings("unchecked")
    private List<Content> generateHtmlTree(Element contentRoot) {
        checkNotNull(contentRoot);
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

    private void generateElement(final Element elem) {
        final GeneratorFrame frame = stack.peek();
        final String name = elem.getName();

        if (CnxmlTag.NAMESPACE.getURI().equals(elem.getNamespaceURI())) {
            switch (CnxmlTag.of(name)) {
            case PARAGRAPH:
                generateParagraph(elem);
                break;
            case SECTION:
                generateSection(elem);
                break;
            case EMPHASIS:
                generateEmphasis(elem);
                break;
            case LINK:
            case FOREIGN:
            case TERM:
                generateLink(elem);
                break;
            case SUB:
                generateSub(elem);
                break;
            case SUP:
                generateSup(elem);
                break;
            case PREFORMAT:
                generatePreformat(elem);
                break;
            case CODE:
                generateCode(elem);
                break;
            case NOTE:
                generateNote(elem);
                break;
            case DEFINITION:
                generateDefinition(elem);
                break;
            case MEANING:
                generateMeaning(elem);
                break;
            case EXERCISE:
                generateExercise(elem);
                break;
            case COMMENTARY:
                generateCommentary(elem);
                break;
            case PROBLEM:
                generateProblem(elem);
                break;
            case SOLUTION:
                generateSolution(elem);
                break;
            case EQUATION:
                generateEquation(elem);
                break;
            case FIGURE:
                generateFigure(elem);
                break;
            case RULE:
                generateRule(elem);
                break;
            case STATEMENT:
                generateStatement(elem);
                break;
            case PROOF:
                generateProof(elem);
                break;
            case EXAMPLE:
                generateExample(elem);
                break;
            case LIST:
                generateList(elem);
                break;
            case LIST_ITEM:
                generateListItem(elem);
                break;
            case NEWLINE:
                generateNewline(elem);
                break;
            case MEDIA:
                generateMedia(elem);
                break;
            case TABLE:
                generateTable(elem);
                break;
            case TABLE_ROW:
                generateTableRow(elem);
                break;
            case TABLE_CELL:
                generateTableCell(elem);
                break;
            case LABEL:
            case FIGURE_CAPTION:
            case TITLE:
                // ignore
                break;
            case INVALID:
            default:
                unrecognized(elem);
                break;
            }
        } else if (MathmlTag.NAMESPACE_URI.equals(elem.getNamespaceURI())) {
            duplicateMath(elem);
        }
    }
    
    private void unrecognized(final Element elem) {
        unrecognized(elem, elem.getName());
    }

    /** Display a placeholder for an unrecognized element. */
    @SuppressWarnings("unchecked")
    private void unrecognized(final Element elem, final String message) {
        // Create a simpler version of the element for serialization
        final Element simpleElem = new Element(elem.getName())
                .setText(UNRECOGNIZED_CONTENT_INNER_TEXT);
        for (Attribute attr : (List<Attribute>)elem.getAttributes()) {
            simpleElem.setAttribute(attr.getName(), attr.getValue(), attr.getNamespace());
        }
        final String simpleText = jdomHtmlSerializer.serialize(simpleElem);

        log.warning("Found unrecognized element: " + simpleText);
        addHtmlContent(new Comment(simpleText));
        addHtmlContent(new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, "unhandled")
                .setText(UNRECOGNIZED_CONTENT_MESSAGE + message));
    }

    /**
     *  This method adds the HTML content to the top parent in the stack.
     */
    private void addHtmlContent(final Content content) {
        stack.peek().htmlParent.addContent(content);
    }

    /**
     *  This method adds the HTML element to the top parent then pushes the HTML element onto the
     *  stack, using the given element for children.
     */
    private void pushHtmlElement(final Element elem, final Element htmlElem) {
        addHtmlContent(htmlElem);
        stack.push(new GeneratorFrame(elem, htmlElem));
    }

    private static Element copyId(final Element elem, final Element htmlElem) {
        final String id = elem.getAttributeValue(CnxmlAttributes.ID);
        if (id != null) {
            htmlElem.setAttribute(HtmlAttributes.ID, id);
        }
        return htmlElem;
    }

    @SuppressWarnings("unchecked")
    private void duplicateMath(final Element elem) {
        final Element htmlElem = new Element(elem.getName());
        for (Attribute attr : (List<Attribute>)elem.getAttributes()) {
            htmlElem.getAttributes().add(new Attribute(attr.getName(), attr.getValue()));
        }

        switch (MathmlTag.of(elem.getName())) {
        case MATH:
            final String modeString = elem.getAttributeValue(MathmlAttributes.MODE);
            if (modeString != null) {
                htmlElem.removeAttribute(MathmlAttributes.MODE);
                if (elem.getAttribute(MathmlAttributes.DISPLAY) == null) {
                    final MathmlAttributes.Mode mode = MathmlAttributes.Mode.of(
                            modeString, MathmlAttributes.Mode.INLINE);
                    switch (mode) {
                    case INLINE:
                        htmlElem.setAttribute(MathmlAttributes.DISPLAY,
                                MathmlAttributes.Display.INLINE.getValue());
                        break;
                    case BLOCK:
                        htmlElem.setAttribute(MathmlAttributes.DISPLAY,
                                MathmlAttributes.Display.BLOCK.getValue());
                        break;
                    }
                }
            }
            break;
        }

        pushHtmlElement(elem, htmlElem);
    }

    private void generateParagraph(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HtmlTag.PARAGRAPH.getTag()));
        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        if (title != null) {
            addHtmlContent(new Element(HtmlTag.DIV.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_PARAGRAPH_TITLE_CLASS)
                    .setText(title));
        }
        pushHtmlElement(elem, htmlElem);
    }

    private void generateSection(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HtmlTag.SECTION.getTag()));
        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        if (title != null) {
            htmlElem.addContent(new Element(HtmlTag.HEADING.getTag()).setText(title));
        }
        pushHtmlElement(elem, htmlElem);
    }

    private void generateEmphasis(final Element elem) {
        final Element htmlElem = new Element(HtmlTag.SPAN.getTag());
        final CnxmlAttributes.EmphasisEffect effect = CnxmlAttributes.EmphasisEffect.of(
                elem.getAttributeValue(CnxmlAttributes.EFFECT),
                CnxmlAttributes.EmphasisEffect.BOLD);

        switch (effect) {
        case ITALICS:
            htmlElem.setName(HtmlTag.EMPHASIS.getTag());
            break;
        case UNDERLINE:
            htmlElem.setName(HtmlTag.UNDERLINE.getTag());
            break;
        case SMALLCAPS:
            htmlElem.setAttribute(HtmlAttributes.CLASS, HTML_SMALLCAPS_CLASS);
            break;
        case NORMAL:
            htmlElem.setAttribute(HtmlAttributes.CLASS, HTML_NORMAL_CLASS);
            break;
        case BOLD:
            htmlElem.setName(HtmlTag.STRONG.getTag());
            break;
        }

        copyId(elem, htmlElem);
        pushHtmlElement(elem, htmlElem);
    }

    private void generateLink(final Element elem) {
        final Element htmlElem = new Element(HtmlTag.LINK.getTag());
        final CnxmlTag tag = CnxmlTag.of(elem.getName());
        final String url = elem.getAttributeValue(CnxmlAttributes.LINK_URL);
        final String targetId = elem.getAttributeValue(CnxmlAttributes.LINK_TARGET_ID);

        if (tag == CnxmlTag.FOREIGN) {
            htmlElem.setAttribute(HtmlAttributes.CLASS, HTML_FOREIGN_CLASS);
        } else if (tag == CnxmlTag.TERM) {
            htmlElem.setAttribute(HtmlAttributes.CLASS, HTML_TERM_CLASS);
        }

        copyId(elem, htmlElem);

        // Default text (if no text found)
        if (elem.getContentSize() == 0) {
            String text = HTML_DEFAULT_LINK_TEXT;
            String ref = null;
            if (url != null && url.startsWith(Links.FRAGMENT)) {
                ref = url.substring(Links.FRAGMENT.length());
            } else if (targetId != null) {
                ref = targetId;
            }
            if (ref != null) {
                final Element refElem = idElementMap.get(ref);
                if (refElem != null && CnxmlTag.NAMESPACE_URI.equals(refElem.getNamespaceURI())) {
                    switch (CnxmlTag.of(refElem.getName())) {
                    case FIGURE:
                        text = HTML_DEFAULT_FIGURE_LINK_TEXT;
                        break;
                    case EQUATION:
                        text = HTML_DEFAULT_EQUATION_LINK_TEXT;
                        break;
                    }
                    if (elementNumberMap.containsKey(refElem)) {
                        text += " " + elementNumberMap.get(refElem);
                    }
                }
            }
            htmlElem.setText(text);
        }

        if (url != null) {
            htmlElem.setAttribute(HtmlAttributes.LINK_URL, url);
            pushHtmlElement(elem, htmlElem);
        } else if (targetId != null && elem.getAttribute(CnxmlAttributes.LINK_DOCUMENT) == null) {
            htmlElem.setAttribute(HtmlAttributes.LINK_URL, "#" + targetId);
            pushHtmlElement(elem, htmlElem);
        } else if (tag == CnxmlTag.FOREIGN || tag == CnxmlTag.TERM) {
            htmlElem.setName(HtmlTag.SPAN.getTag());
            pushHtmlElement(elem, htmlElem);
        } else {
            stack.push(new GeneratorFrame(elem, stack.peek().htmlParent));
        }
    }

    private void generateSup(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.SUP.getTag())));
    }

    private void generateSub(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.SUB.getTag())));
    }

    private void generatePreformat(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.PREFORMAT.getTag())));
    }

    private void generateCode(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HtmlTag.CODE.getTag()));
        final CnxmlAttributes.Display display = CnxmlAttributes.Display.of(
                elem.getAttributeValue(CnxmlAttributes.DISPLAY),
                CnxmlAttributes.Display.INLINE);

        switch (display) {
        case NONE:
            pushHtmlElement(elem, htmlElem.setAttribute(HtmlAttributes.STYLE, CSS_DISPLAY_NONE));
            break;
        case BLOCK:
            final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
            if (title != null) {
                addHtmlContent(new Element(HtmlTag.DIV.getTag())
                        .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
                        .setText(title));
            }

            addHtmlContent(
                    new Element(HtmlTag.PREFORMAT.getTag()).addContent(htmlElem));
            stack.push(new GeneratorFrame(elem, htmlElem));
            break;
        case INLINE:
            pushHtmlElement(elem, htmlElem);
        }
    }

    private void generateNote(final Element elem) {
        final CnxmlAttributes.Display display = CnxmlAttributes.Display.of(
                elem.getAttributeValue(CnxmlAttributes.DISPLAY),
                CnxmlAttributes.Display.BLOCK);
        if (display == CnxmlAttributes.Display.INLINE) {
            pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.SPAN.getTag())
                        .setAttribute(HtmlAttributes.CLASS, HTML_NOTE_CLASS)));
            return;
        }

        final Element htmlElem = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_NOTE_CLASS);
        copyId(elem, htmlElem);

        String label = elem.getChildText(CnxmlTag.LABEL.getTag(), CnxmlTag.NAMESPACE);
        if (label == null) {
            final CnxmlAttributes.NoteType type = CnxmlAttributes.NoteType.of(
                    elem.getAttributeValue(CnxmlAttributes.TYPE),
                    CnxmlAttributes.NoteType.NOTE);
            switch (type) {
            case ASIDE:
                label = NOTE_LABEL_ASIDE;
                break;
            case WARNING:
                label = NOTE_LABEL_WARNING;
                break;
            case TIP:
                label = NOTE_LABEL_TIP;
                break;
            case IMPORTANT:
                label = NOTE_LABEL_IMPORTANT;
                break;
            case NOTE:
                label = NOTE_LABEL_NOTE;
                break;
            }
        }

        final Element htmlTitleElem = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS);
        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        if (title != null) {
            htmlTitleElem.setText(label + ": " + title);
        } else {
            htmlTitleElem.setText(label + ":");
        }
        htmlElem.addContent(htmlTitleElem);

        pushHtmlElement(elem, htmlElem);
    }

    private Element numberedContainer(final Element elem,
            final String className, final String defaultLabel) {
        final Element htmlElem = copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, className));

        String label = elem.getChildText(CnxmlTag.LABEL.getTag(), CnxmlTag.NAMESPACE);
        if (label == null) {
            label = defaultLabel;
        }

        final Element htmlTitleElem = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS);
        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        final int number = elementNumberMap.get(elem);
        if (title != null) {
            htmlTitleElem.setText(label + " " + number + ": " + title);
        } else {
            htmlTitleElem.setText(label + " " + number);
        }
        htmlElem.addContent(htmlTitleElem);

        return htmlElem;
    }

    @SuppressWarnings("unchecked")
    private void generateDefinition(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_DEFINITION_CLASS));

        final int number = elementNumberMap.get(elem);
        String label = elem.getChildText(CnxmlTag.LABEL.getTag(), CnxmlTag.NAMESPACE);
        if (label == null) {
            label = DEFINITION_LABEL;
        }
        final Element htmlTitleElem = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS);
        htmlTitleElem.addContent(label + " " + number);
        htmlElem.addContent(htmlTitleElem);

        addHtmlContent(htmlElem);
        final List<Content> children = (List<Content>)elem.getContent(
                new ElementFilter(CnxmlTag.TERM.getTag(), CnxmlTag.NAMESPACE).negate());
        stack.push(new GeneratorFrame(children.iterator(), htmlElem, false));

        final Element term = elem.getChild(CnxmlTag.TERM.getTag(), CnxmlTag.NAMESPACE);
        if (term != null) {
            htmlTitleElem.addContent(": ");
            stack.push(new GeneratorFrame(term, htmlTitleElem));
        }
    }

    private void generateMeaning(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_MEANING_CLASS)));
    }

    private void generateExercise(final Element elem) {
        pushHtmlElement(elem, numberedContainer(elem, HTML_EXERCISE_CLASS, EXERCISE_LABEL));
    }

    private void generateCommentary(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_COMMENTARY_CLASS)));
    }

    private void generateProblem(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_PROBLEM_CLASS)));
    }

    private void generateSolution(final Element elem) {
        final Element htmlTitleElem = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
                .setText(SOLUTION_LABEL);
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_SOLUTION_CLASS))
                .addContent(htmlTitleElem));
    }

    private void generateEquation(final Element elem) {
        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        if (title != null) {
            addHtmlContent(new Element(HtmlTag.DIV.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_EQUATION_TITLE_CLASS)
                    .setText(title));
        }

        final Element htmlElem = copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_EQUATION_CLASS));
        addHtmlContent(htmlElem);

        final int number = elementNumberMap.get(elem);
        htmlElem.addContent(new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_EQUATION_NUMBER_CLASS)
                .setText(Integer.toString(number)));

        final Element htmlContentDiv = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_EQUATION_CONTENT_CLASS);
        htmlElem.addContent(0, htmlContentDiv);
        stack.push(new GeneratorFrame(elem, htmlContentDiv));
    }

    @SuppressWarnings("unchecked")
    private void generateFigure(final Element elem) {
        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        if (title != null) {
            addHtmlContent(new Element(HtmlTag.DIV.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_FIGURE_TITLE_CLASS)
                    .setText(title));
        }

        final Element htmlElem = copyId(elem, new Element(HtmlTag.FIGURE.getTag()));
        addHtmlContent(htmlElem);

        // Add caption to HTML figure element
        final Element htmlCaptionElem = new Element(HtmlTag.FIGURE_CAPTION.getTag());
        final Element htmlCaptionPrefixElem = new Element(HtmlTag.SPAN.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_PREFIX_CLASS);
        final String label = elem.getChildText(CnxmlTag.LABEL.getTag(), CnxmlTag.NAMESPACE);
        final int number = elementNumberMap.get(elem);
        htmlCaptionPrefixElem.addContent((label != null ? label : FIGURE_LABEL) + " " + number);
        htmlCaptionElem.addContent(htmlCaptionPrefixElem);

        // Push CNXML caption element to stack if it is present
        final Element captionElem =
                elem.getChild(CnxmlTag.FIGURE_CAPTION.getTag(), CnxmlTag.NAMESPACE);
        if (captionElem != null) {
            htmlCaptionPrefixElem.addContent(":");
            htmlCaptionElem.addContent(" ");
            stack.push(new GeneratorFrame(captionElem, htmlCaptionElem));
        }

        final List<Element> subfigures = (List<Element>)elem.getContent(
                new ElementFilter(CnxmlTag.SUBFIGURE.getTag(), CnxmlTag.NAMESPACE));
        if (subfigures.isEmpty()) {
            final Element tempSpan = new Element(HtmlTag.SPAN.getTag());
            htmlElem.addContent(0, tempSpan);
            stack.push(new GeneratorFrame(elem, tempSpan, true));
        } else {
            final Element htmlContainerElem = new Element(HtmlTag.DIV.getTag());
            htmlElem.addContent(0, htmlContainerElem);

            // Set class for orientation
            final CnxmlAttributes.FigureOrientation orient = CnxmlAttributes.FigureOrientation.of(
                    elem.getAttributeValue(CnxmlAttributes.FIGURE_ORIENT),
                    CnxmlAttributes.FigureOrientation.HORIZONTAL);
            switch (orient) {
            case VERTICAL:
                htmlContainerElem.setAttribute(HtmlAttributes.CLASS,
                        HTML_SUBFIGURE_CONTAINER_VERTICAL_CLASS);
                break;
            case HORIZONTAL:
                htmlContainerElem.setAttribute(HtmlAttributes.CLASS,
                        HTML_SUBFIGURE_CONTAINER_HORIZONTAL_CLASS);
                break;
            }

            // Add subfigures
            final ArrayList<GeneratorFrame> frames =
                    new ArrayList<GeneratorFrame>(subfigures.size());
            for (int i = 0; i < subfigures.size(); i++) {
                frames.add(generateSubfigure(i, subfigures.get(i), htmlContainerElem));
            }
            for (GeneratorFrame frame : Lists.reverse(frames)) {
                stack.push(frame);
            }
        }

        htmlElem.addContent(htmlCaptionElem);
    }

    /**
     *  Builds a prefix for the subfigure.
     *  <p>
     *  The sequence generated is:
     *  a, b, c, ... z, aa, ab, ac, ...
     */
    @VisibleForTesting static String getSubfigurePrefix(final int index) {
        // Our argument is a 0-based index, but we work with a 1-based number.
        checkArgument(index >= 0);
        int n = index + 1;

        final StringBuilder sb = new StringBuilder();
        while (n > 0) {
            final int mod = (n - 1) % SUBFIGURE_ALPHABET_SIZE;
            sb.insert(0, (char)(SUBFIGURE_ALPHABET_START + mod));
            n = (n - mod) / SUBFIGURE_ALPHABET_SIZE;
        }
        sb.insert(0, SUBFIGURE_COUNTER_PREFIX).append(SUBFIGURE_COUNTER_SUFFIX);
        return sb.toString();
    }

    private GeneratorFrame generateSubfigure(final int number, final Element elem,
            final Element htmlContainerElem) {
        final Element htmlElem = copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_SUBFIGURE_CLASS));
        final Element htmlContentElem = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_SUBFIGURE_CONTENT_CLASS);
        final Element htmlCaptionElem = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_SUBFIGURE_CAPTION_CLASS);

        htmlCaptionElem.addContent(new Element(HtmlTag.SPAN.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_PREFIX_CLASS)
                .setText(getSubfigurePrefix(number)));

        // Push CNXML caption element to stack if it is present
        final Element captionElem =
                elem.getChild(CnxmlTag.FIGURE_CAPTION.getTag(), CnxmlTag.NAMESPACE);
        if (captionElem != null) {
            htmlCaptionElem.addContent(" ");
            stack.push(new GeneratorFrame(captionElem, htmlCaptionElem));
        }

        htmlContainerElem.addContent(htmlElem);
        htmlElem.addContent(htmlContentElem).addContent(htmlCaptionElem);

        return new GeneratorFrame(elem, htmlContentElem);
    }

    private void generateRule(final Element elem) {
        final CnxmlAttributes.RuleType type = CnxmlAttributes.RuleType.of(
                elem.getAttributeValue(CnxmlAttributes.TYPE),
                CnxmlAttributes.RuleType.RULE);
        String defaultLabel;
        switch (type) {
        case THEOREM:
            defaultLabel = RULE_LABEL_THEOREM;
            break;
        case LEMMA:
            defaultLabel = RULE_LABEL_LEMMA;
            break;
        case COROLLARY:
            defaultLabel = RULE_LABEL_COROLLARY;
            break;
        case LAW:
            defaultLabel = RULE_LABEL_LAW;
            break;
        case PROPOSITION:
            defaultLabel = RULE_LABEL_PROPOSITION;
            break;
        case RULE:
        default:
            defaultLabel = RULE_LABEL_RULE;
            break;
        }
        pushHtmlElement(elem, numberedContainer(elem, HTML_RULE_CLASS, defaultLabel));
    }

    private void generateStatement(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_STATEMENT_CLASS)));
    }

    private void generateProof(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_PROOF_CLASS)
                    .addContent(new Element(HtmlTag.DIV.getTag())
                            .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
                            .setText(PROOF_LABEL))));
    }

    private void generateExample(final Element elem) {
        pushHtmlElement(elem, numberedContainer(elem, HTML_EXAMPLE_CLASS, EXAMPLE_LABEL));
    }

    private void generateList(final Element elem) {
        final CnxmlAttributes.Display display = CnxmlAttributes.Display.of(
                elem.getAttributeValue(CnxmlAttributes.DISPLAY),
                CnxmlAttributes.Display.BLOCK);
        Element htmlElem;

        final CnxmlAttributes.ListType type = CnxmlAttributes.ListType.of(
                elem.getAttributeValue(CnxmlAttributes.LIST_TYPE),
                CnxmlAttributes.ListType.BULLETED);

        if (display == CnxmlAttributes.Display.INLINE) {
            // TODO(light): handle inline lists
            unrecognized(elem, elem.getName()
                    + " " + CnxmlAttributes.DISPLAY + "=" + display.getValue());
            return;
        }

        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        if (title != null) {
            addHtmlContent(new Element(HtmlTag.DIV.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
                    .setText(title));
        }

        switch (type) {
        case BULLETED:
            htmlElem = copyId(elem, new Element(HtmlTag.UNORDERED_LIST.getTag()));

            final CnxmlAttributes.BulletStyle bulletStyle = CnxmlAttributes.BulletStyle.of(
                    elem.getAttributeValue(CnxmlAttributes.LIST_BULLET_STYLE),
                    CnxmlAttributes.BulletStyle.BULLET);
            switch (bulletStyle) {
            case NONE:
                htmlElem.setAttribute(HtmlAttributes.CLASS, HTML_BULLET_STYLE_NONE_CLASS);
                break;
            case BULLET:
                // Don't add a class for the normal case.
                break;
            default:
                log.warning("unsupported bullet style: " + bulletStyle.getValue());
                break;
            }
            break;
        case ENUMERATED:
            htmlElem = copyId(elem, new Element(HtmlTag.ORDERED_LIST.getTag()));
            final String numberStyle = elem.getAttributeValue(CnxmlAttributes.LIST_NUMBER_STYLE);
            if (numberStyle != null) {
                htmlElem.setAttribute(HtmlAttributes.LIST_NUMBER_STYLE, numberStyle);
            }
            final String startValue = elem.getAttributeValue(CnxmlAttributes.LIST_START_VALUE);
            if (startValue != null) {
                htmlElem.setAttribute(HtmlAttributes.LIST_START_VALUE, startValue);
            }
            break;
        case LABELED_ITEM:
            htmlElem = new Element(HtmlTag.UNORDERED_LIST.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_LABELED_ITEM_LIST_CLASS);
            copyId(elem, htmlElem);
            break;
        default:
            // TODO(light): gracefully handle other list types
            unrecognized(elem, elem.getName()
                    + " " + CnxmlAttributes.LIST_TYPE + "=" + type.getValue());
            return;
        }

        if (display == CnxmlAttributes.Display.NONE) {
            htmlElem.setAttribute(HtmlAttributes.STYLE, CSS_DISPLAY_NONE);
        }

        pushHtmlElement(elem, htmlElem);
    }

    private void generateListItem(final Element elem) {
        final Element listElem = (Element)elem.getParent();
        final Element htmlElem = copyId(elem, new Element(HtmlTag.LIST_ITEM.getTag()));
        final GeneratorFrame parentFrame = stack.peek();

        // Labeled item lists must copy the label element.
        final CnxmlAttributes.ListType type = CnxmlAttributes.ListType.of(
                listElem.getAttributeValue(CnxmlAttributes.LIST_TYPE),
                CnxmlAttributes.ListType.BULLETED);
        if (type == CnxmlAttributes.ListType.LABELED_ITEM) {
            final String label = elem.getChildText(CnxmlTag.LABEL.getTag(), CnxmlTag.NAMESPACE);
            if (label != null) {
                htmlElem.addContent(new Element(HtmlTag.SPAN.getTag())
                        .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
                        .setText(label));
            }
        }

        // If the list has an item separator and this is not the last item, then add the separator.
        if (parentFrame.iterator.hasNext()) {
            final String itemSep = listElem.getAttributeValue(CnxmlAttributes.LIST_ITEM_SEP);
            if (itemSep != null) {
                final Element tempSpan = new Element(HtmlTag.SPAN.getTag());
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

    private void generateNewline(final Element elem) {
        Element parent = stack.peek().htmlParent;
        final String id = elem.getAttributeValue(CnxmlAttributes.ID);
        if (id != null) {
            final Element anchor = new Element(HtmlTag.ANCHOR.getTag())
                    .setAttribute(HtmlAttributes.ID, id);
            parent.addContent(anchor);
            parent = anchor;
        }

        int count = 1;
        final String countAttr = elem.getAttributeValue(CnxmlAttributes.NEWLINE_COUNT);
        if (countAttr != null) {
            try {
                count = Integer.valueOf(countAttr);
            } catch (NumberFormatException e) {
                log.severe("count is not a number; CNXML was not validated");
                return;
            }
        }
        final CnxmlAttributes.NewlineEffect effect = CnxmlAttributes.NewlineEffect.of(
                elem.getAttributeValue(CnxmlAttributes.NEWLINE_EFFECT),
                CnxmlAttributes.NewlineEffect.NORMAL);
        switch (effect) {
        case UNDERLINE:
            for (int i = 0; i < count; i++) {
                parent.addContent(new Element(HtmlTag.HORIZONTAL_RULE.getTag()));
            }
            break;
        case NORMAL:
            for (int i = 0; i < count; i++) {
                parent.addContent(new Element(HtmlTag.LINE_BREAK.getTag()));
            }
            break;
        }
    }

    @SuppressWarnings("unchecked")
    private void generateMedia(final Element elem) {
        Element child = null;

        // Select child
        childLoop:
        for (Element e : (List<Element>)elem.getContent(mediaFilter)) {
            final CnxmlAttributes.MediaChildFor mediaFor = CnxmlAttributes.MediaChildFor.of(
                    e.getAttributeValue(CnxmlAttributes.MEDIA_CHILD_FOR),
                    CnxmlAttributes.MediaChildFor.DEFAULT);
            if (mediaFor == null) {
                continue;
            }

            switch (mediaFor) {
            case OVERRIDE:
                // XXX(light): A special for="webview2.0" attribute is used to force the media child
                // to be used.  This is necessary to provide backward compatibility with cnx.org and
                // still allow Mathematica to be embedded (the <object> element does not work as
                // documented on cnx.org).
                //
                // This hack should be removed when cnx.org is fixed or when cnx.org is migrated to
                // this renderer, whichever comes first.
                child = e;
                break childLoop;
            case PDF:
                break;
            case DEFAULT:
            case ONLINE:
                if (child == null) {
                    child = e;
                }
                break;
            }
        }

        // If no child is found, then don't render anything.
        if (child == null) {
            return;
        }

        switch (CnxmlTag.of(child.getName())) {
        case IMAGE:
            generateImage(child);
            break;
        case FLASH:
        case OBJECT:
            final String type = child.getAttributeValue(CnxmlAttributes.OBJECT_TYPE);
            if (CDF_MIME_TYPE.equals(type) || CDF_TEXT_MIME_TYPE.equals(type)) {
                generateMathematica(child);
            } else {
                generateObject(child);
            }
            break;
        case LABVIEW:
        case DOWNLOAD:
            generateDownloadLink(child);
            break;
        default:
            unrecognized(child);
            break;
        }
    }

    private void generateImage(final Element elem) {
        final Element mediaElem = (Element)elem.getParent();
        final Element htmlElem = copyId(mediaElem, new Element(HtmlTag.IMAGE.getTag()))
                .setAttribute(HtmlAttributes.IMAGE_ALT,
                        mediaElem.getAttributeValue(CnxmlAttributes.MEDIA_ALT));

        final String source = elem.getAttributeValue(CnxmlAttributes.MEDIA_CHILD_SOURCE);
        final String thumbnail = elem.getAttributeValue(CnxmlAttributes.IMAGE_THUMBNAIL);
        if (thumbnail != null) {
            htmlElem.setAttribute(HtmlAttributes.IMAGE_SOURCE, thumbnail);
            addHtmlContent(new Element(HtmlTag.LINK.getTag())
                    .setAttribute(HtmlAttributes.LINK_URL, source)
                    .addContent(htmlElem));
        } else {
            htmlElem.setAttribute(HtmlAttributes.IMAGE_SOURCE, source);
            addHtmlContent(htmlElem);
        }

        final String width = elem.getAttributeValue(CnxmlAttributes.IMAGE_WIDTH);
        if (width != null) {
            htmlElem.setAttribute(HtmlAttributes.IMAGE_WIDTH, width);
        }
        final String height = elem.getAttributeValue(CnxmlAttributes.IMAGE_HEIGHT);
        if (height != null) {
            htmlElem.setAttribute(HtmlAttributes.IMAGE_HEIGHT, height);
        }
    }

    private static String getDownloadLabel(final Element elem) {
        final Element mediaElem = (Element)elem.getParent();
        final String source = elem.getAttributeValue(CnxmlAttributes.MEDIA_CHILD_SOURCE);
        final String alt = mediaElem.getAttributeValue(CnxmlAttributes.MEDIA_ALT);
        final String originalSource = elem.getAttributeValue(ProcessorData.ORIGINAL_SOURCE_ATTR,
                ProcessorData.NAMESPACE);
        if (!alt.isEmpty()) {
            return HTML_DOWNLOAD_LABEL + alt;
        } else if (!Strings.isNullOrEmpty(originalSource)) {
            return HTML_DOWNLOAD_LABEL + originalSource;
        } else {
            return HTML_DOWNLOAD_LABEL + source;
        }
    }

    private void generateObject(final Element elem) {
        final Element mediaElem = (Element)elem.getParent();
        final Element htmlElem = copyId(mediaElem, new Element(HtmlTag.OBJECT.getTag()))
                .setAttribute(HtmlAttributes.OBJECT_SOURCE,
                        elem.getAttributeValue(CnxmlAttributes.MEDIA_CHILD_SOURCE))
                .setAttribute(HtmlAttributes.OBJECT_TYPE,
                        elem.getAttributeValue(CnxmlAttributes.OBJECT_TYPE))
                .setText(getDownloadLabel(elem));

        final String width = elem.getAttributeValue(CnxmlAttributes.OBJECT_WIDTH);
        if (width != null) {
            htmlElem.setAttribute(HtmlAttributes.OBJECT_WIDTH, width);
        }
        final String height = elem.getAttributeValue(CnxmlAttributes.OBJECT_HEIGHT);
        if (height != null) {
            htmlElem.setAttribute(HtmlAttributes.OBJECT_HEIGHT, height);
        }
        addHtmlContent(htmlElem);
    }

    private void generateDownloadLink(final Element elem) {
        final Element mediaElem = (Element)elem.getParent();
        addHtmlContent(copyId(mediaElem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_DOWNLOAD_CLASS)
                .addContent(new Element(HtmlTag.LINK.getTag())
                        .setAttribute(HtmlAttributes.LINK_URL,
                                elem.getAttributeValue(CnxmlAttributes.MEDIA_CHILD_SOURCE))
                        .setAttribute(HtmlAttributes.LINK_TYPE,
                                elem.getAttributeValue(CnxmlAttributes.DOWNLOAD_TYPE))
                        .setText(getDownloadLabel(elem)))));
    }

    private void generateMathematica(final Element elem) {
        final Element mediaElem = (Element)elem.getParent();
        final String type = elem.getAttributeValue(CnxmlAttributes.OBJECT_TYPE);
        final String source = elem.getAttributeValue(CnxmlAttributes.MEDIA_CHILD_SOURCE);
        final String width = elem.getAttributeValue(CnxmlAttributes.OBJECT_WIDTH);
        final String height = elem.getAttributeValue(CnxmlAttributes.OBJECT_HEIGHT);

        final Element htmlEmbedElem = new Element(HtmlTag.EMBED.getTag())
                .setAttribute(HtmlAttributes.EMBED_SOURCE, source)
                .setAttribute(HtmlAttributes.EMBED_TYPE, type);
        final Element htmlObjectElem = copyId(mediaElem, new Element(HtmlTag.OBJECT.getTag()))
                .setAttribute(HtmlAttributes.OBJECT_SOURCE, source)
                .setAttribute(HtmlAttributes.OBJECT_TYPE, type)
                .addContent(new Element(HtmlTag.PARAM.getTag())
                        .setAttribute(HtmlAttributes.PARAM_NAME, HtmlAttributes.PARAM_SOURCE)
                        .setAttribute(HtmlAttributes.PARAM_VALUE, source))
                .addContent(htmlEmbedElem);
        if (width != null) {
            htmlObjectElem.setAttribute(HtmlAttributes.OBJECT_WIDTH, width);
            htmlEmbedElem.setAttribute(HtmlAttributes.EMBED_WIDTH, width);
        }
        if (height != null) {
            htmlObjectElem.setAttribute(HtmlAttributes.OBJECT_HEIGHT, height);
            htmlEmbedElem.setAttribute(HtmlAttributes.EMBED_HEIGHT, height);
        }
        stack.peek().htmlParent
                .addContent(htmlObjectElem)
                .addContent(new Element(HtmlTag.DIV.getTag())
                        .setAttribute(HtmlAttributes.CLASS, HTML_CDF_DOWNLOAD_CLASS)
                        .addContent(new Element(HtmlTag.LINK.getTag())
                                .setAttribute(HtmlAttributes.LINK_URL, source)
                                .setText(getDownloadLabel(elem))));
    }

    // TODO(light): allow multiple tgroups
    private void generateTable(final Element elem) {
        final Element tgroup = elem.getChild(CnxmlTag.TABLE_GROUP.getTag(), CnxmlTag.NAMESPACE);
        final Element htmlElem = copyId(elem, new Element(HtmlTag.TABLE.getTag()));

        final List<String> classList = computeTableClasses(tgroup);
        classList.add(0, HTML_CALS_TABLE_CLASS);

        final CnxmlAttributes.CalsFrame frame = CnxmlAttributes.CalsFrame.of(
                elem.getAttributeValue(CnxmlAttributes.CALS_FRAME),
                CnxmlAttributes.CalsFrame.ALL);
        String frameClass;
        switch (frame) {
        case SIDES:
            frameClass = HTML_CALS_FRAME_SIDES_CLASS;
            break;
        case TOP:
            frameClass = HTML_CALS_FRAME_TOP_CLASS;
            break;
        case BOTTOM:
            frameClass = HTML_CALS_FRAME_BOTTOM_CLASS;
            break;
        case TOPBOTTOM:
            frameClass = HTML_CALS_FRAME_TOPBOTTOM_CLASS;
            break;
        case ALL:
        default:
            frameClass = HTML_CALS_FRAME_ALL_CLASS;
            break;
        }
        classList.add(1, frameClass);

        htmlElem.setAttribute(HtmlAttributes.CLASS, Joiner.on(' ').join(classList));

        addHtmlContent(htmlElem);

        // This must be done in reverse order (for the stack).
        pushTablePart(tgroup, CnxmlTag.TABLE_FOOT, htmlElem, HtmlTag.TABLE_FOOT);
        pushTablePart(tgroup, CnxmlTag.TABLE_BODY, htmlElem, HtmlTag.TABLE_BODY);
        pushTablePart(tgroup, CnxmlTag.TABLE_HEAD, htmlElem, HtmlTag.TABLE_HEAD);

        final Element htmlCaptionElem = new Element(HtmlTag.TABLE_CAPTION.getTag());
        final int number = elementNumberMap.get(elem);

        final Element htmlPrefixElem = new Element(HtmlTag.SPAN.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_PREFIX_CLASS)
                .addContent(CALS_TABLE_LABEL + " " + number);
        htmlCaptionElem.addContent(htmlPrefixElem);

        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        final String summary = elem.getAttributeValue(CnxmlAttributes.CALS_TABLE_SUMMARY);
        String captionText = null;
        if (!Strings.isNullOrEmpty(title)) {
            captionText = title;
        } else if (!Strings.isNullOrEmpty(summary)) {
            captionText = summary;
        }
        if (captionText != null) {
            htmlPrefixElem.addContent(":");
            htmlCaptionElem.addContent(" " + captionText);
        }

        htmlElem.addContent(htmlCaptionElem);
    }

    private void pushTablePart(final Element tableGroupElem, final CnxmlTag cnxmlTag,
            final Element htmlTableElem, final HtmlTag htmlTag) {
        final Element elem = tableGroupElem.getChild(cnxmlTag.getTag(), CnxmlTag.NAMESPACE);
        if (elem == null) {
            return;
        }

        final Element htmlElem = new Element(htmlTag.getTag());
        final List<String> classList = computeTableClasses(elem);
        if (!classList.isEmpty()) {
            htmlElem.setAttribute(HtmlAttributes.CLASS, Joiner.on(' ').join(classList));
        }
        htmlTableElem.addContent(0, htmlElem);
        stack.push(new GeneratorFrame(elem, htmlElem));
    }

    private void generateTableRow(final Element elem) {
        final Element htmlElem = new Element(HtmlTag.TABLE_ROW.getTag());
        final List<String> classList = computeTableClasses(elem);
        if (!classList.isEmpty()) {
            htmlElem.setAttribute(HtmlAttributes.CLASS, Joiner.on(' ').join(classList));
        }
        pushHtmlElement(elem, htmlElem);
    }

    private void generateTableCell(final Element elem) {
        final String grandparentName = ((Element)elem.getParent().getParent()).getName();
        final String htmlName = CnxmlTag.TABLE_HEAD.getTag().equals(grandparentName)
                ? HtmlTag.TABLE_HEAD_CELL.getTag() : HtmlTag.TABLE_CELL.getTag();
        final Element htmlElem = new Element(htmlName);

        final List<String> classList = computeTableClasses(elem);
        if (!classList.isEmpty()) {
            htmlElem.setAttribute(HtmlAttributes.CLASS, Joiner.on(' ').join(classList));
        }
        pushHtmlElement(elem, htmlElem);
    }

    /**
     *  Determine the HTML classes to be applied for a CALS table element.
     */
    private List<String> computeTableClasses(final Element elem) {
        final ArrayList<String> classList = new ArrayList<String>(4);
        final CnxmlAttributes.CalsAlign align = CnxmlAttributes.CalsAlign.of(
                elem.getAttributeValue(CnxmlAttributes.CALS_ALIGN));
        final CnxmlAttributes.CalsVerticalAlign valign = CnxmlAttributes.CalsVerticalAlign.of(
                elem.getAttributeValue(CnxmlAttributes.CALS_VALIGN));
        final String colsepAttr = elem.getAttributeValue(CnxmlAttributes.CALS_COLSEP);
        final String rowsepAttr = elem.getAttributeValue(CnxmlAttributes.CALS_ROWSEP);

        if (align != null) {
            switch (align) {
            case LEFT:
                classList.add(HTML_CALS_ALIGN_LEFT_CLASS);
                break;
            case RIGHT:
                classList.add(HTML_CALS_ALIGN_RIGHT_CLASS);
                break;
            case CENTER:
                classList.add(HTML_CALS_ALIGN_CENTER_CLASS);
                break;
            case JUSTIFY:
                classList.add(HTML_CALS_ALIGN_JUSTIFY_CLASS);
                break;
            }
        }

        if (valign != null) {
            switch (valign) {
            case TOP:
                classList.add(HTML_CALS_VALIGN_TOP_CLASS);
                break;
            case MIDDLE:
                classList.add(HTML_CALS_VALIGN_MIDDLE_CLASS);
                break;
            case BOTTOM:
                classList.add(HTML_CALS_VALIGN_BOTTOM_CLASS);
                break;
            }
        }

        if (colsepAttr != null) {
            if (CnxmlAttributes.CALS_NOSEP.equals(colsepAttr)) {
                classList.add(HTML_CALS_NO_COLSEP_CLASS);
            } else {
                classList.add(HTML_CALS_COLSEP_CLASS);
            }
        }

        if (rowsepAttr != null) {
            if (CnxmlAttributes.CALS_NOSEP.equals(rowsepAttr)) {
                classList.add(HTML_CALS_NO_ROWSEP_CLASS);
            } else {
                classList.add(HTML_CALS_ROWSEP_CLASS);
            }
        }

        return classList;
    }
}
