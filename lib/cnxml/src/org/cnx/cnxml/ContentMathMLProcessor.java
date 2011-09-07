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

package org.cnx.cnxml;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javax.xml.transform.Transformer;

import org.jdom.Document;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

/**
 *  ContentMathMLProcessor transforms content MathML into presentation MathML.
 */
@Singleton public class ContentMathMLProcessor implements Processor {
    private final Transformer transformer;

    @Inject public ContentMathMLProcessor(
            @Named("ContentMathMLProcessor.transformer") Transformer transformer) {
        this.transformer = transformer;
    }

    public Module process(Module module) throws Exception {
        final JDOMResult result = new JDOMResult();
        transformer.transform(new JDOMSource(module.getCnxml()), result);
        return new Module(module.getId(), result.getDocument(), module.getResources(),
                module.getMetadata());
    }
}
