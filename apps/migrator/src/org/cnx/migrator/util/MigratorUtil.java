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
package org.cnx.migrator.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.InputStream;

import javax.annotation.Nullable;

import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 * Assorted util methods.
 * 
 * @author tal
 */
public class MigratorUtil {

    /** Like Thread.sleep() but throws an unchecked exception instead */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to sleep", e);
        }
    }

    /** Similar to InputStream.close() but throws an unchecked exception instead. */
    public static void safeClose(@Nullable InputStream in) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Assert that an atompub entry has given base id and version number */
    public static void checkAtombuyEntryId(String expectedBaseId, int expectedVersionNumber,
            ClientEntry actualEntry) {
        final String actualId = actualEntry.getId();
        final String expectedId = String.format("%s:%d", expectedBaseId, expectedVersionNumber);
        checkArgument(expectedId.equals(actualEntry.getId()),
                "Entry ID mismatch, Expected: %s, found: %s", expectedId, actualId);
    }
}
