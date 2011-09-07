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

package org.cnx.mdml;

import javax.annotation.Nullable;

import org.cnx.util.ValueEnum;
import org.cnx.util.ValueEnums;

/**
 *  MdmlAttributes holds constants for all MDML attributes.
 */
public class MdmlAttributes {
    public static final String USER_ID = "userid";
    public static final String ROLE_TYPE = "type";

    public static enum RoleType implements ValueEnum {
        AUTHOR("author"),
        LICENSOR("licensor"),
        MAINTAINER("maintainer");

        private String value;

        private RoleType(String value) {
            this.value = value;
        }

        @Override public String getValue() {
            return value;
        }

        public static RoleType of(@Nullable final String value) {
            return ValueEnums.findEnum(RoleType.class, value, null);
        }

        public static RoleType of(@Nullable final String value,
                @Nullable final RoleType defaultValue) {
            return ValueEnums.findEnum(RoleType.class, value, defaultValue);
        }
    }
}
