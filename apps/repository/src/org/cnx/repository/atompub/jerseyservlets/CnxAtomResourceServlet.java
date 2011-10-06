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

import static org.cnx.common.repository.atompub.CommonUtils.getURI;
import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.fromRepositoryError;
import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.logAndReturn;

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

import org.cnx.common.exceptions.CnxBadRequestException;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.ContentType;
import org.cnx.common.repository.PrettyXmlOutputter;
import org.cnx.common.repository.RepositoryConstants;
import org.cnx.common.repository.atompub.CnxAtomPubLinkRelations;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.ServletUris;
import org.cnx.common.repository.atompub.objects.ResourceInfoWrapper;
import org.cnx.repository.atompub.service.CnxAtomService;
import org.cnx.repository.atompub.utils.RepositoryUtils;
import org.cnx.repository.atompub.utils.ServerUtil;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.AddResourceResult;
import org.cnx.repository.service.api.GetResourceInfoResult;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.ServeResourceResult;
import org.cnx.repository.service.api.UploadedResourceContentInfo;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;

/**
 * Jersey Servlet for CNX Resources.
 * 
 * @author Arjun Satyapal
 */
@Path(ServletUris.Resource.RESOURCE_SERVLET)
public class CnxAtomResourceServlet {
    private final Logger logger = Logger.getLogger(CnxAtomResourceServlet.class.getName());
    private CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();
    private CnxAtomService atomPubService;

    /**
     * When Client does HTTP-POST on
     * {@link org.cnx.common.repository.atompub.ServletUris.Resource#RESOURCE_POST_NEW}, then this method
     * is invoked.
     * 
     * This method in turn sends request to {@link CnxRepositoryService#createResource}.
     * 
     * This method is used to create a new ResourceId.
     * 
     * @param req HttpServletRequest
     */
    @POST
    @Produces(ContentType.APPLICATION_ATOM_XML)
    @Path(ServletUris.Resource.RESOURCE_POST_NEW)
    public Response createNewResource(@Context HttpServletRequest req) throws CnxException {
        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<AddResourceResult> createdResource =
                repositoryService.addResource(RepositoryUtils.getRepositoryContext());

        return handleCreationOfResource(req, atomPubService, createdResource);
    }

    /**
     * When Clients does HTTP-POST on
     * {@link org.cnx.common.repository.atompub.ServletUris.Resource#RESOURCE_POST_MIGRATION}, then this
     * method is invoked.
     * 
     * This is a special function provided in order to allow migration and retaining old ResourceIds
     * from CNX. Once migration is complete, this method will be removed.
     * 
     * In functionality it is similar to {@link #createNewResource}.
     * 
     * @param req HttpServletRequest.
     * @param resourceId Id that client wants to retain. It should be less then
     *            {@link RepositoryConstants#MIN_NON_RESERVED_KEY_ID}.
     */
    @POST
    @Produces(ContentType.APPLICATION_ATOM_XML)
    @Path(ServletUris.Resource.RESOURCE_POST_MIGRATION)
    public Response createNewResourceForMigration(@Context HttpServletRequest req,
            @PathParam(ServletUris.RESOURCE_ID_PATH_PARAM) String resourceId) throws CnxException {
        final IdWrapper idWrapper = new IdWrapper(resourceId, IdWrapper.Type.RESOURCE);
        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        // TODO(tal): get this from the request (required param).
        final Date forcedCreationTime = new Date();

        RepositoryResponse<AddResourceResult> createdResource =
                repositoryService.addResourceForMigration(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId(), forcedCreationTime);

        return handleCreationOfResource(req, atomPubService, createdResource);
    }

    private Response
    handleCreationOfResource(HttpServletRequest req, CnxAtomService atomPubService,
            RepositoryResponse<AddResourceResult> createdResource)
                    throws CnxBadRequestException, CnxException {
        if (createdResource.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            AddResourceResult repoResult = createdResource.getResult();
            Entry entry = new Entry();
            entry.setId(repoResult.getResourceId());
            entry.setPublished(new Date());

            // TODO(arjuns) : Create a function for this.
            // URL to fetch the Module published now.

            IdWrapper repoIdWrapper = new IdWrapper(repoResult.getResourceId(), IdWrapper.Type.RESOURCE);

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
            blobstoreLink.setRel(CnxAtomPubLinkRelations.UPLOAD_URL.getLinkRelation());
            blobstoreLink.setHref(editUrl.toString());

            listOfLinks.add(blobstoreLink);
            entry.setOtherLinks(listOfLinks);

            return logAndReturn(logger, Status.CREATED, entry, getURI(selfUrl.toString()));
        }

        return fromRepositoryError(logger, createdResource);
    }

    // TODO(arjuns) : Do we need URL to return AtomEntry for Resources?

    // TODO(arjuns) : Repository should start sending the content-type.

    /**
     * When client does HTTP-GET on
     * {@link org.cnx.common.repository.atompub.ServletUris.Resource#RESOURCE_PATH}, then this method is
     * invoked.
     * 
     * This method in turn contacts {@link CnxRepositoryService#serveResouce}.
     * 
     * This method is used to fetch Resource from repository. Repository returns a set of headers,
     * which are set as part of the response. One of the important headers is BlobKey. This header
     * is consumed by AppEngine, and AppEngine replaces this header with actual Blobstore content.
     * 
     * @param res HttpServletResponse.
     * @param resourceId ResourceId requested by Client.
     * @return Response with headers returned by {@link CnxRepositoryService}
     */
    @GET
    @Path(ServletUris.Resource.RESOURCE_PATH)
    public Response getResource(@Context HttpServletResponse res,
            @PathParam(ServletUris.RESOURCE_ID_PATH_PARAM) String resourceId) {
        final IdWrapper idWrapper = new IdWrapper(resourceId, IdWrapper.Type.RESOURCE);
        // TODO(tal): allow callers to specify baseSaveFileName (using null for now).
        RepositoryResponse<ServeResourceResult> serveResourceResult =
                repositoryService.serveResouce(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId(), null, res);

        if (serveResourceResult.isOk()) {

            ResponseBuilder responseBuilder = Response.ok();
            ServeResourceResult repoResult = serveResourceResult.getResult();

            for (Map.Entry<String, String> header : repoResult.getAdditionalHeaders().entrySet()) {
                responseBuilder.header(header.getKey(), header.getValue());
            }

            return responseBuilder.build();
        }

        return fromRepositoryError(logger, serveResourceResult);
    }

    @GET
    @Produces(ContentType.APPLICATION_XML)
    @Path(ServletUris.Resource.RESOURCE_INFO)
    public Response getResourceInfo(@Context HttpServletRequest req,
            @PathParam(ServletUris.RESOURCE_ID_PATH_PARAM) String resourceId) throws CnxException {
        final IdWrapper idWrapper = new IdWrapper(resourceId, IdWrapper.Type.RESOURCE);
        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<GetResourceInfoResult> repoResult =
                repositoryService.getResourceInfo(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId());
        if (repoResult.isOk()) {
            GetResourceInfoResult resourceInfoResult = repoResult.getResult();
            Entry entry = new Entry();
            entry.setId(resourceInfoResult.getResourceId());
            entry.setPublished(resourceInfoResult.getCreationTime());

            if (resourceInfoResult.hasContent()) {
                UploadedResourceContentInfo contentInfo = resourceInfoResult.getContentInfo();
                StringBuilder stringBuilder = new StringBuilder();
                appendNameValuePair(stringBuilder, ResourceInfoWrapper.CONTENT_SIZE,
                        Long.toString(contentInfo.getContentSize()));
                appendNameValuePair(stringBuilder, ResourceInfoWrapper.MD5HASH,
                        contentInfo.getMd5Hash());

                Content summary = new Content();
                summary.setValue(stringBuilder.toString());
                entry.setSummary(summary);

                Content title = new Content();
                title.setType(contentInfo.getContentType());
                title.setValue(contentInfo.getContentOriginalFileName());
                entry.setTitleEx(title);

                URL selfUrl = atomPubService.getConstants().getResourceAbsPath(idWrapper);
                List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, null/* editUrl */);
                entry.setOtherLinks(listOfLinks);
            }

            System.out.println(PrettyXmlOutputter.prettyXmlOutputEntry(entry));
            return logAndReturn(logger, Status.OK, entry, null /* locationUri */);
        }
        return fromRepositoryError(logger, repoResult);
    }

    private void appendNameValuePair(StringBuilder stringBuilder, String key, String value) {
        stringBuilder.append("{" + key + "=" + value + "}\n");
    }
}
