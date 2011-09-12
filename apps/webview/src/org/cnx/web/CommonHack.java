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
package org.cnx.web;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParser;

import org.cnx.exceptions.CnxInvalidUrlException;
import org.cnx.exceptions.CnxPossibleValidIdException;
import org.cnx.repository.atompub.IdWrapper;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.resourcemapping.Resources;

import org.jdom.Document;
import org.jdom.input.SAXHandler;

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
    public static final String COLLECTION = "/collection";
    public static final String MODULE = "/module";
    public static final String CONFIG = "/config";

    public static final String MODULE_ID_PATH_PARAM = "moduleId";
    public static final String MODULE_VERSION_PATH_PARAM = "moduleVersion";
    public static final String COLLECTION_ID_PATH_PARAM = "collectionId";
    public static final String COLLECTION_VERSION_PATH_PARAM = "collectionVersion";

    public static Document parseXmlString(final SAXParser parser, final String source)
            throws SAXException, IOException {
        final SAXHandler handler = new SAXHandler();
        parser.parse(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8)), handler);
        return handler.getDocument();
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

    public static Response fetchFromRepositoryAndReturn(URL url) throws IOException {
        String cnxml = CharStreams.toString(new InputStreamReader(url.openStream()));

        return Response.ok(cnxml).build();
    }

    public static void handleCnxInvalidUrlException(IdWrapper id, VersionWrapper version,
            CnxInvalidUrlException cnxInvalidUrlException) throws CnxInvalidUrlException {
        if (id.isIdUnderForcedRange()) {
            throw new CnxPossibleValidIdException(id, cnxInvalidUrlException.getMessage(),
                    cnxInvalidUrlException.getCause());
        }
        
        throw cnxInvalidUrlException;
    }
}
