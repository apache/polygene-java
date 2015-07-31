/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.index.solr.internal;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.QuerySpecification;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.index.solr.EmbeddedSolrService;
import org.qi4j.index.solr.SolrSearch;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
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
    public Iterable<EntityReference> findEntities( Class<?> resultType, @Optional Specification<Composite> whereClause, @Optional OrderBy[] orderBySegments, @Optional Integer firstResult, @Optional Integer maxResults, Map<String, Object> variables ) throws EntityFinderException
    {
        try
        {
            QuerySpecification expr = (QuerySpecification) whereClause;

            SolrServer server = solr.solrServer();

            NamedList<Object> list = new NamedList<Object>();

            list.add( "q", expr.query() );
            list.add( "rows", maxResults != 0 ? maxResults : 10000 );
            list.add( "start", firstResult );

            if( orderBySegments != null && orderBySegments.length > 0 )
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

            List<EntityReference> references = new ArrayList<EntityReference>( results.size() );
            for( SolrDocument result : results )
            {
                references.add( EntityReference.parseEntityReference( result.getFirstValue( "id" ).toString() ) );
            }
            return references;

        } catch( SolrServerException e )
        {
            throw new EntityFinderException( e );
        }
    }

    @Override
    public EntityReference findEntity( Class<?> resultType, @Optional Specification<Composite> whereClause, Map<String, Object> variables ) throws EntityFinderException
    {
        Iterator<EntityReference> iter = findEntities( resultType, whereClause, null, 0, 1, variables ).iterator();

        if( iter.hasNext() )
            return iter.next();
        else
            return null;
    }

    @Override
    public long countEntities( Class<?> resultType, @Optional Specification<Composite> whereClause, Map<String, Object> variables ) throws EntityFinderException
    {
        return Iterables.count( findEntities( resultType, whereClause, null, 0, 1, variables ) );
    }

    @Override
    public SolrDocumentList search( String queryString ) throws SolrServerException
    {
        SolrServer server = solr.solrServer();

        NamedList<Object> list = new NamedList<Object>();

        list.add( "q", queryString );

        QueryResponse query = server.query( SolrParams.toSolrParams( list ) );
        return query.getResults();
    }
}
