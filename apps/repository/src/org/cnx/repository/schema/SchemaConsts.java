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

package org.cnx.repository.schema;

/**
 * Global schema related constants.
 * 
 * @author Tal Dayan
 */
public class SchemaConsts {

  // TODO(tal): move these definitions to the respective entities?

	/**
	 * Resource entities are persisted with this entity kind.
	 */
	public static final String RESOURCE_KEY_KIND = "Resource";

	/**
	 * Resources ids exposed via the repository API always have this prefix.
	 */
	public static final String RESOURCE_ID_PREFIX = "R";

	/**
	 * Module entities are persisted with this entity kind.
	 */
	public static final String MODULE_KEY_KIND = "Module";

	/**
	 * Modules ids exposed via the repository API always have this prefix.
	 */
	public static final String MODULE_ID_PREFIX = "M";

	/**
	 * Module version entities are persisted with this entity kind.
	 */
	public static final String MODULE_VERSION_KEY_KIND = "ModuleVer";
}
