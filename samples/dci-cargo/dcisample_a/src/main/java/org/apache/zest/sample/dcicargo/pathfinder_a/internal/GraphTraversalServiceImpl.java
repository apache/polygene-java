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
package org.apache.zest.sample.dcicargo.pathfinder_a.internal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.apache.zest.sample.dcicargo.pathfinder_a.api.GraphTraversalService;
import org.apache.zest.sample.dcicargo.pathfinder_a.api.TransitEdge;
import org.apache.zest.sample.dcicargo.pathfinder_a.api.TransitPath;

public class GraphTraversalServiceImpl
    implements GraphTraversalService
{
    private GraphDAO dao;
    private Random random;

    public GraphTraversalServiceImpl( GraphDAO dao )
    {
        this.dao = dao;
        this.random = new Random();
    }

    public List<TransitPath> findShortestPath( String originUnLocode, String destinationUnLocode )
    {
        LocalDate date = nextDate( LocalDate.now());

        List<String> allVertices = dao.listLocations();
        allVertices.remove( originUnLocode );
        allVertices.remove( destinationUnLocode );

        final int candidateCount = getRandomNumberOfCandidates();
        final List<TransitPath> candidates = new ArrayList<>( candidateCount );

        for( int i = 0; i < candidateCount; i++ )
        {
            allVertices = getRandomChunkOfLocations( allVertices );
            final List<TransitEdge> transitEdges = new ArrayList<>( allVertices.size() - 1 );
            final String firstLegTo = allVertices.get( 0 );

            LocalDate fromDate = nextDate( date );
            LocalDate toDate = nextDate( fromDate );
            date = nextDate( toDate );

            transitEdges.add( new TransitEdge(
                dao.getVoyageNumber( originUnLocode, firstLegTo ),
                originUnLocode, firstLegTo, fromDate, toDate ) );

            for( int j = 0; j < allVertices.size() - 1; j++ )
            {
                final String curr = allVertices.get( j );
                final String next = allVertices.get( j + 1 );
                fromDate = nextDate( date );
                toDate = nextDate( fromDate );
                date = nextDate( toDate );
                transitEdges.add( new TransitEdge( dao.getVoyageNumber( curr, next ), curr, next, fromDate, toDate ) );
            }

            final String lastLegFrom = allVertices.get( allVertices.size() - 1 );
            fromDate = nextDate( date );
            toDate = nextDate( fromDate );
            transitEdges.add( new TransitEdge(
                dao.getVoyageNumber( lastLegFrom, destinationUnLocode ),
                lastLegFrom, destinationUnLocode, fromDate, toDate ) );

            candidates.add( new TransitPath( transitEdges ) );
        }

        return candidates;
    }

    private LocalDate nextDate( LocalDate date )
    {
        return date.plusDays( 1 );
    }

    private int getRandomNumberOfCandidates()
    {
        return 3 + random.nextInt( 3 );
    }

    private List<String> getRandomChunkOfLocations( List<String> allLocations )
    {
        Collections.shuffle( allLocations );
        final int total = allLocations.size();
        final int chunk = total > 4 ? 1 + new Random().nextInt( 5 ) : total;
        return allLocations.subList( 0, chunk );
    }
}
