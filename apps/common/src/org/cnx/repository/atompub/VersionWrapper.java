/*
 * Copyright The CNX Authors.
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
package org.cnx.repository.atompub;

import static org.cnx.repository.atompub.CnxAtomPubConstants.LATEST_VERSION_STRING;

import org.cnx.exceptions.CnxInvalidUrlException;

/**
 * This is to wrap version in a TypeSafe object.
 * Version has two possible values :
 *     * latest
 *     * integer whose value should be non-negative (i.e. >= 0).
 * @author Arjun Satyapal
 */
public class VersionWrapper {
    private final Integer versionInt;

    @Override
    public String toString() {
        if (versionInt == null) {
            return LATEST_VERSION_STRING;
        }

        return versionInt.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof VersionWrapper) {
            VersionWrapper other = (VersionWrapper) object;
            return versionInt.equals(other.versionInt);
        }

        return false;
    }

    public VersionWrapper getNextVersion() {
        return new VersionWrapper(versionInt + 1);
    }

    // TODO(arjuns) : Add test to ensure version and versionInt semantically mean same.
    public Integer getVersionInt() {
        return versionInt;
    }
    
    public VersionWrapper(String version) throws CnxInvalidUrlException {
        if (!isValidVersion(version)) {
            throw new CnxInvalidUrlException("Invalid version : " + version, null);
        }

        if(version.equals(LATEST_VERSION_STRING)) {
            this.versionInt = null;
        } else {
            this.versionInt = Integer.parseInt(version);
        }
    }

    // TODO(arjuns) : Add checks for negative version.
    public VersionWrapper(int version) {
        this.versionInt = version;
    }

    public static boolean isValidVersion(String version) {
        if (version.equals(CnxAtomPubConstants.LATEST_VERSION_STRING)) {
            return true;
        }

        try {
            Integer.parseInt(version);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
