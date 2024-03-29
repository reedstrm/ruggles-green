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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nullable;

/**
 * Base class for repository operation responses. Each operation is expected to return a subclass of
 * this class specific for the operation.
 * 
 * @author Tal Dayan
 */
public class RepositoryResponse<T> {

    private final RepositoryStatus status;

    private final String description;

    @Nullable
    private final T result;

    /**
     * Private constructor. Use static methods to construct.
     */
    private RepositoryResponse(RepositoryStatus status, String statusDescription, @Nullable T result) {
        this.status = checkNotNull(status);
        this.description = checkNotNull(statusDescription);
        this.result = result;

        checkArgument(status.isError() == (result == null), "Status: %s", status);
    }

    public boolean isOk() {
        return status.isOk();
    }

    public boolean isError() {
        return status.isError();
    }

    public RepositoryStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns an extended description that includes the description and status.
     */
    public String getExtendedDescription() {
        return description + " (" + status + ")";
    }

    /**
     * Return result. Asserts that status is ok.
     */
    public T getResult() {
        checkState(status.isOk());
        return checkNotNull(result);
    }

    /**
     * Create a new response with an OK status.
     * 
     * @param <T> the result type of this response.
     * @param statusDescription a short human friendly description of the error.
     * @param result the operation result
     * @return a new response
     */
    public static <T> RepositoryResponse<T> newOk(String statusDescription, T result) {
        checkNotNull(statusDescription);
        checkNotNull(result);
        return new RepositoryResponse<T>(RepositoryStatus.OK, statusDescription, result);
    }

    /**
     * Create a response with an error status.
     * 
     * @param <T> the result type of this response.
     * @param status an error status. Asserted to have isError() true.
     * @param statusDescription a short human friendly description of the error.
     * @return a new response.
     */
    public static <T> RepositoryResponse<T> newError(RepositoryStatus status,
            String statusDescription) {
        checkNotNull(status);
        checkArgument(status.isError(), "%s", status);
        checkNotNull(statusDescription);
        return new RepositoryResponse<T>(status, statusDescription, null);
    }
}
