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

/**
 * CNX repository service abstract API package.
 * 
 * The repository service is an abstraction layer that handles all the repository operations. This
 * package defines the abstract interface of the repository service. This interface is general
 * and is not specific to Google app engine (GAE) or any other implementation.
 * <p>
 * The repository service has an implementation for GAE at {@link org.cnx.repository.service.impl}
 * and other implementations may be added as needed. The functionality of the repository service
 * is exposed to external client via a web API layer such as the AtomPub implementation at
 * {@link org.cnx.repository.service.atompub}.
 * <p>
 * The main file in this package is the interface
 * {@link org.cnx.repository.service.api.CnxRepositoryService} which defines the methods and data
 * types of any repository service implementations.
 * <p>
 * It is very important not to contaminate this package with GAE specific types and functionality
 * since this is merely one implementation of this API.
 * 
 * @author Tal Dayan
 */
package org.cnx.repository.service.api;