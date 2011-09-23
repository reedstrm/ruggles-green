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
package org.cnx.migrator.util;


/**
 * Measures and report elapsed time.
 * 
 * @author tal
 */
public class Timer {

    private final long startTimeMillies = System.currentTimeMillis();

    public Timer() {}

    public long timeElapsed() {
        return System.currentTimeMillis() - startTimeMillies;
    }

    @Override
    public String toString() {
        // TODO(tal): use off the shelf time formatter?
        int seconds = (int)((timeElapsed() + 500)/ 1000);
        final int hours = seconds / 3600;
        seconds -= hours * 3600;
        final int minutes = seconds / 60;
        seconds -= minutes * 60;
        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(String.format("%dh", hours));
        }
        builder.append(String.format("%dm%ds", minutes, seconds));
        return builder.toString();
    }
}
