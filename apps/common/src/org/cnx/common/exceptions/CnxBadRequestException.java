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

import org.cnx.common.http.HttpStatusEnum;

/**
 * CnxException to indicate a BAD_REQUEST.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class CnxBadRequestException extends CnxException {
    public CnxBadRequestException(String message, Throwable throwable) {
        super(HttpStatusEnum.BAD_REQUEST, message, throwable);
    }
}
