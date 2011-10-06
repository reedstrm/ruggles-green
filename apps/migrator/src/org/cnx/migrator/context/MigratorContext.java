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
package org.cnx.migrator.context;

import static com.google.common.base.Preconditions.checkNotNull;

import org.cnx.atompubclient2.CnxClient;

/**
 * Migration session context.
 * <p>
 * Thread safe.
 * 
 * @author tal
 */
public class MigratorContext {

    private final MigratorConfiguration config;
    private final CnxClient client;
    private final MigratorSummary summary = new MigratorSummary();
    private final CounterSet counterSet = new CounterSet();

    public MigratorContext(MigratorConfiguration config, CnxClient client) {
        this.config = checkNotNull(config);
        this.client = checkNotNull(client);
    }

    public MigratorConfiguration getConfig() {
        return config;
    }

    public CnxClient getCnxClient() {
        return client;
    }

    public MigratorSummary getSummary() {
        return summary;
    }

    public CounterSet getCounterSet() {
        return counterSet;
    }

    public void addSummaryMessage(String format, Object... args) {
        summary.addMessage(format, args);
    }

    public void incrementCounter(String counterName, int delta) {
        counterSet.increment(counterName, delta);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CONFIG:\n");
        builder.append(config.toString());
        builder.append("\n");

        builder.append("COUNTERS:\n");
        builder.append(counterSet.toString());
        builder.append("\n");

        builder.append("SUMMARY MESSAGES:\n");
        builder.append(summary.toString());
        builder.append("\n");

        return builder.toString();
    }
}
