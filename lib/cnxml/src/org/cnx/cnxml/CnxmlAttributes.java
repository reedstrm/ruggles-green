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

import javax.annotation.Nullable;

import org.cnx.util.ValueEnum;
import org.cnx.util.ValueEnums;

/**
 *  CnxmlAttributes holds constants for all CNXML attributes.
 */
public class CnxmlAttributes {
    public final static String ID = "id";
    public final static String TYPE = "type";
    public final static String LINK_URL = "url";
    public final static String LINK_TARGET_ID = "target-id";
    public final static String LINK_DOCUMENT = "document";
    public final static String LINK_RESOURCE = "resource";
    public final static String LINK_VERSION = "version";
    public final static String EFFECT = "effect";
    public final static String DISPLAY = "display";
    public final static String LIST_ITEM_SEP = "item-sep";
    public final static String LIST_TYPE = "list-type";
    public final static String LIST_NUMBER_STYLE = "number-style";
    public final static String LIST_BULLET_STYLE = "bullet-style";
    public final static String LIST_START_VALUE = "start-value";
    public final static String NEWLINE_COUNT = "count";
    public final static String NEWLINE_EFFECT = "effect";
    public final static String MEDIA_ALT = "alt";
    public final static String MEDIA_CHILD_SOURCE = "src";
    public final static String IMAGE_THUMBNAIL = "thumbnail";
    public final static String IMAGE_WIDTH = "width";
    public final static String IMAGE_HEIGHT = "height";
    public final static String OBJECT_TYPE = "mime-type";
    public final static String OBJECT_WIDTH = "width";
    public final static String OBJECT_HEIGHT = "height";
    public final static String DOWNLOAD_TYPE = "mime-type";
    public final static String MEDIA_CHILD_FOR = "for";
    public final static String FIGURE_ORIENT = "orient";
    public final static String CALS_TABLE_SUMMARY = "summary";
    public final static String CALS_ALIGN = "align";
    public final static String CALS_VALIGN = "valign";
    public final static String CALS_COLSEP = "colsep";
    public final static String CALS_ROWSEP = "rowsep";
    public final static String CALS_FRAME = "frame";

    public static enum EmphasisEffect implements ValueEnum {
        BOLD("bold"),
        ITALICS("italics"),
        UNDERLINE("underline"),
        SMALLCAPS("smallcaps"),
        NORMAL("normal");

        private String value;

        private EmphasisEffect(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static EmphasisEffect of(@Nullable final String value) {
            return ValueEnums.findEnum(EmphasisEffect.class, value, null);
        }

        public static EmphasisEffect of(@Nullable final String value,
                @Nullable final EmphasisEffect defaultValue) {
            return ValueEnums.findEnum(EmphasisEffect.class, value, defaultValue);
        }
    }

    public static enum Display implements ValueEnum {
        NONE("none"),
        BLOCK("block"),
        INLINE("inline");

        private String value;

        private Display(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Display of(@Nullable final String value) {
            return ValueEnums.findEnum(Display.class, value, null);
        }

        public static Display of(@Nullable final String value,
                @Nullable final Display defaultValue) {
            return ValueEnums.findEnum(Display.class, value, defaultValue);
        }
    }

    public static enum NoteType implements ValueEnum {
        ASIDE("aside"),
        WARNING("warning"),
        TIP("tip"),
        IMPORTANT("important"),
        NOTE("note");

        private String value;

        private NoteType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static NoteType of(@Nullable final String value) {
            return ValueEnums.findEnum(NoteType.class, value, null);
        }

        public static NoteType of(@Nullable final String value,
                @Nullable final NoteType defaultValue) {
            return ValueEnums.findEnum(NoteType.class, value, defaultValue);
        }
    }

    public static enum RuleType implements ValueEnum {
        THEOREM("theorem"),
        LEMMA("lemma"),
        COROLLARY("corollary"),
        LAW("law"),
        PROPOSITION("proposition"),
        RULE("rule");

        private String value;

        private RuleType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static RuleType of(@Nullable final String value) {
            return ValueEnums.findEnum(RuleType.class, value, null);
        }

        public static RuleType of(@Nullable final String value,
                @Nullable final RuleType defaultValue) {
            return ValueEnums.findEnum(RuleType.class, value, defaultValue);
        }
    }

    public static enum ListType implements ValueEnum {
        BULLETED("bulleted"),
        ENUMERATED("enumerated"),
        LABELED_ITEM("labeled-item");

        private String value;

        private ListType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ListType of(@Nullable final String value) {
            return ValueEnums.findEnum(ListType.class, value, null);
        }

        public static ListType of(@Nullable final String value,
                @Nullable final ListType defaultValue) {
            return ValueEnums.findEnum(ListType.class, value, defaultValue);
        }
    }

    public static enum NewlineEffect implements ValueEnum {
        NORMAL("normal"),
        UNDERLINE("underline");

        private String value;

        private NewlineEffect(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static NewlineEffect of(@Nullable final String value) {
            return ValueEnums.findEnum(NewlineEffect.class, value, null);
        }

        public static NewlineEffect of(@Nullable final String value,
                @Nullable final NewlineEffect defaultValue) {
            return ValueEnums.findEnum(NewlineEffect.class, value, defaultValue);
        }
    }

    public static enum MediaChildFor implements ValueEnum {
        DEFAULT("default"),
        ONLINE("online"),
        PDF("pdf"),
        OVERRIDE("webview2.0");

        private String value;

        private MediaChildFor(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static MediaChildFor of(@Nullable final String value) {
            return ValueEnums.findEnum(MediaChildFor.class, value, null);
        }

        public static MediaChildFor of(@Nullable final String value,
                @Nullable final MediaChildFor defaultValue) {
            return ValueEnums.findEnum(MediaChildFor.class, value, defaultValue);
        }
    }

    public static enum FigureOrientation implements ValueEnum {
        HORIZONTAL("horizontal"),
        VERTICAL("vertical");

        private String value;

        private FigureOrientation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static FigureOrientation of(@Nullable final String value) {
            return ValueEnums.findEnum(FigureOrientation.class, value, null);
        }

        public static FigureOrientation of(@Nullable final String value,
                @Nullable final FigureOrientation defaultValue) {
            return ValueEnums.findEnum(FigureOrientation.class, value, defaultValue);
        }
    }

    public static enum CalsAlign implements ValueEnum {
        LEFT("left"),
        RIGHT("right"),
        CENTER("center"),
        JUSTIFY("justify");

        private String value;

        private CalsAlign(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static CalsAlign of(@Nullable final String value) {
            return ValueEnums.findEnum(CalsAlign.class, value, null);
        }

        public static CalsAlign of(@Nullable final String value,
                @Nullable final CalsAlign defaultValue) {
            return ValueEnums.findEnum(CalsAlign.class, value, defaultValue);
        }
    }

    public static enum CalsVerticalAlign implements ValueEnum {
        TOP("top"),
        MIDDLE("middle"),
        BOTTOM("bottom");

        private String value;

        private CalsVerticalAlign(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static CalsVerticalAlign of(@Nullable final String value) {
            return ValueEnums.findEnum(CalsVerticalAlign.class, value, null);
        }

        public static CalsVerticalAlign of(@Nullable final String value,
                @Nullable final CalsVerticalAlign defaultValue) {
            return ValueEnums.findEnum(CalsVerticalAlign.class, value, defaultValue);
        }
    }

    public final static String CALS_NOSEP = "0";

    public static enum CalsFrame implements ValueEnum {
        ALL("all"),
        SIDES("sides"),
        TOP("top"),
        BOTTOM("bottom"),
        TOPBOTTOM("topbot");

        private String value;

        private CalsFrame(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static CalsFrame of(@Nullable final String value) {
            return ValueEnums.findEnum(CalsFrame.class, value, null);
        }

        public static CalsFrame of(@Nullable final String value,
                @Nullable final CalsFrame defaultValue) {
            return ValueEnums.findEnum(CalsFrame.class, value, defaultValue);
        }
    }

    public static enum BulletStyle implements ValueEnum {
        BULLET("bullet"),
        OPEN_CIRCLE("open-circle"),
        PILCROW("pilcrow"),
        RPILCROW("rpilcrow"),
        ASTERISK("asterisk"),
        DASH("dash"),
        SECTION("section"),
        NONE("none");

        private String value;

        private BulletStyle(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static BulletStyle of(@Nullable final String value) {
            return ValueEnums.findEnum(BulletStyle.class, value, null);
        }

        public static BulletStyle of(@Nullable final String value,
                @Nullable final BulletStyle defaultValue) {
            return ValueEnums.findEnum(BulletStyle.class, value, defaultValue);
        }
    }
}
