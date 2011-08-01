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

package org.cnx.repository.service.impl.operations;

/**
 * @author Tal Dayan
 */
public class ParamUtil {

    /**
     * @param enumType an enum type class.
     * @param param a string value, possible null.
     * @return if param matches a value of enum type return this value otherwise return null.
     */
    public static <T extends Enum<T>> T paramToEnum(Class<T> enumType, String param) {
        if (param == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, param);
        } catch (Throwable t) {
            return null;
        }
    }
}
