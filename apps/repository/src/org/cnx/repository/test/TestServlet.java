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

package org.cnx.repository.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.schema.JdoResourceEntity;
import org.cnx.util.Assertions;

import com.google.appengine.api.datastore.Key;

/**
 * A test servlet for initial development and experimentation.
 * 
 * TODO(tal): delete when not needed anymore..
 * 
 * @author Tal Dayan
 */
public class TestServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("Hello world");
    }
}