/*
    Copyright 2011 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.cnx.html;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
    ContentMathMLProcessor transforms content MathML into presentation MathML.
*/
public class ContentMathMLProcessor implements Processor {
    private static final String TRANSFORMER_FACTORY_CLASS = "org.apache.xalan.processor.TransformerFactoryImpl";
    private Transformer transformer;

    public ContentMathMLProcessor() {
        final TransformerFactory factory = TransformerFactory.newInstance(TRANSFORMER_FACTORY_CLASS, null);
        final Source xsltSource = new StreamSource(getClass().getResourceAsStream("ctop.xsl"));
        try {
            transformer = factory.newTransformer(xsltSource);
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException("ContentMathMLProcessor XSLT is invalid", e);
        }
    }

    public Node process(Node node) throws Exception {
        final Document doc = CNXML.getBuilder().newDocument();
        transformer.transform(new DOMSource(node), new DOMResult(doc));
        return doc;
    }
}
