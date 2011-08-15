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

import com.google.common.collect.ImmutableMap;

/**
 * Immutable container for repository service configuration.
 * 
 * This class is part of the abstracted CNX repository service API and thus should not include
 * implementation specific configuration attributes.
 * 
 * @author tal
 * 
 */
public interface CnxRepositoryConfiguration {

    /**
     * Get supported export types.
     * 
     * @return a map from export type id to export type spec.
     */
    ImmutableMap<String, ExportType> getExportTypes();

    /**
     * Get max allowable resource size in bytes.
     * 
     * Resources larger than this size are rejected upon upload. Existing resources with larger size
     * are allowed.
     */
    long getMaxResourceSize();
}
