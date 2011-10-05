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
package org.cnx.common.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.cnx.common.repository.FileContentType;

import java.security.SecureRandom;

import org.junit.Test;

/**
 * Test for {@link FileContentType}
 * 
 * @author Arjun Satyapal
 */
public class FileContentTypeEnumTest {
    @Test
    public void testUniqExtensions() {
        for (FileContentType curr : FileContentType.values()) {
            for (FileContentType compareTo : FileContentType.values()) {
                if (curr == compareTo) {
                    continue;
                }

                assertFalse(curr.getExtension().equals(compareTo.getExtension()));
            }
        }
    }

    @Test
    public void test_getFileContentTypeEnumFromExtension() {
        SecureRandom random = new SecureRandom();
        int randomIndex = Math.abs(random.nextInt()) % FileContentType.values().length;

        FileContentType expectedEnum = FileContentType.values()[randomIndex];

        FileContentType actualEnum =
                FileContentType.getFileContentTypeEnumFromFileName("random"
                        + expectedEnum.getExtension());
        assertEquals(expectedEnum, actualEnum);

        assertEquals(FileContentType.DEFAULT,
                FileContentType.getFileContentTypeEnumFromFileName("random"));
    }
}
