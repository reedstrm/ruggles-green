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

import org.cnx.repository.scripts.MigratorUtils;

import com.google.cloud.sql.jdbc.internal.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.sun.syndication.io.FeedException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import org.apache.commons.codec.digest.DigestUtils;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.FileContentType;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.objects.ResourceInfoWrapper;
import org.cnx.common.repository.atompub.objects.ResourceWrapper;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CnxAtomResourceServlet}
 * 
 * @author Arjun Satyapal
 */
public class CnxResourceServletTests extends CnxAtomPubBasetest {
    private CnxClient cnxClient;

    @Before
    public void initialize() throws Exception {
        cnxClient = new CnxClient(getCnxServerAtomPubUrl());
    }

    @Test
    public void test_createResource() throws Exception {
        ResourceWrapper resource = cnxClient.createResource();
        doTestForCreateResource(resource, false /* isMigaration */);
    }

    @Test
    public void test_createResourceForMigration() throws Exception {
        IdWrapper resourceIdWrapper = new IdWrapper("r0001", IdWrapper.Type.RESOURCE);
        MigratorUtils.cleanUp(cnxClient, resourceIdWrapper);

        ResourceWrapper resource = cnxClient.createResourceForMigration(resourceIdWrapper);
        doTestForCreateResource(resource, true /* isMigaration */);
    }

    private void doTestForCreateResource(ResourceWrapper resource, boolean isMigration)
            throws Exception {
        TestingUtils
                .validateAtomPubResource(resource, isMigration, IdWrapper.Type.RESOURCE,
                        null /* version */);

        String randomFileName = Long.toString(System.currentTimeMillis());

        File file = TestingUtils.createTempFile("abc.ext", null /* content */);
        Files.write("Hello Wrold", file, Charsets.UTF_8);

        String expectedContentType = FileContentType.CDF.getContentType();
        cnxClient.uploadResource(resource.getUploadUri(), expectedContentType,
                randomFileName, file);

        // Now validating ResourceInformation.
        ResourceInfoWrapper resourceInformation =
                cnxClient.getResourceInformation(resource.getId());

        String expectedMD5Hash = getMD5HashForFile(file);

        validateResourceIinfo(resourceInformation, isMigration, randomFileName,
                new Long(file.length()), expectedContentType, expectedMD5Hash);
        InputStream inputStream = cnxClient.getResource(resource.getId());
        File downloadedFile = getFileFromInputStream(inputStream);
        String downloadedFileMd5Hash = getMD5HashForFile(downloadedFile);
        assertEquals(expectedMD5Hash, downloadedFileMd5Hash);
    }

    private void validateResourceIinfo(ResourceInfoWrapper resourceInformation,
            boolean isMigration,
            String expectedFileName, Long expectedFileLength, String expectedContentType,
            String expectedMd5Hash) {
        TestingUtils.validateAtomPubResource(resourceInformation, isMigration,
                IdWrapper.Type.RESOURCE, null /* version */);
        assertEquals(expectedFileName, resourceInformation.getFileName());
        assertEquals(expectedContentType, resourceInformation.getFileContentType());
        assertEquals(expectedFileLength, resourceInformation.getContentSize());

        // TODO(arjuns) : Fix this according to internal appengine bug b/5375118.
        // assertEquals(expectedMd5Hash, resourceInformation.getMd5hash());
    }

    private File getFileFromInputStream(InputStream inputStream) throws IOException {
        File tmpFile = TestingUtils.createTempFile(null /* fileName */, null /* content */);
        final OutputStream outputStream = new FileOutputStream(tmpFile);
        try {
            ByteStreams.copy(inputStream, outputStream);
        } finally {
            outputStream.close();
        }
        return tmpFile;
    }

    // TODO(arjuns) : move this to testing utils.
    private String getMD5HashForFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String md5 = DigestUtils.md5Hex(fis);
        return md5;
    }

    @Test
    public void testCreateResource_NO_FILE_EXT() throws IllegalArgumentException,
            URISyntaxException, IOException, JDOMException, FeedException, CnxException {
        ResourceWrapper resource = cnxClient.createResource();
        String fileName = "noext";

        File file = TestingUtils.createTempFile(fileName, null /* content */);
        Files.write("Hello World", file, Charsets.UTF_8);

        String expectedContentType = FileContentType.DEFAULT.getContentType();
        cnxClient.uploadResource(resource.getUploadUri(), expectedContentType, fileName, file);

        // Now validating ResourceInformation.
        ResourceInfoWrapper resourceInformation =
                cnxClient.getResourceInformation(resource.getId());

        String expectedMD5Hash = getMD5HashForFile(file);

        validateResourceIinfo(resourceInformation, false /* isMigration */, fileName,
                new Long(file.length()), expectedContentType, expectedMD5Hash);
    }

    @Test
    public void testCreateResource_NO_FILE_NAME() throws
            IllegalArgumentException,
            URISyntaxException, IOException, JDOMException, FeedException, CnxException {
        ResourceWrapper resource = cnxClient.createResource();

        File file = TestingUtils.createTempFile(null/* fileName */, null /* content */);
        Files.write("Hello Wrold", file, Charsets.UTF_8);

        String expectedContentType = FileContentType.DEFAULT.getContentType();
        cnxClient.uploadResource(resource.getUploadUri(), expectedContentType, null /* fileName */,
                file);

        // Now validating ResourceInformation.
        ResourceInfoWrapper resourceInformation =
                cnxClient.getResourceInformation(resource.getId());

        String expectedMD5Hash = getMD5HashForFile(file);

        validateResourceIinfo(resourceInformation, false /* isMigration */, file.getName(),
                new Long(file.length()), expectedContentType, expectedMD5Hash);
    }
}
