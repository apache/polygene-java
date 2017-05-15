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

package org.apache.polygene.index.solr.internal;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.query.grammar.OrderBy;
import org.apache.polygene.api.query.grammar.QuerySpecification;
import org.apache.polygene.index.solr.EmbeddedSolrService;
import org.apache.polygene.index.solr.SolrSearch;
import org.apache.polygene.spi.query.EntityFinder;
import org.apache.polygene.spi.query.EntityFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAVADOC
 */
public class SolrEntityQueryMixin
        implements EntityFinder, SolrSearch
{
    @Service
    private EmbeddedSolrService solr;

    private Logger logger = LoggerFactory.getLogger( SolrEntityQueryMixin.class );

    @Override
    public Stream<EntityReference> findEntities( Class<?> resultType,
                                                 Predicate<Composite> whereClause,
                                                 List<OrderBy> orderBySegments,
                                                 Integer firstResult,
                                                 Integer maxResults,
                                                 Map<String, Object> variables ) throws EntityFinderException
    {
        try
        {
            QuerySpecification expr = (QuerySpecification) whereClause;

            SolrServer server = solr.solrServer();

            NamedList<Object> list = new NamedList<>();

            list.add( "q", expr.query() );
            list.add( "rows", maxResults != 0 ? maxResults : 10000 );
            list.add( "start", firstResult );

            if( orderBySegments != null && orderBySegments.size() > 0 )
            {
                for( OrderBy orderBySegment : orderBySegments )
                {
                    String propName = ((Member)orderBySegment.property().accessor()).getName() + "_for_sort";
                    String order = orderBySegment.order() == OrderBy.Order.ASCENDING ? "asc" : "desc";
                    list.add( "sort", propName + " " + order );

                }
            }

            SolrParams solrParams = SolrParams.toSolrParams( list );
            logger.debug( "Search:" + list.toString() );

            QueryResponse query = server.query( solrParams );

            SolrDocumentList results = query.getResults();

            List<EntityReference> references = new ArrayList<>( results.size() );
            for( SolrDocument result : results )
            {
                references.add( EntityReference.parseEntityReference( result.getFirstValue( "id" ).toString() ) );
            }
            return references.stream();

        } catch( SolrServerException e )
        {
            throw new EntityFinderException( e );
        }
    }

    @Override
    public EntityReference findEntity( Class<?> resultType, @Optional Predicate<Composite> whereClause, Map<String, Object> variables ) throws EntityFinderException
    {
        return findEntities( resultType, whereClause, null, 0, 1, variables )
            .findFirst().orElse( null );
    }

    @Override
    public long countEntities( Class<?> resultType, @Optional Predicate<Composite> whereClause, Map<String, Object> variables ) throws EntityFinderException
    {
        return findEntities( resultType, whereClause, null, 0, 1, variables )
            .count();
    }

    @Override
    public SolrDocumentList search( String queryString ) throws SolrServerException
    {
        SolrServer server = solr.solrServer();

        NamedList<Object> list = new NamedList<>();

        list.add( "q", queryString );

        QueryResponse query = server.query( SolrParams.toSolrParams( list ) );
        return query.getResults();
    }
}
