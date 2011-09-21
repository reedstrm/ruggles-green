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

/**
 * Represents work queue momentary statistics.
 * 
 * @author tal
 */
public class WorkQueueStats {

    private final int pendingItemCount;
    private final int inProgressItemCount;
    private final int completedItemCount;

    public WorkQueueStats(int pendingItemCount, int inProgressItemCount, int completedItems) {
        this.pendingItemCount = pendingItemCount;
        this.inProgressItemCount = inProgressItemCount;
        this.completedItemCount = completedItems;
    }

    public int getPendingItemCount() {
        return pendingItemCount;
    }

    public int getInProgressItemCount() {
        return inProgressItemCount;
    }

    public int getCompletedItemCount() {
        return completedItemCount;
    }

    public int getNonCompletedItemCount() {
        return pendingItemCount + inProgressItemCount;
    }

    @Override
    public String toString() {
        return String.format("completed: %d, in_progress: %d, pending: %d", completedItemCount,
                inProgressItemCount, pendingItemCount);
    }

}
