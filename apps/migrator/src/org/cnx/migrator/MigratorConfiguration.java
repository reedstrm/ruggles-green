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
package org.cnx.migrator;

import static com.google.common.base.Preconditions.checkArgument;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Contains the configuration parameters of the migration session.
 * 
 * @author tal
 */
public class MigratorConfiguration {

    // e.g. "http://localhost:8888/atompub"
    // e.g. http://qa-cnx-repo.appspot.com/atompub
    @Option(name = "-repository_atompub_url", usage = "Repository atompub root URL")
    private String repositoryAtomPubUrl;

    // e.g. "/usr/local/cnx/data";
    @Option(name = "-data_root_dir", usage = "Input data root directory")
    private String dataRootDirectory;

    // e.g. 100
    @Option(name = "-worker_threads", usage = "Number of worker threads")
    private int workerThreadCount = 3;

    @Option(name = "-max_tries", usage = "Max number of item upload attempt")
    private int max_tries = 5;

    @Option(name = "-v", usage = "Verbose mode")
    private boolean verbose = false;

    public MigratorConfiguration(String args[]) {

        CmdLineParser parser = new CmdLineParser(this);
        try {
            // TODO(tal): provide a more graceful error message and usage text.
            parser.parseArgument(args);
            checkArgument(dataRootDirectory != null,
                    "Missing required command line arg: -data_root_dir");
            checkArgument(repositoryAtomPubUrl != null,
                    "Missing required command line arg: -repository_atompub_url");
            checkArgument(workerThreadCount > 0, "-worker_threads should be at least 1");
            checkArgument(max_tries > 0, "-max_tries should be at least 1");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getMaxTries() {
        return max_tries;
    }

    public int getWorkerThreadCount() {
        return workerThreadCount;
    }

    public String getRepositoryAtomPubUrl() {
        // return "http://localhost:8888/atompub";
        return repositoryAtomPubUrl;
    }

    public String getDataRootDirectory() {
        return dataRootDirectory;
    }

    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("data_root_dir ............ [%s]\n", dataRootDirectory));
        sb.append(String.format("repository_atompub_url ... [%s]\n", repositoryAtomPubUrl));
        sb.append(String.format("worker_threads ........... [%d]\n", workerThreadCount));
        sb.append(String.format("verbose .................. [%s]\n", verbose));
        return sb.toString();
    }

}
