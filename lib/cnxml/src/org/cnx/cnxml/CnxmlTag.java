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
import org.jdom.Namespace;

/**
 *  CnxmlTag holds constants for all CNXML elements.
 */
public enum CnxmlTag {
    INVALID(null),

    APPLET("java-applet"),
    AUDIO("audio"),
    CODE("code"),
    COMMENTARY("commentary"),
    CONTENT("content"),
    DEFINITION("definition"),
    DOWNLOAD("download"),
    EMPHASIS("emphasis"),
    EQUATION("equation"),
    EXAMPLE("example"),
    EXERCISE("exercise"),
    FIGURE("figure"),
    FIGURE_CAPTION("caption"),
    FLASH("flash"),
    FOREIGN("foreign"),
    IMAGE("image"),
    LABEL("label"),
    LABVIEW("labview"),
    LINK("link"),
    LIST("list"),
    LIST_ITEM("item"),
    MEANING("meaning"),
    MEDIA("media"),
    METADATA("metadata"),
    NEWLINE("newline"),
    NOTE("note"),
    OBJECT("object"),
    PARAGRAPH("para"),
    PREFORMAT("preformat"),
    PROBLEM("problem"),
    PROOF("proof"),
    RULE("rule"),
    SECTION("section"),
    SOLUTION("solution"),
    STATEMENT("statement"),
    SUB("sub"),
    SUBFIGURE("subfigure"),
    SUP("sup"),
    TABLE("table"),
    TABLE_BODY("tbody"),
    TABLE_CELL("entry"),
    TABLE_FOOT("tfoot"),
    TABLE_GROUP("tgroup"),
    TABLE_HEAD("thead"),
    TABLE_ROW("row"),
    TERM("term"),
    TITLE("title"),
    VIDEO("video");

    private final String tag;

    public static final String NAMESPACE_URI = "http://cnx.rice.edu/cnxml";
    public static final Namespace NAMESPACE = Namespace.getNamespace(NAMESPACE_URI);

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
            APPLET,
            AUDIO,
            DOWNLOAD,
            FLASH,
            IMAGE,
            LABVIEW,
            OBJECT,
            VIDEO);
}
