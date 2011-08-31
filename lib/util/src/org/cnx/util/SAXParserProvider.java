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

package org.cnx.util;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 *  SAXParserProvider allows Guice to inject SAXParser instances.
 *  <p>
 *  Parsers from this provider are automatically secured with {@link SecureGenericXMLFactory}.
 */
public class SAXParserProvider implements Provider<SAXParser> {
    private static final String FACTORY_SETTING = "javax.xml.parsers.SAXParserFactory";

    private String factoryClass;

    @Inject(optional=true) public void setFactoryClass(@Named(FACTORY_SETTING) String name) {
        factoryClass = name;
    }

    @Override public SAXParser get() {
        try {
            final SAXParserFactory factory =
                    SecureGenericXMLFactory.getSAXParserFactory(getFactory());
            factory.setNamespaceAware(true);
            return factory.newSAXParser();
        } catch (Exception e) {
            // We can't throw exceptions in the provider.  Not being able to create a parser means
            // that the modules are configured wrong anyway.  Throw a runtime exception.
            throw new RuntimeException("Could not create a parser factory, check your "
                    + FACTORY_SETTING + " setting.", e);
        }
    }

    private SAXParserFactory getFactory() {
        if (factoryClass == null) {
            return SAXParserFactory.newInstance();
        } else {
            return SAXParserFactory.newInstance(factoryClass, null);
        }
    }
}
