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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * Provides a thread safe counter set to track migrator statistics.
 * 
 * @author tal
 */
public class CounterSet {

    /** Holder for a counter value */
    private static class Count {
        private int value = 0;
    }

    /** Map from counter names to counter value holders */
    private final Map<String, Count> counters = Maps.newHashMap();

    public CounterSet() {
    }

    /**
     * Increment given counter by given delta.
     * 
     * If counter does not exists, it is created with initial value = delta.
     */
    public void increment(String counterName, int delta) {
        checkArgument(delta >= 0);
        synchronized (counters) {
            Count count = counters.get(counterName);
            if (count == null) {
                count = new Count();
                counters.put(counterName, count);
            }
            count.value += delta;
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        synchronized (counters) {
            final List<String> counterNameList = new ArrayList<String>(counters.keySet());
            java.util.Collections.sort(counterNameList, String.CASE_INSENSITIVE_ORDER);
            // Find length of longest name
            int maxNameLength = 0;
            for (String counterName : counterNameList) {
                maxNameLength = Math.max(maxNameLength, counterName.length());
            }
            // Generate counter lines
            for (String counterName : counterNameList) {
                builder.append(String.format("%s [%8d]\n",
                        Strings.padEnd(counterName, maxNameLength, ' '),
                        counters.get(counterName).value));
            }
        }
        return builder.toString();
    }
}
