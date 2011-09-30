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
package org.cnx.common.exceptions;

import org.cnx.common.repository.RepositoryConstants;
import org.cnx.common.repository.atompub.IdWrapper;

import javax.ws.rs.core.Response.Status;

/**
 * This is a special exception which is thrown when module/collection ids are valid, but fall under
 * {@link RepositoryConstants#MIN_NON_RESERVED_KEY_ID}. So it is possible that those
 * modules/collections are not yet migrated to AER. So a special 404 page is shown to users so that
 * they can attempt to fetch those modules/collections from cnx.org.
 * 
 * Here we dont try to retain version information, and Readers are always redirected to the latest
 * version for collection/module.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class CnxPossibleValidIdException extends CnxRuntimeException {
    private final IdWrapper id;

    public CnxPossibleValidIdException(IdWrapper id, String message,
            Throwable throwable) {
        super(Status.SEE_OTHER, message, throwable);
        this.id = id;
    }

    public IdWrapper getId() {
        return id;
    }
}
