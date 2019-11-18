/*
 * Copyright (c) 2016 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.client.java.search.result;

import java.time.Duration;

public class SearchMetrics {

    private final long took;
    private final long totalRows;
    private final double maxScore;
    private final long successPartitionCount;
    private final long errorPartitionCount;

    public SearchMetrics(long took, long totalRows, double maxScore, final long successPartitionCount,
                         final long errorPartitionCount) {
        this.took = took;
        this.totalRows = totalRows;
        this.maxScore = maxScore;
        this.successPartitionCount = successPartitionCount;
        this.errorPartitionCount = errorPartitionCount;
    }

    public Duration took() {
        return Duration.ofNanos(this.took);
    }

    public long totalRows() {
        return this.totalRows;
    }

    public double maxScore() {
        return this.maxScore;
    }

    public long successPartitionCount() {
        return successPartitionCount;
    }

    public long errorPartitionCount() {
        return errorPartitionCount;
    }

    public long totalPartitionCount() {
        return successPartitionCount + errorPartitionCount;
    }

    @Override
    public String toString() {
        return "SearchMetrics{" +
          "took=" + took +
          ", totalRows=" + totalRows +
          ", maxScore=" + maxScore +
          ", successPartitionCount=" + successPartitionCount +
          ", errorPartitionCount=" + errorPartitionCount +
          '}';
    }
}
