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

package org.cnx.repository.service.api;

import java.util.Date;

import org.cnx.repository.service.impl.persistence.PersistenceMigrationUtil;

/**
 * Additional methods of the CNX repository service that are specific for the initial data migration.
 */
public interface CnxRepositoryServiceMigrationExtensions {

    /**
     * Create a new resource with enforced id.
     * 
     * Temp method for migration. Accepts the id that should be assigned to the new resource. The id
     * must be in the range of protected module ids. See
     * {@link org.cnx.repository.service.impl.persistence.PersistenceMigrationUtil} for more
     * details.
     */
    RepositoryResponse<AddResourceResult> addResourceForMigration(
            RepositoryRequestContext context, String forcedId, Date forcedCreationTime);

    /**
     * Create a new module with enforced id.
     * 
     * Temp method for migration. Accepts the id that should be assigned to the new module. The id
     * must be in the range of protected module ids. See
     * {@link org.cnx.repository.service.impl.persistence.PersistenceMigrationUtil} for more
     * details.
     */
    RepositoryResponse<AddModuleResult> addModuleForMigration(
            RepositoryRequestContext context, String forcedId);

    /**
     * Add module version
     * <p>
     * Similar to {@link CnxRepositoryService#addModuleVersion} except that it requires a specific version number,
     * allow to skip modules version numbers (takedown gaps) and allow to overwrite the last
     * module version to allow safe migrator retry.
     */
    RepositoryResponse<AddModuleVersionResult> addModuleVersionForMigration(RepositoryRequestContext context,
            String moduleId, int versionNumber, String cnxmlDoc,
            String resourceMapDoc);

    /**
     * Create a new collected with enforced id.
     * 
     * Temp method for migration. Accepts the id that should be assigned to the new collection. The
     * id must be in the range of protected collection ids. See {@link PersistenceMigrationUtil} for
     * more details.
     */
    RepositoryResponse<AddCollectionResult> addCollectionForMigration(
            RepositoryRequestContext context, String forcedId);

    /**
     * Add collection version
     * <p>
     * Similar to {@link CnxRepositoryService#addCollectionVersion} except that it requires a specific version number,
     * allow to skip collection version numbers (takedown gaps) and allow to overwrite the last
     * collection version to allow safe migrator retry.
     */
    RepositoryResponse<AddCollectionVersionResult> addCollectionVersionForMigration(
            RepositoryRequestContext context, String collectionId,
            int versionNumber, String colxmlDoc);
}
