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

import com.google.inject.ImplementedBy;
import com.google.template.soy.data.SoyData;
import org.w3c.dom.Node;

/**
 *  Implementors of the DomToSoyDataConverter interface can convert an XML DOM to Soy data.
 */
@ImplementedBy(DomToSoyDataConverterImpl.class)
public interface DomToSoyDataConverter {
    public SoyData convertDomToSoyData(Node node) throws Exception;
}
