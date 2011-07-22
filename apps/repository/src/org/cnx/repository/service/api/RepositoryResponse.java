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

import org.cnx.util.Nullable;

import com.google.common.base.Preconditions;

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
    private RepositoryResponse(RepositoryStatus status, @Nullable String statusDescription,
        @Nullable T result) {
        // TODO(tal): do sanity checks
        this.status = Preconditions.checkNotNull(status);
        this.description = statusDescription;
        this.result = result;
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
     * Return result. Asserts that status is ok.
     */
    public T getResult() {
        Preconditions.checkState(status.isOk());
        return Preconditions.checkNotNull(result);
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
        Preconditions.checkNotNull(statusDescription);
        Preconditions.checkNotNull(result);
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
        Preconditions.checkNotNull(status);
        Preconditions.checkArgument(status.isError(), "%s", status);
        Preconditions.checkNotNull(statusDescription);
        return new RepositoryResponse<T>(status, statusDescription, null);
    }
}
