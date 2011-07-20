/*
 * Copyright 2011 Google Inc.
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

package org.cnx.repository.modules;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoModuleEntity;
import org.cnx.repository.schema.JdoModuleVersionEntity;
import org.cnx.repository.schema.SchemaConsts;
import org.cnx.util.Assertions;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * An API servlet to get the xml and manifest of a module version.
 * 
 * TODO(tal): provide more details.
 * 
 * @author Tal Dayan
 */
public class GetModuleVersionServlet extends HttpServlet {

	private static final Logger log = Logger
			.getLogger(GetModuleVersionServlet.class.getName());

	private static final Pattern uriPattern = Pattern
			.compile("/module/([a-zA-Z0-9_-]+)/(latest|[0-9]+)");

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// Parse request resource id from the query.
		final String moduleUri = req.getRequestURI();
		final Matcher matcher = uriPattern.matcher(moduleUri);
		if (!matcher.matches()) {
			final String message = "Could not parse module id in request URI ["
					+ moduleUri + "]";
			log.log(Level.SEVERE, message);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
			return;
		}
		final String moduleIdString = matcher.group(1);
		final String versionParam = matcher.group(2);

		// Determine version to serve. If latest, leave as null and we will set
		// it up later.
		Integer versionToServe = versionParam.equals("latest") ? null : Integer
				.valueOf(versionParam);

		final Long moduleId = JdoModuleEntity.stringToModuleId(moduleIdString);
		if (moduleId == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Invalid module id format: [" + moduleIdString + "]");
			return;
		}
		log.info("Module id: " + moduleId + ", moduleIdString: "
				+ moduleIdString);

		PersistenceManager pm = Services.datastore.getPersistenceManager();

		final JdoModuleVersionEntity versionEntity;

		try {

			if (versionToServe == null) {
				final JdoModuleEntity moduleEntity;
				// Read the module entity to figure out what is the latest
				// version.
				//
				// TODO(tal): we don't really need transaction here, right?
				//
				try {
					moduleEntity = pm.getObjectById(JdoModuleEntity.class,
							moduleId);
				} catch (Throwable e) {
					log.log(Level.SEVERE, "Could not find module by id "
							+ moduleIdString, e);
					;
					resp.sendError(HttpServletResponse.SC_NO_CONTENT,
							"Error looking up a module: " + e.getMessage());
					return;
				}

				// Determine version number to serve
				if (moduleEntity.getVersionCount() < 1) {
					resp.sendError(HttpServletResponse.SC_NO_CONTENT, "Module "
							+ moduleIdString
							+ " does not have a published version yet.");
					return;
				}
				versionToServe = moduleEntity.getVersionCount();
			}

			// Fetch module version entity
			//
			// TODO(tal) refactor and share this with other module servlets.
			final Key parentKey = KeyFactory.createKey(
					SchemaConsts.MODULE_KEY_KIND, moduleId);
			final Key childKey = KeyFactory.createKey(parentKey,
					SchemaConsts.MODULE_VERSION_KEY_KIND, versionToServe);
			try {
				versionEntity = pm.getObjectById(JdoModuleVersionEntity.class,
						childKey);
				Assertions
						.check(versionEntity.getVersionNumber() == versionToServe,
								"Inconsistent version in module %s, expected %s found %s",
								moduleIdString, versionToServe,
								versionEntity.getVersionNumber());
			} catch (Throwable e) {
				log.log(Level.SEVERE,
						"Could not find version module of module "
								+ moduleIdString, e);
				;
				resp.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Error looking up version " + versionToServe
								+ " of module " + moduleIdString
								+ e.getMessage());
				return;
			}
		} finally {
			pm.close();
		}

		// All done OK. Return response.
		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();

		out.println();
		out.println("Module Version:");

		out.println("* Version:\n" + versionEntity.getVersionNumber() + "\n");
		out.println("* CNXM:\n" + versionEntity.getCNXMLDoc());
		out.println("* Manifest:\n" + versionEntity.getManifestDoc());
	}
}
