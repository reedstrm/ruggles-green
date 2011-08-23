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

import org.cnx.cnxml.Module;
import org.cnx.common.collxml.Collection;
import org.cnx.resourcemapping.Resources;

/**
 *  An implementor of the XmlFetcher interface can retrieve modules and collections from a source.
 *
 *  These may be locally cached, stored on another server, etc.
 */
public interface XmlFetcher {
    public Module getModule(String moduleId, String cnxml, String resourceMappingXml) throws Exception;
    public Collection getCollection(String collectionId, String collXml) throws Exception;
    public Resources getResources(String resourceMappingXml);

    // TODO(arjuns): Remove following two methods.
    public Module fetchModuleVersion(String moduleId, String version) throws Exception;
    public Collection fetchCollectionVersion(String collectionId, String version) throws Exception;

}
