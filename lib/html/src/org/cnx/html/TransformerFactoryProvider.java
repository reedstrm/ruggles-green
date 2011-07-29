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
import javax.xml.transform.TransformerFactory;

/**
 *  TransformerFactoryProvider provides TransformerFactory objects.
 */
public class TransformerFactoryProvider implements Provider<TransformerFactory> {
    private String factoryClass;

    @Inject(optional=true) public void setFactoryClass(
            @Named("javax.xml.transform.TransformerFactory") String name) {
        factoryClass = name;
    }

    @Override public TransformerFactory get() {
        try {
            if (factoryClass == null) {
                return TransformerFactory.newInstance();
            } else {
                return TransformerFactory.newInstance(factoryClass, null);
            }
        } catch (Exception e) {
            // We can't throw exceptions in the provider.  Not being able to create a factory means
            // that the modules are configured wrong anyway.  Throw a runtime exception.
            throw new RuntimeException("Could not create a transformer factory, check your "
                                       + "javax.xml.transform.TransformerFactory setting.", e);
        }
    }
}
