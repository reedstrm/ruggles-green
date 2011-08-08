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

package org.cnx.mdml;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.cnx.util.DOMUtils;
import org.w3c.dom.Element;

/**
 *  MdmlMetadata provides accessors for common MDML fields.
 */
public class MdmlMetadata implements Metadata {
    private final Element parent;
    private final String mdmlNamespace;

    public interface Factory {
        public MdmlMetadata create(Element parent);
    }

    @Inject public MdmlMetadata(@Assisted Element parent,
            @MdmlNamespace String metadataNamespace) {
        this.parent = parent;
        this.mdmlNamespace = metadataNamespace;
    }

    public String getTitle() throws Exception {
        final Element titleElement = DOMUtils.findFirstChild(parent, mdmlNamespace, "title");
        if (titleElement == null) {
            return null;
        }
        return titleElement.getTextContent();
    }
}
