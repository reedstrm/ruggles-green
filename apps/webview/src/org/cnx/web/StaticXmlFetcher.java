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

package org.cnx.web;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;

/**
 *  StaticXmlFetcher fetches XML documents from static files on the server.
 *  <p>
 *  This is for testing purposes only.
 */
@Singleton public class StaticXmlFetcher implements XmlFetcher {
    private static final String PREFIX = "staticxml/";
    private static final String MODULE_SUFFIX = ".cnxml";
    private static final String RESOURCE_MAPPING_SUFFIX = "-mapping.xml";
    private static final String COLLECTION_SUFFIX = ".collxml";
    private static final String VERSION_SEP = "_";

    private final DocumentBuilder docBuilder;

    @Inject public StaticXmlFetcher(DocumentBuilder docBuilder) {
        this.docBuilder = docBuilder;
    }

    public Module fetchModuleVersion(String moduleId, String version) throws Exception {
        Document module = docBuilder.parse(
                new File(PREFIX + moduleId + VERSION_SEP + version + MODULE_SUFFIX));
        Document manifest = docBuilder.parse(
                new File(PREFIX + moduleId + VERSION_SEP + version + RESOURCE_MAPPING_SUFFIX));
        return new XmlFetcher.Module(module, manifest);
    }

    public Document fetchCollectionVersion(String collectionId, String version) throws Exception {
        return docBuilder.parse(
                new File(PREFIX + collectionId + VERSION_SEP + version + COLLECTION_SUFFIX));
    }
}
