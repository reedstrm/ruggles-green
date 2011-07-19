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

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PrimaryKey;

import org.cnx.repository.common.KeyUtil;
import org.cnx.util.Nullable;

/**
 * A JDO representing a module entity.
 * 
 * @author Tal Dayan
 */
@PersistenceCapable(table = SchemaConsts.MODULE_KEY_KIND)
public class JdoModuleEntity {

	/**
	 * The key of this module in the data store. Assigned automatically by the
	 * data store when this object is persisted for the first time. Unique only
	 * within the keys of this entity type. The externally exposed module id is
	 * derived from this key.
	 */
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	/**
	 * Number of versions of this module. Value >= 0. If > 0, this is also the
	 * version of the last version of this module (version numbering is 1, 2,
	 * ...).
	 */
	@Persistent
	private Integer versionCount = 0;

	/**
	 * Get the id of this module.
	 */
	@Nullable
	public Long getId() {
		return id;
	}

	public int versionCount() {
		return versionCount;
	}

	public int incrementVersionCount() {
		versionCount++;
		return versionCount;
	}

	/**
	 * Convert an module id to external string representation.
	 */
	public static String moduleIdToString(Long moduleId) {
		return KeyUtil.idToString(SchemaConsts.MODULE_ID_PREFIX, moduleId);
	}

	/**
	 * Convert a module id in external string representation to internal module
	 * id. Return the module id or null of invalid string format.
	 */
	@Nullable
	public static Long stringToModuleId(String idString) {
		return KeyUtil.stringToId(SchemaConsts.MODULE_ID_PREFIX, idString);
	}
}
