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
package org.cnx.web.servlets;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.cnx.resourcemapping.Resources;

/**
 * TODO(arjuns) : Figure out what to do with this class.
 *
 * @author Arjun Satyapal
 */
public class CommonHack {
    public static final String MODULE_TEMPLATE_NAME = "org.cnx.web.module";
    public static final String COLLECTION_TEMPLATE_NAME = "org.cnx.web.collection";
    public static final String COLLECTION_MODULE_TEMPLATE_NAME = "org.cnx.web.collectionModule";
    public static final String CONTENT_NAME_SPACE = "/content";

    private static final String REPO_SERVER_URL = "http://100.cnx-repo.appspot.com";
//     public static String REPO_SERVER_URL = "http://localhost:8888";

    public static final String REPO_ATOM_PUB_URL = REPO_SERVER_URL + "/atompub";

    public static final String COLLECTION_URI_PREFIX = CONTENT_NAME_SPACE + "/collection/";


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
