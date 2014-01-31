/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.pathfinder_b.api;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents an edge in a path through a graph,
 * describing the route of a cargo.
 */
public final class TransitEdge implements Serializable
{

    private final String voyageNumber;
    private final String fromUnLocode;
    private final String toUnLocode;
    private final Date fromDate;
    private final Date toDate;

    /**
     * Constructor.
     *
     * @param voyageNumber
     * @param fromUnLocode
     * @param toUnLocode
     * @param fromDate
     * @param toDate
     */
    public TransitEdge( final String voyageNumber,
                        final String fromUnLocode,
                        final String toUnLocode,
                        final Date fromDate,
                        final Date toDate
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

    public Date getFromDate()
    {
        return fromDate;
    }

    public Date getToDate()
    {
        return toDate;
    }
}