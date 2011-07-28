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
package org.cnx.repository.atompub.service;

/**
 * Enum to encapsulate types of Handlers provied by CNX AtomPub API.
 * 
 * @author Arjun Satyapal
 */
public enum CnxAtomHandlerEnum {
    /** This will handle requests related to Resource. */
    RESOURCE,
    
    /** This will handle requests related to Module. */
    MODULE,
    
    /** This will handle requests related to CNX Collections. */
    COLLECTION,
    
    /** This will handle all other requests. */
    SERVICE;
}
