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

package org.cnx.common.collxml;

/**
 *  Implementors of the CollectionHtmlGenerator interface can convert a collection to HTML.
 */
public interface CollectionHtmlGenerator {
    /**
     *  The generate method outputs HTML that corresponds to the given collection into a string.
     *
     *  @param coll The collection to render
     *  @return The rendered HTML string
     */
    public String generate(Collection coll) throws Exception;
}
