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

/**
 * Status codes of the repository service operations.
 */
public enum RepositoryStatus {
    /** Trying to create new data that already exists */
    ALREADY_EXISTS,

    /**
     * Bad request format (e.g. bad format of an object id). This error is more about the request
     * syntax than the request semantic (e.g. if an object is not found, this is not classified as
     * BAD_REQUEST but as NOT_FOUND).
     */
    BAD_REQUEST,

    /** Requested object not found. */
    NOT_FOUND,

    /** Operation done ok. */
    OK,

    /** Some portion of the reuqest is out of valid range. */
    OUT_OF_RANGE,

    /** Some portion of the query is too large. */
    OVERSIZE,

    /** General error, typically internal to the server. */
    SERVER_ERROR,

    /**
     * Cannot perform the operation on an object in this state. The actual details of the state
     * mismatch are request specific. When this status is returned, client developer should have
     * sufficient information to determine what the problem is (via operation documentation, via
     * returned status message, etc).
     */
    STATE_MISMATCH,

    /**
     * Version specification in request does not match object's state. Caller need to resolved the
     * version conflict.
     */
    VERSION_CONFLICT;


    public boolean isOk() {
        return this == OK;
    }

    public boolean isError() {
        return !isOk();
    }
}
