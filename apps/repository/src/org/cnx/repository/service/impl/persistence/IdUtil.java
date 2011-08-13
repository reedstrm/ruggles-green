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
package org.cnx.repository.service.impl.persistence;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.regex.Pattern;

/**
 * Entity id utils.
 * 
 * TODO(tal): consider to append to the id checksum bits.
 * 
 * @author Tal Dayan
 */
public class IdUtil {

    /**
     * Regex for the post prefix portion of the id. We reject values such as "0x123", "0004" or
     * "-98".
     */
    private static final Pattern SUB_ID_PATTERN = Pattern.compile("0|[1-9][0-9]{0,20}");

    /**
     * Return an id based on the given prefix and a non negative numeric id.
     */
    public static String idToString(String prefix, long id) {
        checkArgument(id >= 0, "Negative id: %s", id);
        return prefix + id;
    }

    /**
     * Decode an id generated by {@link #idToString}.
     * 
     * @param expectedPrefix the prefix passed to {@link #idToString}.
     * @param id the string id
     * @return return the decoded numeric id.
     */
    public static Long stringToId(String expectedPrefix, String id) {
        if (!id.startsWith(expectedPrefix)) {
            return null;
        }

        final String subId = id.substring(expectedPrefix.length());

        if (!SUB_ID_PATTERN.matcher(subId).matches()) {
            return null;
        }

        try {
            return Long.decode(subId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
