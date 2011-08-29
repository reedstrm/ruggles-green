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

import com.google.common.collect.ImmutableSet;
import com.google.common.base.Objects;

import javax.annotation.Nullable;

/**
 *  CnxmlTag holds constants for all CNXML elements.
 */
public enum CnxmlTag {
    INVALID(null),

    CONTENT("content"),
    TITLE("title"),
    LABEL("label"),
    PARAGRAPH("para"),
    SECTION("section"),
    EMPHASIS("emphasis"),
    LINK("link"),
    FOREIGN("foreign"),
    TERM("term"),
    SUP("sup"),
    SUB("sub"),
    PREFORMAT("preformat"),
    CODE("code"),
    NOTE("note"),
    DEFINITION("definition"),
    MEANING("meaning"),
    EXERCISE("exercise"),
    COMMENTARY("commentary"),
    PROBLEM("problem"),
    SOLUTION("solution"),
    EQUATION("equation"),
    RULE("rule"),
    STATEMENT("statement"),
    PROOF("proof"),
    EXAMPLE("example"),
    LIST("list"),
    LIST_ITEM("item"),
    NEWLINE("newline"),
    MEDIA("media"),
    FLASH("flash"),
    IMAGE("image"),
    OBJECT("object"),
    LABVIEW("labview"),
    FIGURE("figure"),
    FIGURE_CAPTION("caption"),
    SUBFIGURE("subfigure"),
    TABLE("table"),
    TABLE_GROUP("tgroup"),
    TABLE_ROW("row"),
    TABLE_CELL("entry"),
    TABLE_HEAD("thead"),
    TABLE_BODY("tbody"),
    TABLE_FOOT("tfoot");

    private final String tag;

    private CnxmlTag(@Nullable String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static CnxmlTag of(@Nullable final String name) {
        return CnxmlTag.of(name, INVALID);
    }

    public static CnxmlTag of(@Nullable final String name, @Nullable final CnxmlTag defaultTag) {
        if (name == null) {
            return defaultTag;
        }
        for (CnxmlTag tag : CnxmlTag.values()) {
            if (Objects.equal(tag.tag, name)) {
                return tag;
            }
        }
        return defaultTag;
    }

    public final static ImmutableSet<CnxmlTag> MEDIA_CHILDREN = ImmutableSet.of(
            FLASH,
            IMAGE,
            OBJECT,
            LABVIEW);
}
