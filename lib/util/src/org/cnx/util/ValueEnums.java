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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.EnumSet;

import javax.annotation.Nullable;

/**
 *  ValueEnums provides a set of methods that operate on ValueEnum implementors.
 */
public final class ValueEnums {
    /**
     *  findEnum returns the corresponding enum for the string.
     *  <p>
     *  enumClass must implement {@link ValueEnum}.
     */
    public static <E extends Enum<E>> E findEnum(final Class<E> enumClass,
            @Nullable final String value, @Nullable final E defaultEnum) {
        checkNotNull(enumClass);
        if (value == null) {
            return defaultEnum;
        }
        for (E v : EnumSet.allOf(enumClass)) {
            if (((ValueEnum)v).getValue().equals(value)) {
                return v;
            }
        }
        return defaultEnum;
    }
}
