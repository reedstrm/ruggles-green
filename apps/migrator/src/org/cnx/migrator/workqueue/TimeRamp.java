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

package org.cnx.migrator.workqueue;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;

/**
 * Provides a controlled increasing value over a time period. Used to ramp up the migration
 * throughput.
 * 
 * @author tal
 */
public class TimeRamp {

    /** Function value at the start of the ramp up period */
    private final int startValue;
    /** Function value at the end of the ramp up period */
    private final int endValue;
    /** The time in milliseconds to ramp up from start to end value */
    private final long timePeriodMillies;
    /** The system time in which the ramp up started. */
    private final long startTimeMillis;

    // Value growth factor per 1 millisecond. The ramp up function is
    // value(dt) = start * (k ^ dt). This provides an exponential growth.
    private final double k;

    public TimeRamp(int startValue, int endValue, int timePeriodMillies) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.timePeriodMillies = timePeriodMillies;
        this.startTimeMillis = System.currentTimeMillis();

        checkArgument(startValue > 0, "Invalid start value: %s", startValue);
        checkArgument(endValue >= startValue, "Invalid end value: %s", endValue);
        checkArgument(timePeriodMillies > 0, "Invalid time period: %s", timePeriodMillies);

        this.k = Math.exp(Math.log((double) endValue / (double) startValue) / timePeriodMillies);
    }

    /** Get the current value based on the time ramp function */
    public int getValue() {
        final long timeSinceStartMillies = System.currentTimeMillis() - startTimeMillis;
        return getValueAtTimedif(timeSinceStartMillies);
    }

    /** Get the ramp value function at a given time diff from starting time */
    @VisibleForTesting
    int getValueAtTimedif(long timeSinceStartMillies) {
        if (timeSinceStartMillies == 0) {
            return startValue;
        }
        if (timeSinceStartMillies >= timePeriodMillies) {
            return endValue;
        }
        return (int) ((double) startValue * Math.pow(k, timeSinceStartMillies));
    }
}
