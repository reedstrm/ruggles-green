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

package org.cnx.common.collxml;

import com.google.common.base.Objects;

import javax.annotation.Nullable;

import org.jdom.Namespace;

/**
 *  CollxmlTag holds constants for all CollXML elements.
 */
public enum CollxmlTag {
    INVALID(null),

    METADATA("metadata"),
    CONTENT("content"),
    MODULE("module"),
    SUBCOLLECTION("subcollection");

    private final String tag;

    public static final String NAMESPACE_URI = "http://cnx.rice.edu/collxml";
    public static final Namespace NAMESPACE = Namespace.getNamespace(NAMESPACE_URI);

    private CollxmlTag(@Nullable String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static CollxmlTag of(@Nullable final String name) {
        for (CollxmlTag tag : CollxmlTag.values()) {
            if (Objects.equal(tag.tag, name)) {
                return tag;
            }
        }
        return INVALID;
    }
}
