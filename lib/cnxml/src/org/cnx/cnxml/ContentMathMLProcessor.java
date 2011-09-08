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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.xml.transform.Transformer;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.jdom.Document;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

/**
 *  ContentMathMLProcessor transforms content MathML into presentation MathML.
 */
@Singleton public class ContentMathMLProcessor implements Processor {
    private final Transformer transformer;

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public static @interface ContentToPresentation {
    }

    @Inject public ContentMathMLProcessor(@ContentToPresentation Transformer transformer) {
        this.transformer = transformer;
    }

    public Module process(Module module) throws Exception {
        final JDOMResult result = new JDOMResult();
        transformer.transform(new JDOMSource(module.getCnxml()), result);
        return new Module(module.getId(), result.getDocument(), module.getResources(),
                module.getMetadata());
    }
}
