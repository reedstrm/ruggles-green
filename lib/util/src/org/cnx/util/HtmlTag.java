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

package org.cnx.util;

import com.google.common.base.Objects;

import javax.annotation.Nullable;

/**
 *  HtmlTag holds constants for all HTML elements.
 */
public enum HtmlTag {
    INVALID(null, false),

    ANCHOR("a", false),
    AREA("area", true),
    BASE("base", true),
    CODE("code", false),
    COLUMN("col", true),
    COMMAND("command", true),
    DIV("div", false),
    DOCUMENT_LINK("link", true),
    EMBED("embed", true),
    EMPHASIS("em", false),
    FIGURE("figure", false),
    FIGURE_CAPTION("figcaption", false),
    HEADING("h1", false),
    HORIZONTAL_RULE("hr", true),
    IMAGE("img", true),
    INPUT("input", true),
    KEYGEN("keygen", true),
    LINE_BREAK("br", true),
    LINK("a", false),
    LIST_ITEM("li", false),
    META("meta", true),
    OBJECT("object", false),
    ORDERED_LIST("ol", false),
    PARAGRAPH("p", false),
    PARAM("param", true),
    PREFORMAT("pre", false),
    SECTION("section", false),
    SOURCE("source", true),
    SPAN("span", false),
    STRONG("strong", false),
    SUB("sub", false),
    SUP("sup", false),
    TABLE("table", false),
    TABLE_BODY("tbody", false),
    TABLE_CAPTION("caption", false),
    TABLE_CELL("td", false),
    TABLE_FOOT("tfoot", false),
    TABLE_HEAD("thead", false),
    TABLE_HEAD_CELL("th", false),
    TABLE_ROW("tr", false),
    TRACK("track", true),
    UNDERLINE("u", false),
    UNORDERED_LIST("ul", false),
    WORD_BREAK("wbr", true);

    private final String tag;
    private final boolean voidTag;

    private HtmlTag(@Nullable String tag, boolean voidTag) {
        this.tag = tag;
        this.voidTag = voidTag;
    }

    public String getTag() {
        return tag;
    }

    /** If this tag is a void tag, then it does not render with a closing tag. */
    public boolean isVoidTag() {
        return voidTag;
    }

    public static HtmlTag of(@Nullable final String name) {
        return HtmlTag.of(name, null);
    }

    public static HtmlTag of(@Nullable final String name, @Nullable final HtmlTag defaultTag) {
        if (name == null) {
            return defaultTag;
        }
        for (HtmlTag tag : HtmlTag.values()) {
            if (Objects.equal(tag.tag, name)) {
                return tag;
            }
        }
        return defaultTag;
    }
}
