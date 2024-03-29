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

import com.google.common.base.Objects;
import javax.annotation.Nullable;

/**
 *  The Organization class holds a group contributor.
 */
public class Organization extends Actor {
    private final String shortName;

    public Organization(String fullName, String shortName, @Nullable String emailAddress,
            @Nullable String homepage) {
        super(fullName, emailAddress, homepage);
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public int hashCode() {
        return Objects.hashCode(shortName, super.hashCode());
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!super.equals(o)) {
            return false;
        }

        // If Actor says we're equal, then the classes are identical.
        Organization other = (Organization)o;
        return Objects.equal(shortName, other.shortName);
    }
}
