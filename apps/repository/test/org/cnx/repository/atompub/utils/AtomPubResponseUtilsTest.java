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
package org.cnx.repository.atompub.utils;

import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.getJerseyStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.cnx.repository.service.api.RepositoryStatus;
import org.junit.Test;

import java.util.Set;

import javax.ws.rs.core.Response.Status;

/**
 * Test for {@link AtomPubResponseUtils}
 * 
 * @author Arjun Satyapal
 */
public class AtomPubResponseUtilsTest {
    // This test ensures that for each RespositoryStatus there is a corresponding
    // value in Map.
    @Test
    public void testRepositoryStatusToJerseyStatusMap() {
        Set<RepositoryStatus> keySet =
            AtomPubResponseUtils.repositoryStatusToJerseyStatusMap.keySet();
        assertEquals(keySet.size(), RepositoryStatus.values().length);

        // Now check that all enums exist in Map.
        for (RepositoryStatus currRepoStatus : RepositoryStatus.values()) {
            assertTrue(keySet.contains(currRepoStatus));
            validateRepoStatus(currRepoStatus);
        }
    }

    private void validateRepoStatus(RepositoryStatus currRepoStatus) {
        switch (currRepoStatus) {
            case ALREADY_EXISTS:
                validateFor_ALREADY_EXISTS();
                break;

            case BAD_REQUEST:
                validateFor_BAD_REQUEST();
                break;
                
            case NOT_FOUND:
                validateFor_NOT_FOUND();
                break;
                
            case OK:
                validateFor_OK();
                break;
                
            case OUT_OF_RANGE:
                validateFor_OUT_OF_RANGE();
                break;

            case OVERSIZE:
                validateFor_OVERSIZE();
                break;

            case SERVER_ERROR:
                validateFor_SERVER_ERRROR();
                break;

            case STATE_MISMATCH:
                validateFor_STATE_MISMATCH();
                break;

            case VERSION_CONFLICT:
                validateFor_VERSION_CONFLICT();
                break;

            default:
                throw new IllegalArgumentException("Test not implemented for : " + currRepoStatus);
        }
    }

    /**
     * Validate for RepositoryStatus = {@link RepositoryStatus#ALREADY_EXISTS}
     */
    private void validateFor_ALREADY_EXISTS() {
        RepositoryStatus repoStatus = RepositoryStatus.ALREADY_EXISTS;
        assertEquals(Status.CONFLICT, getJerseyStatus(repoStatus));
    }

    /**
     * Validate for RepositoryStatus = {@link RepositoryStatus#BAD_REQUEST}
     */
    private void validateFor_BAD_REQUEST() {
        RepositoryStatus repoStatus = RepositoryStatus.BAD_REQUEST;
        assertEquals(Status.BAD_REQUEST, getJerseyStatus(repoStatus));
    }

    /**
     * Validate for RepositoryStatus = {@link RepositoryStatus#NOT_FOUND}
     */
    private void validateFor_NOT_FOUND() {
        RepositoryStatus repoStatus = RepositoryStatus.NOT_FOUND;
        assertEquals(Status.NOT_FOUND, getJerseyStatus(repoStatus));
    }

    /**
     * Validate for RepositoryStatus = {@link RepositoryStatus#OK}
     */
    private void validateFor_OK() {
        RepositoryStatus repoStatus = RepositoryStatus.OK;
        assertEquals(Status.OK, getJerseyStatus(repoStatus));
    }

    /**
     * Validate for RepositoryStatus = {@link RepositoryStatus#OUT_OF_RANGE}
     */
    private void validateFor_OUT_OF_RANGE() {
        RepositoryStatus repoStatus = RepositoryStatus.OUT_OF_RANGE;
        assertEquals(Status.FORBIDDEN, getJerseyStatus(repoStatus));
    }

    /**
     * Validate for RepositoryStatus = {@link RepositoryStatus#OVERSIZE}
     */
    private void validateFor_OVERSIZE() {
        RepositoryStatus repoStatus = RepositoryStatus.OVERSIZE;
        assertEquals(Status.NOT_ACCEPTABLE, getJerseyStatus(repoStatus));
    }

    /**
     * Validate for RepositoryStatus = {@link RepositoryStatus#SERVER_ERROR}
     */
    private void validateFor_SERVER_ERRROR() {
        RepositoryStatus repoStatus = RepositoryStatus.SERVER_ERROR;
        assertEquals(Status.INTERNAL_SERVER_ERROR, getJerseyStatus(repoStatus));
    }

    /**
     * Validate for RepositoryStatus = {@link RepositoryStatus#STATE_MISMATCH}
     */
    private void validateFor_STATE_MISMATCH() {
        RepositoryStatus repoStatus = RepositoryStatus.STATE_MISMATCH;
        assertEquals(Status.PRECONDITION_FAILED, getJerseyStatus(repoStatus));
    }

    /**
     * Validate for RepositoryStatus = {@link RepositoryStatus#VERSION_CONFLICT}
     */
    private void validateFor_VERSION_CONFLICT() {
        RepositoryStatus repoStatus = RepositoryStatus.VERSION_CONFLICT;
        assertEquals(Status.CONFLICT, getJerseyStatus(repoStatus));
    }
}
