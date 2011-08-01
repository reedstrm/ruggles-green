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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *  ContentMathMLProcessor transforms content MathML into presentation MathML.
 */
@Singleton public class ContentMathMLProcessor implements Processor {
    private final Transformer transformer;
    private final DocumentBuilder documentBuilder;

    @Inject public ContentMathMLProcessor(
            @Named("ContentMathMLProcessor.transformer") Transformer transformer,
            DocumentBuilder documentBuilder) {
        this.documentBuilder = documentBuilder;
        this.transformer = transformer;
    }

    public Node process(Node node) throws Exception {
        final Document doc = documentBuilder.newDocument();
        transformer.transform(new DOMSource(node), new DOMResult(doc));
        return doc;
    }
}
