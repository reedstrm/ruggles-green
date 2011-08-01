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
import com.google.inject.Provider;
import com.google.inject.name.Named;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 *  DocumentBuilderProvider provides a DocumentBuilder
 */
public class DocumentBuilderProvider implements Provider<DocumentBuilder> {
    private static final String XML_EXTERNAL_ENTITIES_ATTR =
            "http://xml.org/sax/features/external-general-entities";
    private static final String XML_EXTERNAL_PARAM_ENTITIES_ATTR =
            "http://xml.org/sax/features/external-parameter-entities";
    private static final String XML_EXTERNAL_DTD_ATTR =
            "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    private String factoryClass;

    @Inject(optional=true) public void setFactoryClass(
            @Named("javax.xml.parsers.DocumentBuilderFactory") String name) {
        factoryClass = name;
    }

    @Override public DocumentBuilder get() {
        final DocumentBuilderFactory factory;

        try {
            if (factoryClass == null) {
                factory = DocumentBuilderFactory.newInstance();
            } else {
                factory = DocumentBuilderFactory.newInstance(factoryClass, null);
            }
            factory.setValidating(false);
            try {
                factory.setXIncludeAware(false);
            } catch (UnsupportedOperationException e) {
                // We really don't want XInclude, so if this gives an error, we can ignore it.
            }
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setNamespaceAware(true);
            disableAttr(factory, XML_EXTERNAL_ENTITIES_ATTR);
            disableAttr(factory, XML_EXTERNAL_PARAM_ENTITIES_ATTR);
            disableAttr(factory, XML_EXTERNAL_DTD_ATTR);
            return factory.newDocumentBuilder();
        } catch (Exception e) {
            // We can't throw exceptions in the provider.  Not being able to create a factory means
            // that the modules are configured wrong anyway.  Throw a runtime exception.
            throw new RuntimeException("Could not create a document builder, check your "
                                       + "javax.xml.parsers.DocumentBuilderFactory setting.", e);
        }
    }

    private static void disableAttr(final DocumentBuilderFactory factory, final String attr) {
        try {
            factory.setAttribute(attr, false);
        } catch (IllegalArgumentException e) {
            // The factory doesn't support the attribute anyway, so the error can be ignored.
        }
    }
}
