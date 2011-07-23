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

package org.cnx.repository.service.api;

/**
 * Status codes of the repository service operations.
 */
public enum RepositoryStatus {
    /**
     * Operation done ok.
     */
    OK,
    
    /**
     * Requested object not found.
     */
    NOT_FOUND,
    
    /**
     * Version specification in request does not match object's state. Caller need to resolved the
     * version conflict.
     */
    VERSION_CONFLICT,
    
    /**
     * Cannot perform the operation on an object in this state.
     */
    STATE_MISMATCH,
    
    /**
     * Bad request format (e.g. bad format of an object id). This error is more about the request
     * syntax than the request semantic (e.g. if an object is not found, this is not classified as
     * BAD_REQUEST but as NOT_FOUND).
     */
    BAD_REQUEST,
    
    /**
     * General error, typically internal to the server.
     */
    SERVER_ERRROR;

    public boolean isOk() {
        return this == OK;
    }

    public boolean isError() {
        return !isOk();
    }
}
