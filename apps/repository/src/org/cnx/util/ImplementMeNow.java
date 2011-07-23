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
 * A place holder exception for code being developed. Typically don't want
 * to push to production code that refers to this exception.
 * For longer term and planned unavailability of features use NotSupportedException
 * or similar.
 *
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class ImplementMeNow extends Error {
    public ImplementMeNow(String format, Object... args) {
        super(String.format(format, args));
    }

    public ImplementMeNow() {
    }
}
