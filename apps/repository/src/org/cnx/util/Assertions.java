/*
 * Copyright 2011 Google Inc.
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

package org.cnx.util;

/**
 * Runtime assertions. Always enabled.
 * 
 * @author Tal Dayan
 */
public class Assertions {

    public static void check(boolean expression) {
        if (!expression) {
            checkFailed("Assertion failed");
        }
    }

    public static void check(boolean expression, String format, Object... args) {
        if (!expression) {
            checkFailed(String.format(format, args));
        }
    }

    public static <T> T checkNotNull(T object) {
        if (object == null) {
            checkFailed("Assertion failed, pointer is null");
        }
        return object;
    }

    public static <T> T checkNotNull(T object, String format, Object... args) {
        if (object == null) {
            checkFailed(String.format(format, args));
        }
        return object;
    }

    private static void checkFailed(String message) {
        throw new IllegalArgumentException(message);
    }
}
