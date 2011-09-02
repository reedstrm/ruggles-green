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

import org.jdom.Namespace;

/**
 *  The ProcessorData class defines constants for XML attributes and tags that may be added by
 *  module processors.
 *  <p>
 *  This class is package-private.
 */
class ProcessorData {
    public static final String NAMESPACE_URI = "libcnxml";
    public static final Namespace NAMESPACE = Namespace.getNamespace("proc", NAMESPACE_URI);

    public static final String ORIGINAL_SOURCE_ATTR = "original-src";
}
