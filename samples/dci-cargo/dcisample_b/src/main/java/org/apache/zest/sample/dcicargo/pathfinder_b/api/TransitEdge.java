/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.pathfinder_b.api;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents an edge in a path through a graph,
 * describing the route of a cargo.
 */
public final class TransitEdge implements Serializable
{

    private final String voyageNumber;
    private final String fromUnLocode;
    private final String toUnLocode;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    /**
     * Constructor.
     *
     * @param voyageNumber The voyage number
     * @param fromUnLocode The UNLO code of the originating port
     * @param toUnLocode  The UNLO code of the destination port
     * @param fromDate The starting date of the voyage
     * @param toDate The last day of the voyage
     */
    public TransitEdge( final String voyageNumber,
                        final String fromUnLocode,
                        final String toUnLocode,
                        final LocalDate fromDate,
                        final LocalDate toDate
    )
    {
        this.voyageNumber = voyageNumber;
        this.fromUnLocode = fromUnLocode;
        this.toUnLocode = toUnLocode;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public String getVoyageNumber()
    {
        return voyageNumber;
    }

    public String getFromUnLocode()
    {
        return fromUnLocode;
    }

    public String getToUnLocode()
    {
        return toUnLocode;
    }

    public LocalDate getFromDate()
    {
        return fromDate;
    }

    public LocalDate getToDate()
    {
        return toDate;
    }
}