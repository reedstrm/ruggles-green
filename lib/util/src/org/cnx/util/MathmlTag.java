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

import org.jdom.Namespace;

/**
 *  MathmlTag holds constants for MathML elements.
 */
public enum MathmlTag {
    INVALID(null),

    MATH("math");

    public static final String NAMESPACE_URI = "http://www.w3.org/1998/Math/MathML";
    public static final Namespace NAMESPACE = Namespace.getNamespace(NAMESPACE_URI);

    private final String tag;

    private MathmlTag(@Nullable String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static MathmlTag of(@Nullable final String name) {
        return MathmlTag.of(name, INVALID);
    }

    public static MathmlTag of(@Nullable final String name, @Nullable final MathmlTag defaultTag) {
        if (name == null) {
            return defaultTag;
        }
        for (MathmlTag tag : MathmlTag.values()) {
            if (Objects.equal(tag.tag, name)) {
                return tag;
            }
        }
        return defaultTag;
    }
}
