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
package org.cnx.repository.atompub;

import static org.cnx.repository.atompub.CnxAtomPubConstants.LATEST_VERSION_STRING;

import com.google.common.annotations.VisibleForTesting;

import org.cnx.exceptions.CnxInvalidUrlException;

/**
 * This is to wrap version in a TypeSafe object. Version has two possible values : * latest *
 * integer whose value should be non-negative (i.e. >= 0).
 * 
 * @author Arjun Satyapal
 */
public class VersionWrapper {
    private final Integer version;

    @Override
    public String toString() {
        if (version == null) {
            return LATEST_VERSION_STRING;
        }

        return version.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof VersionWrapper) {
            VersionWrapper other = (VersionWrapper) object;
            return version.equals(other.version);
        }

        return false;
    }

    public VersionWrapper getNextVersion() {
        if (version == null) {
            throw new IllegalStateException("getNext operation is not valid for version="
                    + CnxAtomPubConstants.LATEST_VERSION_STRING);
        }
        return new VersionWrapper(version + 1);
    }

    // TODO(arjuns) : Add test to ensure version and versionInt semantically mean same.
    public Integer getVersionInt() {
        return version;
    }

    public VersionWrapper(String versionString) throws CnxInvalidUrlException {
        if (!isValidVersion(versionString)) {
            throw new CnxInvalidUrlException("Invalid version : " + versionString, null /* throwable */);
        }

        if (versionString.equals(LATEST_VERSION_STRING)) {
            this.version = null;
        } else {
            try {
                this.version = Integer.parseInt(versionString);
            } catch (NumberFormatException e) {
                throw new CnxInvalidUrlException("Invalid version : " + versionString, null /* throwable */);
            }
        }
    }

    public VersionWrapper(int version) {
        if (version < 0) {
            throwInvalidVersionExecption(Long.toString(version), null /* throwable */);
        }

        this.version = version;
    }

    @VisibleForTesting
    static boolean isValidVersion(String version) {
        if (version.equals(CnxAtomPubConstants.LATEST_VERSION_STRING)) {
            return true;
        }

        if (version.startsWith("0")) {
            return false;
        }

        try {

            long versionLong = Long.parseLong(version);

            if (versionLong > 0) {
                return true;
            }

            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void throwInvalidVersionExecption(String version, Throwable throwable)
            throws CnxInvalidUrlException {
        throw new CnxInvalidUrlException("Illegal Version[" + version + "]", throwable);
    }
}
