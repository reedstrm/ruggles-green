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
 *  The Actor class stores information about a content contributor.
 */
public abstract class Actor {
    private final String fullName;
    private final String emailAddress;
    private final String homepage;

    public Actor(String fullName) {
        this(fullName, null, null);
    }

    public Actor(String fullName, @Nullable String emailAddress, @Nullable String homepage) {
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.homepage = homepage;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getHomepage() {
        return homepage;
    }

    public String toString() {
        return getFullName();
    }

    public int hashCode() {
        return Objects.hashCode(fullName, emailAddress, homepage);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o.getClass() == this.getClass()) {
            Actor other = (Actor)o;
            return Objects.equal(fullName, other.fullName)
                    && Objects.equal(emailAddress, other.emailAddress)
                    && Objects.equal(homepage, other.homepage);
        }
        return false;
    }
}
