/*
 * Copyright 2011 Google Inc.
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
package org.cnx.web;

import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;

import org.cnx.resourcemapping.Resources;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * TODO(arjuns) : Figure out what to do with this class.
 *
 * @author Arjun Satyapal
 */
public class CommonHack {
    public static final String FRONT_PAGE_TEMPLATE_NAME = "org.cnx.web.index";
    public static final String MODULE_TEMPLATE_NAME = "org.cnx.web.module";
    public static final String COLLECTION_TEMPLATE_NAME = "org.cnx.web.collection";
    public static final String COLLECTION_MODULE_TEMPLATE_NAME = "org.cnx.web.collectionModule";
    public static final String CONTENT_NAME_SPACE = "/content";
    public static final String COLLECTION_URI_PREFIX = CONTENT_NAME_SPACE + "/collection/";
    public static final String MODULE_URI_PREFIX = CONTENT_NAME_SPACE + "/module/";

    public static Document parseXmlString(final DocumentBuilder builder, final String source)
            throws SAXException, IOException {
        return builder.parse(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8)));
    }

    public static Resources getResourcesFromResourceMappingDoc(String resourceMappingXml) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Resources.class);
            Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
            Resources resources =
                (Resources) unMarshaller.unmarshal(new StringReader(resourceMappingXml));
            return resources;
        } catch (JAXBException e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }
    }
}
