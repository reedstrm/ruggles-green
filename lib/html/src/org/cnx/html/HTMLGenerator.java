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

package org.cnx.html;

import org.w3c.dom.Node;

/**
 *  Implementors of the HTMLGenerator interface can convert CNXML to HTML.
 */
public interface HTMLGenerator {
    /**
     *  The generate method outputs HTML that corresponds to the given CNXML node to a string.  The
     *  node passed into generate is usually the XML document node, but it can be a particular
     *  element.
     *
     *  @param node The CNXML node to render
     *  @param p The writer to output to
     *  @return The rendered HTML string
     */
    public String generate(Node node) throws Exception;
}
