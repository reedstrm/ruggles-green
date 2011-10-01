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
package org.cnx.repository.atompub.jerseyservlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.google.common.base.Objects;

import com.google.cloud.sql.jdbc.internal.Charsets;
import com.google.common.io.Files;

import java.io.File;

import org.cnx.servicedocument.AtomTextConstruct;

import javax.annotation.Nullable;
import org.cnx.common.repository.RepositoryConstants;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.common.repository.atompub.objects.AtomPubResource;

/**
 * 
 * @author Arjun Satyapal
 */
public class TestingUtils {

    // TODO(arjuns) : move this to testing utils.
    public static void validateAtomPubResource(AtomPubResource apResource, boolean isMigration,
            IdWrapper.Type expectedIdType, @Nullable VersionWrapper expectedVersion) {
        validateId(apResource.getId(), expectedIdType, isMigration);
        assertNotNull(apResource.getPublished());

        validateSelfUri(apResource, expectedIdType, expectedVersion);

        if (expectedIdType == IdWrapper.Type.RESOURCE) {
            assertNull(apResource.getEditUri());
            assertNull(apResource.getVersion());
        } else {
            assertNotNull(apResource.getEditUri());
            assertNotNull(apResource.getVersion());
        }
    }

    private static void
            validateId(IdWrapper id, IdWrapper.Type expectedIdType, boolean isMigration) {
        assertEquals(expectedIdType, id.getType());
        boolean isReservedId =
                Long.parseLong(id.getId().substring(1)) < RepositoryConstants.MIN_NON_RESERVED_KEY_ID;
        if (isMigration) {
            assertTrue(isReservedId);
        } else {
            assertTrue(!isReservedId);
        }
    }

    private static void validateSelfUri(AtomPubResource apResource, IdWrapper.Type expectedIdType,
            VersionWrapper expectedVersion) {
        // For resources, selfUri is mandatory.
        if (expectedIdType == IdWrapper.Type.RESOURCE) {
            assertNotNull(apResource.getSelfUri());
            return;
        }

        if (expectedVersion.equals(CnxAtomPubUtils.DEFAULT_VERSION)) {
            // For Modules and Collections, for default_version, it should be absent.
            assertNull(apResource.getSelfUri());
        } else {
            // For Modules and Collections, for non-default versions, selfUri should be present.
            assertNotNull(apResource.getSelfUri());
        }
    }
    
    public static void validateTitle(AtomTextConstruct textConstruct, String title) {
        assertEquals(1, textConstruct.getContent().size());
        assertEquals(title, textConstruct.getContent().get(0));
    }
    
    public static File createTempFile(@Nullable String fileName, @Nullable String content)
            throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        
        String tempFileName = fileName;
        if (tempFileName == null) {
            tempFileName = Long.toString(System.currentTimeMillis());
        }
        String filePath = tmpDir + "/" + tempFileName;
        File tmpFile = new File(filePath);
        
        if (content != null) {
            Files.write(Objects.firstNonNull(content, "Hello Wrold"), tmpFile, Charsets.UTF_8);
        }
        
        tmpFile.delete();
        return tmpFile;
    }
}
