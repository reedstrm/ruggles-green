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

import static org.cnx.repository.atompub.CommonUtils.getURI;
import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.fromRepositoryError;
import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.logAndReturn;
import static org.cnx.repository.atompub.utils.ServerUtil.getPostedEntry;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;

import org.cnx.exceptions.CnxException;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.CnxMediaTypes;
import org.cnx.repository.atompub.IdWrapper;
import org.cnx.repository.atompub.service.CnxAtomService;
import org.cnx.repository.atompub.utils.RepositoryUtils;
import org.cnx.repository.atompub.utils.ServerUtil;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateResourceResult;
import org.cnx.repository.service.api.GetResourceInfoResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.ServeResourceResult;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

/**
 * Jersey Servlet for CNX Resources.
 * 
 * @author Arjun Satyapal
 */
@Path(CnxAtomPubConstants.COLLECTION_RESOURCE_REL_PATH)
public class CnxAtomResourceServlet {
    private Logger logger = Logger.getLogger(CnxAtomResourceServlet.class.getName());
    // TODO(arjuns) : Move these servlet strings to a common place.
    private final String COLLECTION_RESOURCE_POST = "/";

    private final String RESOURCE_GET_PATH_PARAM = "resourceId";
    private final String RESOURCE_GET_URL_PATTERN = "/{" + RESOURCE_GET_PATH_PARAM + "}";

    private CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();

    /**
     * Clients should post to {@link #COLLECTION_RESOURCE_POST} in order to get a new ResourceId and
     * Blobstore URL where client will upload the blob.
     */
    @POST
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_RESOURCE_POST)
    public Response postNewResource(@Context HttpServletRequest req) throws CnxException {
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        Entry postedEntry = getPostedEntry(logger, req);

        RepositoryResponse<CreateResourceResult> createdResource =
                repositoryService.createResource(RepositoryUtils.getRepositoryContext());

        if (createdResource.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            CreateResourceResult repoResult = createdResource.getResult();
            Entry entry = new Entry();
            entry.setId(repoResult.getResourceId());
            entry.setTitle(postedEntry.getTitle());
            entry.setPublished(new Date());

            // TODO(arjuns) : Create a function for this.
            // URL to fetch the Module published now.

            IdWrapper repoIdWrapper = IdWrapper.getIdWrapper(repoResult.getResourceId());

            URL selfUrl = atomPubService.getConstants().getResourceAbsPath(repoIdWrapper);
            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, null/* editUrl */);

            // TODO(arjuns) : Temporary hack.
            // URL client is expected to post the blob.
            String uploadUrl = repoResult.getResourceUploadUrl();
            if (!uploadUrl.startsWith("http")) {
                uploadUrl = ServerUtil.computeHostUrl(req) + uploadUrl;
            }

            URI editUrl = getURI(uploadUrl);
            Link blobstoreLink = new Link();
            blobstoreLink.setRel(CnxAtomPubConstants.REL_TAG_FOR_BLOBSTORE_URL);
            blobstoreLink.setHref(editUrl.toString());

            listOfLinks.add(blobstoreLink);
            entry.setOtherLinks(listOfLinks);

            return logAndReturn(logger, Status.CREATED, entry, getURI(selfUrl.toString()));
        }

        return fromRepositoryError(logger, createdResource);
    }

    // TODO(arjuns) : Do we need URL to return AtomEntry for Resources?

    // TODO(arjuns) : Repository should start sending the content-type.
    @GET
    @Path(RESOURCE_GET_URL_PATTERN)
    public Response getResource(@Context HttpServletResponse res,
            @PathParam(RESOURCE_GET_PATH_PARAM) String resourceId) {
        final IdWrapper idWrapper = IdWrapper.getIdWrapper(resourceId);
        RepositoryRequestContext repositoryContext = RepositoryUtils.getRepositoryContext();

        RepositoryResponse<ServeResourceResult> serveResourceResult =
                repositoryService.serveResouce(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId(), res);

        if (serveResourceResult.isOk()) {

            ResponseBuilder responseBuilder = Response.ok();
            ServeResourceResult repoResult = serveResourceResult.getResult();

            for (Map.Entry<String, String> header : repoResult.getAdditionalHeaders().entrySet()) {
                responseBuilder.header(header.getKey(), header.getValue());
            }

            RepositoryResponse<GetResourceInfoResult> repositoryInfo =
                    repositoryService.getResourceInfo(repositoryContext, idWrapper.getId());

            // TODO(arjuns) : Repository should return this.
            if (repositoryInfo.isOk()) {
                String fileName =
                        repositoryInfo.getResult().getContentInfo().getContentOriginalFileName();
                if (fileName.endsWith(".cdf")) {
                    responseBuilder.header("Content-Type", "application/vnd.wolfram.cdf.text");
                }

                responseBuilder.header("Content-Disposition",
                        ("attachment; filename=\"" + fileName + "\""));
            }

            return responseBuilder.build();
        }

        return fromRepositoryError(logger, serveResourceResult);
    }
}
