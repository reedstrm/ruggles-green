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
import java.util.logging.Logger;

import org.cnx.util.RenderTime;
import org.cnx.util.HtmlTag;
import org.cnx.util.HtmlAttributes;
import org.cnx.util.IdFilter;
import org.cnx.util.JdomHtmlSerializer;

import org.jdom.Attribute;
import org.jdom.Comment;
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
    private final static String HTML_LABELED_ITEM_LIST_CLASS = "labeled";

    private final static String CDF_MIME_TYPE = "application/vnd.wolfram.cdf";
    private final static String CDF_TEXT_MIME_TYPE = "application/vnd.wolfram.cdf.text";
    private final static String HTML_CDF_DOWNLOAD_LABEL = "Download CDF";

    private final static String HTML_DOWNLOAD_LABEL = "Download ";

    private final static String HTML_DEFAULT_LINK_TEXT = "link";
    private final static String HTML_DEFAULT_FIGURE_LINK_TEXT = "figure";

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

    private final static Logger log = Logger.getLogger(JdomHtmlGenerator.class.getName());

    private final ImmutableSet<Processor> processors;
    private final JdomHtmlSerializer jdomHtmlSerializer;
    private Module module;
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

    /**
     *  MediaElementFilter only yields elements that are media children.
     *  <p>
     *  Examples of elements yielded are image, flash, and object.
     */
    private static class MediaElementFilter implements Filter {
        @Override public boolean matches(Object obj) {
            if (obj instanceof Element) {
                final Element elem = (Element)obj;
                return CnxmlTag.NAMESPACE_URI.equals(elem.getNamespaceURI())
                        && CnxmlTag.MEDIA_CHILDREN.contains(CnxmlTag.of(elem.getName()));
            }
            return false;
        }
    }

    @Inject public JdomHtmlGenerator(Set<Processor> processors,
            JdomHtmlSerializer jdomHtmlSerializer) {
        this.processors = ImmutableSet.copyOf(processors);
        this.jdomHtmlSerializer = jdomHtmlSerializer;
        this.mediaFilter = new MediaElementFilter();
    }

    @Override public String generate(Module module) throws Exception {
        long startTime, endTime;

        // Apply processors
        startTime = System.currentTimeMillis();

        for (Processor processor : processors) {
            module = processor.process(module);
        }

        endTime = System.currentTimeMillis();
        log.fine("Processors took " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();

        // Convert to JDOM
        final Element contentElem = module.getCnxml().getRootElement()
                .getChild(CnxmlTag.CONTENT.getTag(), CnxmlTag.NAMESPACE);
        if (contentElem == null) {
            return "";
        }

        // Generate HTML tree
        this.module = module;
        counter = new Counter();
        StringBuilder sb = new StringBuilder();
        final List<Content> contentList = generateHtmlTree(contentElem);
        counter = null;
        module = null;

        // Serialize HTML
        for (Content content : contentList) {
            jdomHtmlSerializer.serialize(sb, content);
        }

        endTime = System.currentTimeMillis();
        log.fine("Rendered in " + (endTime - startTime) + " ms");

        return sb.toString();
    }

    /**
     *  This method creates an intermediary HTML JDOM tree that represents the content.
     *
     *  @param contentRoot The content element from the CNXML
     */
    @SuppressWarnings("unchecked")
    protected List<Content> generateHtmlTree(Element contentRoot) {
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

    protected void generateElement(final Element elem) {
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
            case SUBFIGURE:
                generateSubfigure(elem);
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
        } else if (MATHML_NAMESPACE.equals(elem.getNamespaceURI())) {
            // Copy math without modification.  If we add it directly, it detaches from the original
            // CNXML tree.
            // TODO(light): Don't use clone, it's recursive.
            addHtmlContent((Element)elem.clone());
        }
    }
    
    protected void unrecognized(final Element elem) {
        unrecognized(elem, elem.getName());
    }

    /** Display a placeholder for an unrecognized element. */
    @SuppressWarnings("unchecked")
    protected void unrecognized(final Element elem, final String message) {
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
    protected void addHtmlContent(final Content content) {
        stack.peek().htmlParent.addContent(content);
    }

    /**
     *  This method adds the HTML element to the top parent then pushes the HTML element onto the
     *  stack, using the given element for children.
     */
    protected void pushHtmlElement(final Element elem, final Element htmlElem) {
        addHtmlContent(htmlElem);
        stack.push(new GeneratorFrame(elem, htmlElem));
    }

    protected static Element copyId(final Element elem, final Element htmlElem) {
        final String id = elem.getAttributeValue(CnxmlAttributes.ID);
        if (id != null) {
            htmlElem.setAttribute(HtmlAttributes.ID, id);
        }
        return htmlElem;
    }

    protected void generateParagraph(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HtmlTag.PARAGRAPH.getTag()));
        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        if (title != null) {
            addHtmlContent(new Element(HtmlTag.DIV.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
                    .setText(title));
        }
        pushHtmlElement(elem, htmlElem);
    }

    protected void generateSection(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HtmlTag.SECTION.getTag()));
        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        if (title != null) {
            htmlElem.addContent(new Element(HtmlTag.HEADING.getTag()).setText(title));
        }
        pushHtmlElement(elem, htmlElem);
    }

    protected void generateEmphasis(final Element elem) {
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

    protected void generateLink(final Element elem) {
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
            if (url != null && url.startsWith(CnxmlAttributes.FRAGMENT)) {
                ref = url.substring(CnxmlAttributes.FRAGMENT.length());
            } else if (targetId != null) {
                ref = targetId;
            }
            if (ref != null) {
                final Element refElem = IdFilter.getElementById(module.getCnxml(), ref);
                if (refElem != null && CnxmlTag.NAMESPACE_URI.equals(refElem.getNamespaceURI())
                        && CnxmlTag.FIGURE.getTag().equals(refElem.getName())) {
                    // TODO(light): Add numbers
                    text = HTML_DEFAULT_FIGURE_LINK_TEXT;
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

    protected void generateSup(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.SUP.getTag())));
    }

    protected void generateSub(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.SUB.getTag())));
    }

    protected void generatePreformat(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.PREFORMAT.getTag())));
    }

    protected void generateCode(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HtmlTag.CODE.getTag()));
        final CnxmlAttributes.Display display = CnxmlAttributes.Display.of(
                elem.getAttributeValue(CnxmlAttributes.DISPLAY),
                CnxmlAttributes.Display.INLINE);

        switch (display) {
        case NONE:
            pushHtmlElement(elem, htmlElem.setAttribute(HtmlAttributes.STYLE, CSS_DISPLAY_NONE));
            break;
        case BLOCK:
            addHtmlContent(
                    new Element(HtmlTag.PREFORMAT.getTag()).addContent(htmlElem));
            stack.push(new GeneratorFrame(elem, htmlElem));
            break;
        case INLINE:
            pushHtmlElement(elem, htmlElem);
        }
    }

    protected void generateNote(final Element elem) {
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

    protected int getNumber(final Element elem) {
        final String type = elem.getAttributeValue(CnxmlAttributes.TYPE);
        return counter.getNextNumber(elem.getName(), (type != null ? type : ""));
    }

    protected Element numberedContainer(final Element elem,
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
        final int number = getNumber(elem);
        if (title != null) {
            htmlTitleElem.setText(label + " " + number + ": " + title);
        } else {
            htmlTitleElem.setText(label + " " + number);
        }
        htmlElem.addContent(htmlTitleElem);

        return htmlElem;
    }

    @SuppressWarnings("unchecked")
    protected void generateDefinition(final Element elem) {
        final Element htmlElem = copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_DEFINITION_CLASS));

        final Element term = elem.getChild(CnxmlTag.TERM.getTag(), CnxmlTag.NAMESPACE);
        final int number = getNumber(elem);
        String label = elem.getChildText(CnxmlTag.LABEL.getTag(), CnxmlTag.NAMESPACE);
        if (label == null) {
            label = DEFINITION_LABEL;
        }
        final Element htmlTitleElem = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS);
        htmlTitleElem.addContent(label + " " + number);
        // TODO(light): This doesn't handle links.
        if (term != null) {
            htmlTitleElem.addContent(": " + term.getText());
        }

        // TODO(light): Allow interspersed examples with meanings
        final Element htmlMeaningList = new Element(HtmlTag.ORDERED_LIST.getTag());

        addHtmlContent(htmlElem);
        final List<Content> children = (List<Content>)elem.getContent(new Filter() {
            @Override public boolean matches(Object obj) {
                if (obj instanceof Element) {
                    final Element elem = (Element)obj;
                    return !(CnxmlTag.NAMESPACE.getURI().equals(elem.getNamespaceURI())
                            && CnxmlTag.TERM.getTag().equals(elem.getName()));
                }
                return true;
            }
        });
        stack.push(new GeneratorFrame(children.iterator(), htmlMeaningList, false));
        htmlElem.addContent(htmlTitleElem).addContent(htmlMeaningList);
    }

    protected void generateMeaning(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.LIST_ITEM.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_MEANING_CLASS)));
    }

    protected void generateExercise(final Element elem) {
        pushHtmlElement(elem, numberedContainer(elem, HTML_EXERCISE_CLASS, EXERCISE_LABEL));
    }

    protected void generateCommentary(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_COMMENTARY_CLASS)));
    }

    protected void generateProblem(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_PROBLEM_CLASS)));
    }

    protected void generateSolution(final Element elem) {
        final Element htmlTitleElem = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
                .setText(SOLUTION_LABEL);
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_SOLUTION_CLASS))
                .addContent(htmlTitleElem));
    }

    protected void generateEquation(final Element elem) {
        pushHtmlElement(elem, numberedContainer(elem, HTML_EQUATION_CLASS, EQUATION_LABEL));
    }

    protected void generateFigure(final Element elem) {
        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        if (title != null) {
            addHtmlContent(new Element(HtmlTag.DIV.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
                    .setText(title));
        }

        final Element htmlElem = copyId(elem, new Element(HtmlTag.FIGURE.getTag()));
        if (elem.getChild(CnxmlTag.SUBFIGURE.getTag(), CnxmlTag.NAMESPACE) != null) {
            final CnxmlAttributes.FigureOrientation orient = CnxmlAttributes.FigureOrientation.of(
                    elem.getAttributeValue(CnxmlAttributes.FIGURE_ORIENT),
                    CnxmlAttributes.FigureOrientation.HORIZONTAL);
            switch (orient) {
            case VERTICAL:
                htmlElem.setAttribute(HtmlAttributes.CLASS, HTML_FIGURE_VERTICAL_CLASS);
                break;
            case HORIZONTAL:
                htmlElem.setAttribute(HtmlAttributes.CLASS, HTML_FIGURE_HORIZONTAL_CLASS);
                break;
            }
        }

        // Add HTML figure element to parent
        final Element tempSpan = new Element(HtmlTag.SPAN.getTag());
        addHtmlContent(htmlElem.addContent(tempSpan));

        // Add caption to HTML figure element
        final Element htmlCaptionElem = new Element(HtmlTag.FIGURE_CAPTION.getTag());
        final Element htmlCaptionTitleElem = new Element(HtmlTag.SPAN.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS);
        final String label = elem.getChildText(CnxmlTag.LABEL.getTag(), CnxmlTag.NAMESPACE);
        final Element captionElem =
                elem.getChild(CnxmlTag.FIGURE_CAPTION.getTag(), CnxmlTag.NAMESPACE);
        final int number = getNumber(elem);
        htmlCaptionTitleElem.addContent((label != null ? label : FIGURE_LABEL) + " " + number);
        htmlCaptionElem.addContent(htmlCaptionTitleElem);
        htmlElem.addContent(htmlCaptionElem);

        // Push CNXML caption element to stack if it is present
        if (captionElem != null) {
            htmlCaptionTitleElem.addContent(":");
            htmlCaptionElem.addContent(" ");
            stack.push(new GeneratorFrame(captionElem, htmlCaptionElem));
        }

        // Push content to stack for iteration
        stack.push(new GeneratorFrame(elem, tempSpan, true));
    }

    protected void generateSubfigure(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_SUBFIGURE_CLASS)));
    }

    protected void generateRule(final Element elem) {
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

    protected void generateStatement(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_STATEMENT_CLASS)));
    }

    protected void generateProof(final Element elem) {
        pushHtmlElement(elem, copyId(elem, new Element(HtmlTag.DIV.getTag())
                    .setAttribute(HtmlAttributes.CLASS, HTML_PROOF_CLASS)
                    .addContent(new Element(HtmlTag.DIV.getTag())
                            .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
                            .setText(PROOF_LABEL))));
    }

    protected void generateExample(final Element elem) {
        pushHtmlElement(elem, numberedContainer(elem, HTML_EXAMPLE_CLASS, EXAMPLE_LABEL));
    }

    protected void generateList(final Element elem) {
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

    protected void generateListItem(final Element elem) {
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

    protected void generateNewline(final Element elem) {
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
    protected void generateMedia(final Element elem) {
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

    protected void generateImage(final Element elem) {
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

    protected void generateObject(final Element elem) {
        final Element mediaElem = (Element)elem.getParent();
        final Element htmlElem = copyId(mediaElem, new Element(HtmlTag.OBJECT.getTag()))
                .setAttribute(HtmlAttributes.OBJECT_SOURCE,
                        elem.getAttributeValue(CnxmlAttributes.MEDIA_CHILD_SOURCE))
                .setAttribute(HtmlAttributes.OBJECT_TYPE,
                        elem.getAttributeValue(CnxmlAttributes.OBJECT_TYPE));
        final String width = elem.getAttributeValue(CnxmlAttributes.OBJECT_WIDTH);
        if (width != null) {
            htmlElem.setAttribute(HtmlAttributes.OBJECT_WIDTH, width);
        }
        final String height = elem.getAttributeValue(CnxmlAttributes.OBJECT_HEIGHT);
        if (height != null) {
            htmlElem.setAttribute(HtmlAttributes.OBJECT_HEIGHT, height);
        }
        htmlElem.setText(mediaElem.getAttributeValue(CnxmlAttributes.MEDIA_ALT));
        addHtmlContent(htmlElem);
    }

    protected void generateDownloadLink(final Element elem) {
        final Element mediaElem = (Element)elem.getParent();
        final Element htmlElem = copyId(mediaElem, new Element(HtmlTag.LINK.getTag()))
                .setAttribute(HtmlAttributes.LINK_URL,
                        elem.getAttributeValue(CnxmlAttributes.MEDIA_CHILD_SOURCE))
                .setAttribute(HtmlAttributes.LINK_TYPE,
                        elem.getAttributeValue(CnxmlAttributes.DOWNLOAD_TYPE));
        htmlElem.setText(HTML_DOWNLOAD_LABEL
                + mediaElem.getAttributeValue(CnxmlAttributes.MEDIA_ALT));
        addHtmlContent(htmlElem);
    }

    protected void generateMathematica(final Element elem) {
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
                                .setText(HTML_CDF_DOWNLOAD_LABEL)));
    }

    // TODO(light): allow multiple tgroups
    protected void generateTable(final Element elem) {
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
        final String title = elem.getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
        final String summary = elem.getAttributeValue(CnxmlAttributes.CALS_TABLE_SUMMARY);
        final int number = getNumber(elem);

        final Element htmlTitleElem = new Element(HtmlTag.DIV.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
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

    protected void pushTablePart(final Element tableGroupElem, final CnxmlTag cnxmlTag,
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

    protected void generateTableRow(final Element elem) {
        final Element htmlElem = new Element(HtmlTag.TABLE_ROW.getTag());
        final List<String> classList = computeTableClasses(elem);
        if (!classList.isEmpty()) {
            htmlElem.setAttribute(HtmlAttributes.CLASS, Joiner.on(' ').join(classList));
        }
        pushHtmlElement(elem, htmlElem);
    }

    protected void generateTableCell(final Element elem) {
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
    protected List<String> computeTableClasses(final Element elem) {
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
