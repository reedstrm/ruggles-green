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

import javax.jdo.JDOHelper;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PrimaryKey;

import org.cnx.repository.common.KeyUtil;
import org.cnx.util.Assertions;
import org.cnx.util.Nullable;

/**
 * A JDO representing a module version entity.
 * 
 * @author Tal Dayan
 */
@PersistenceCapable(table = SchemaConsts.MODULE_VERSION_KEY_KIND)
public class JdoModuleVersionEntity {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	/**
	 * The ID of the module entity to which this version belong.
	 */
	@Persistent
	private Long moduleId;

	/**
	 * Version number. First version is 1, second is 2, etc.
	 */
	@Persistent
	private Integer versionNumber;
	
	@Persistent
	private String CNXMLDoc;
	
	public int versionNumber() {
		return versionNumber;
	}
	
	public String CNXMLDoc() {
		return CNXMLDoc;
	}
}
