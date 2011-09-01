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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import com.sun.syndication.feed.atom.Entry;

import org.cnx.exceptions.CnxException;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;

import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

/**
 * @author arjuns@google.com (Arjun Satyapal)
 * 
 */
public class AtomPubResponseUtils {
    public static Map<RepositoryStatus, Status> repositoryStatusToJerseyStatusMap;

    static {
        initRepositoryStatusToJerseyStatusMap();
    }

    /**
     * Creates mapping from Repository Status to Jersey Response Status.
     * 
     * TODO(arjuns) : Add a test to validate that all the values are in both maps.
     */
    private static void initRepositoryStatusToJerseyStatusMap() {
        Builder<RepositoryStatus, Status> mapBuilder = ImmutableMap.builder();
        // TODO(arjuns) : Validate with HTTP codes.
        mapBuilder.put(RepositoryStatus.ALREADY_EXISTS, Status.CONFLICT);
        mapBuilder.put(RepositoryStatus.BAD_REQUEST, Status.BAD_REQUEST);
        mapBuilder.put(RepositoryStatus.NOT_FOUND, Status.NOT_FOUND);
        mapBuilder.put(RepositoryStatus.OK, Status.OK);
        // TODO(arjuns) : Add code for 416.
        mapBuilder.put(RepositoryStatus.OUT_OF_RANGE, Status.FORBIDDEN);
        mapBuilder.put(RepositoryStatus.OVERSIZE, Status.NOT_ACCEPTABLE);
        mapBuilder.put(RepositoryStatus.SERVER_ERROR, Status.INTERNAL_SERVER_ERROR);
        mapBuilder.put(RepositoryStatus.STATE_MISMATCH, Status.PRECONDITION_FAILED);
        mapBuilder.put(RepositoryStatus.VERSION_CONFLICT, Status.CONFLICT);

        repositoryStatusToJerseyStatusMap = mapBuilder.build();
    }

    public static Status getJerseyStatus(RepositoryStatus repoStatus) {
        return repositoryStatusToJerseyStatusMap.get(repoStatus);
    }

    public static Response logAndReturn(Logger logger, Status jerseyStatus, Entry entry)
            throws CnxException {
        String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
        return logAndReturn(logger, jerseyStatus, stringEntry, null /* location */);
    }

    public static Response logAndReturn(Logger logger, Status jerseyStatus, Entry entry,
            URI location) throws CnxException {
        String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
        return logAndReturn(logger, jerseyStatus, stringEntry, location);
    }

    public static Response logAndReturn(Logger logger, Status jerseyStatus, String responseString,
            URI location) {
        ResponseBuilder responseBuilder;
        if (jerseyStatus == Status.CREATED) {
            responseBuilder = Response.created(location);
        } else {
            responseBuilder = Response.status(jerseyStatus);
        }

        responseBuilder.entity(responseString);

        logger.fine("Returning response : " + responseString);
        return responseBuilder.build();
    }

    public static Response fromRepositoryError(Logger logger,
            @SuppressWarnings("rawtypes") RepositoryResponse response) {
        Preconditions.checkArgument(response.isError(), "This should be called only for Errors.");
        Status jerseyStatus = getJerseyStatus(response.getStatus());
        String description = response.getExtendedDescription();

        return logAndReturn(logger, jerseyStatus, description, null /* location */);
    }
}
