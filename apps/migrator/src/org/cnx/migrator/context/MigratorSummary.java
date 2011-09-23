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

import java.util.ArrayList;

/**
 * Holds summary messages collected during the migration session.
 * <p>
 * Thread safe.
 * 
 * @author tal
 */
public class MigratorSummary {

    private final ArrayList<String> messages = new ArrayList<String>();

    public MigratorSummary() {
    }

    public void addMessage(String format, Object... args) {
        final String message = String.format(format, args);
        synchronized (messages) {
            messages.add(message);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        synchronized (messages) {
            for (String message : messages) {
                builder.append("* ");
                builder.append(message);
                builder.append('\n');
            }
        }
        return builder.toString();
    }
}
