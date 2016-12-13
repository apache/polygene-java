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
package org.apache.polygene.index.elasticsearch;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.query.grammar.AndPredicate;
import org.apache.polygene.api.query.grammar.AssociationNotNullPredicate;
import org.apache.polygene.api.query.grammar.AssociationNullPredicate;
import org.apache.polygene.api.query.grammar.BinaryPredicate;
import org.apache.polygene.api.query.grammar.ComparisonPredicate;
import org.apache.polygene.api.query.grammar.ContainsAllPredicate;
import org.apache.polygene.api.query.grammar.ContainsPredicate;
import org.apache.polygene.api.query.grammar.EqPredicate;
import org.apache.polygene.api.query.grammar.GePredicate;
import org.apache.polygene.api.query.grammar.GtPredicate;
import org.apache.polygene.api.query.grammar.LePredicate;
import org.apache.polygene.api.query.grammar.LtPredicate;
import org.apache.polygene.api.query.grammar.ManyAssociationContainsPredicate;
import org.apache.polygene.api.query.grammar.MatchesPredicate;
import org.apache.polygene.api.query.grammar.NamedAssociationContainsNamePredicate;
import org.apache.polygene.api.query.grammar.NamedAssociationContainsPredicate;
import org.apache.polygene.api.query.grammar.NePredicate;
import org.apache.polygene.api.query.grammar.Notpredicate;
import org.apache.polygene.api.query.grammar.OrPredicate;
import org.apache.polygene.api.query.grammar.OrderBy;
import org.apache.polygene.api.query.grammar.PropertyNotNullPredicate;
import org.apache.polygene.api.query.grammar.PropertyNullPredicate;
import org.apache.polygene.api.query.grammar.QuerySpecification;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.index.elasticsearch.ElasticSearchFinderSupport.ComplexTypeSupport;
import org.apache.polygene.spi.query.EntityFinder;
import org.apache.polygene.spi.query.EntityFinderException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.polygene.index.elasticsearch.ElasticSearchFinderSupport.resolveVariable;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.wrapperQuery;

@Mixins( ElasticSearchFinder.Mixin.class )
public interface ElasticSearchFinder
    extends EntityFinder
{
    class Mixin
        implements EntityFinder
    {
        private static final Logger LOGGER = LoggerFactory.getLogger( ElasticSearchFinder.class );
        private static final Map<Class<?>, ComplexTypeSupport> COMPLEX_TYPE_SUPPORTS = new HashMap<>( 0 );

        @This
        private ElasticSearchSupport support;

        @Override
        public Stream<EntityReference> findEntities( Class<?> resultType,
                                                     Predicate<Composite> whereClause,
                                                     List<OrderBy> orderBySegments,
                                                     Integer firstResult,
                                                     Integer maxResults,
                                                     Map<String, Object> variables ) throws EntityFinderException
        {
            // Prepare request
            SearchRequestBuilder request = support.client().prepareSearch( support.index() );

            BoolQueryBuilder baseQueryBuilder = baseQuery( resultType );
            QueryBuilder whereQueryBuilder = processWhereSpecification( baseQueryBuilder, whereClause, variables );

            request.setQuery( boolQuery().must( whereQueryBuilder ).filter( baseQueryBuilder ) );
            if( firstResult != null )
            {
                request.setFrom( firstResult );
            }
            if( maxResults != null )
            {
                request.setSize( maxResults );
            }
            else
            {
                //request.setSize( Integer.MAX_VALUE ); // TODO Use scrolls?
            }
            if( orderBySegments != null )
            {
                for( OrderBy order : orderBySegments )
                {
                    request.addSort( order.property().toString(),
                                     order.order() == OrderBy.Order.ASCENDING ? SortOrder.ASC : SortOrder.DESC );
                }
            }

            // Log
            LOGGER.debug( "Will search Entities: {}", request );

            // Execute
            SearchResponse response = request.execute().actionGet();

            return StreamSupport.stream( response.getHits().spliterator(), false )
                                .map( hit -> EntityReference.parseEntityReference( hit.id() ) );
        }

        @Override
        public EntityReference findEntity( Class<?> resultType,
                                           Predicate<Composite> whereClause,
                                           Map<String, Object> variables )
            throws EntityFinderException
        {
            // Prepare request
            SearchRequestBuilder request = support.client().prepareSearch( support.index() );

            BoolQueryBuilder baseQueryBuilder = baseQuery( resultType );
            QueryBuilder whereQueryBuilder = processWhereSpecification( baseQueryBuilder, whereClause, variables );

            request.setQuery( boolQuery().must( whereQueryBuilder ).filter( baseQueryBuilder ) );
            request.setSize( 1 );

            // Log
            LOGGER.debug( "Will search Entity: {}", request );

            // Execute
            SearchResponse response = request.execute().actionGet();

            if( response.getHits().totalHits() == 1 )
            {
                return EntityReference.parseEntityReference( response.getHits().getAt( 0 ).id() );
            }

            return null;
        }

        @Override
        public long countEntities( Class<?> resultType,
                                   Predicate<Composite> whereClause,
                                   Map<String, Object> variables )
            throws EntityFinderException
        {
            // Prepare request
            SearchRequestBuilder request = support.client().prepareSearch( support.index() ).setSize( 0 );

            BoolQueryBuilder baseQueryBuilder = baseQuery( resultType );
            QueryBuilder whereQueryBuilder = processWhereSpecification( baseQueryBuilder, whereClause, variables );

            request.setQuery( boolQuery().must( whereQueryBuilder ).filter( baseQueryBuilder ) );

            // Log
            LOGGER.debug( "Will count Entities: {}", request );

            // Execute
            SearchResponse count = request.execute().actionGet();

            return count.getHits().getTotalHits();
        }

        private static BoolQueryBuilder baseQuery( Class<?> resultType )
        {
            return boolQuery().must( termQuery( "_types", resultType.getName() ) );
        }

        private QueryBuilder processWhereSpecification( BoolQueryBuilder queryBuilder,
                                                        Predicate<Composite> spec,
                                                        Map<String, Object> variables )
            throws EntityFinderException
        {
            if( spec == null )
            {
                return matchAllQuery();
            }

            if( spec instanceof QuerySpecification )
            {
                return wrapperQuery( ( (QuerySpecification) spec ).query() );
            }

            processSpecification( queryBuilder, spec, variables );
            return matchAllQuery();
        }

        private void processSpecification( BoolQueryBuilder queryBuilder,
                                           Predicate<Composite> spec,
                                           Map<String, Object> variables )
            throws EntityFinderException
        {
            if( spec instanceof BinaryPredicate )
            {
                BinaryPredicate binSpec = (BinaryPredicate) spec;
                processBinarySpecification( queryBuilder, binSpec, variables );
            }
            else if( spec instanceof Notpredicate )
            {
                Notpredicate notSpec = (Notpredicate) spec;
                processNotSpecification( queryBuilder, notSpec, variables );
            }
            else if( spec instanceof ComparisonPredicate )
            {
                ComparisonPredicate<?> compSpec = (ComparisonPredicate<?>) spec;
                processComparisonSpecification( queryBuilder, compSpec, variables );
            }
            else if( spec instanceof ContainsAllPredicate )
            {
                ContainsAllPredicate<?> contAllSpec = (ContainsAllPredicate) spec;
                processContainsAllSpecification( queryBuilder, contAllSpec, variables );
            }
            else if( spec instanceof ContainsPredicate )
            {
                ContainsPredicate<?> contSpec = (ContainsPredicate) spec;
                processContainsSpecification( queryBuilder, contSpec, variables );
            }
            else if( spec instanceof MatchesPredicate )
            {
                MatchesPredicate matchSpec = (MatchesPredicate) spec;
                processMatchesSpecification( queryBuilder, matchSpec, variables );
            }
            else if( spec instanceof PropertyNotNullPredicate )
            {
                PropertyNotNullPredicate<?> propNotNullSpec = (PropertyNotNullPredicate) spec;
                processPropertyNotNullSpecification( queryBuilder, propNotNullSpec );
            }
            else if( spec instanceof PropertyNullPredicate )
            {
                PropertyNullPredicate<?> propNullSpec = (PropertyNullPredicate) spec;
                processPropertyNullSpecification( queryBuilder, propNullSpec );
            }
            else if( spec instanceof AssociationNotNullPredicate )
            {
                AssociationNotNullPredicate<?> assNotNullSpec = (AssociationNotNullPredicate) spec;
                processAssociationNotNullSpecification( queryBuilder, assNotNullSpec );
            }
            else if( spec instanceof AssociationNullPredicate )
            {
                AssociationNullPredicate<?> assNullSpec = (AssociationNullPredicate) spec;
                processAssociationNullSpecification( queryBuilder, assNullSpec );
            }
            else if( spec instanceof ManyAssociationContainsPredicate )
            {
                ManyAssociationContainsPredicate<?> manyAssContSpec = (ManyAssociationContainsPredicate) spec;
                processManyAssociationContainsSpecification( queryBuilder, manyAssContSpec, variables );
            }
            else if( spec instanceof NamedAssociationContainsPredicate )
            {

                NamedAssociationContainsPredicate<?> namedAssContSpec = (NamedAssociationContainsPredicate) spec;
                processNamedAssociationContainsSpecification( queryBuilder, namedAssContSpec, variables );
            }
            else if( spec instanceof NamedAssociationContainsNamePredicate )
            {

                NamedAssociationContainsNamePredicate<?> namedAssContNameSpec
                    = (NamedAssociationContainsNamePredicate) spec;
                processNamedAssociationContainsNameSpecification( queryBuilder, namedAssContNameSpec, variables );
            }
            else
            {
                throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search "
                                                         + "(New Query API support missing?): "
                                                         + spec.getClass() + ": " + spec );
            }
        }

        private void processBinarySpecification( BoolQueryBuilder queryBuilder,
                                                 BinaryPredicate spec,
                                                 Map<String, Object> variables )
            throws EntityFinderException
        {
            LOGGER.trace( "Processing BinarySpecification {}", spec );
            Iterable<Predicate<Composite>> operands = spec.operands();

            if( spec instanceof AndPredicate )
            {
                BoolQueryBuilder andBuilder = boolQuery();
                for( Predicate<Composite> operand : operands )
                {
                    processSpecification( andBuilder, operand, variables );
                }
                queryBuilder.must( andBuilder );
            }
            else if( spec instanceof OrPredicate )
            {
                BoolQueryBuilder orBuilder = boolQuery();
                for( Predicate<Composite> operand : operands )
                {
                    BoolQueryBuilder shouldBuilder = boolQuery();
                    processSpecification( shouldBuilder, operand, variables );
                    orBuilder.should( shouldBuilder );
                }
                orBuilder.minimumNumberShouldMatch( 1 );
                queryBuilder.must( orBuilder );
            }
            else
            {
                throw new UnsupportedOperationException( "Binary Query specification is nor an AndSpecification "
                                                         + "nor an OrSpecification, cannot continue." );
            }
        }

        private void processNotSpecification( BoolQueryBuilder queryBuilder,
                                              Notpredicate spec,
                                              Map<String, Object> variables )
            throws EntityFinderException
        {
            LOGGER.trace( "Processing NotSpecification {}", spec );
            BoolQueryBuilder operandBuilder = boolQuery();
            processSpecification( operandBuilder, spec.operand(), variables );
            queryBuilder.mustNot( operandBuilder );
        }

        private void processComparisonSpecification( BoolQueryBuilder queryBuilder,
                                                     ComparisonPredicate<?> spec,
                                                     Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ComparisonSpecification {}", spec );

            if( spec.value() instanceof ValueComposite )
            {
                // Query by "example value"
                throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                                                         + "queries, ie. queries by 'example value'." );
            }
            else if( COMPLEX_TYPE_SUPPORTS.get( spec.value().getClass() ) != null )
            {
                // Query on complex type property
                ComplexTypeSupport support = COMPLEX_TYPE_SUPPORTS.get( spec.value().getClass() );
                queryBuilder.must( support.comparison( spec, variables ) );
            }
            else
            {
                // Query by simple property value
                String name = spec.property().toString();
                Object value = resolveVariable( spec.value(), variables );
                if( spec instanceof EqPredicate )
                {
                    queryBuilder.must( termQuery( name, value ) );
                }
                else if( spec instanceof NePredicate )
                {
                    queryBuilder.must( existsQuery( name ) ).mustNot( termQuery( name, value ) );
                }
                else if( spec instanceof GePredicate )
                {
                    queryBuilder.must( rangeQuery( name ).gte( value ) );
                }
                else if( spec instanceof GtPredicate )
                {
                    queryBuilder.must( rangeQuery( name ).gt( value ) );
                }
                else if( spec instanceof LePredicate )
                {
                    queryBuilder.must( rangeQuery( name ).lte( value ) );
                }
                else if( spec instanceof LtPredicate )
                {
                    queryBuilder.must( rangeQuery( name ).lt( value ) );
                }
                else
                {
                    throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search "
                                                             + "(New Query API support missing?): "
                                                             + spec.getClass() + ": " + spec );
                }
            }
        }

        private void processContainsAllSpecification( BoolQueryBuilder queryBuilder,
                                                      ContainsAllPredicate<?> spec,
                                                      Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ContainsAllSpecification {}", spec );
            Collection<?> values = spec.containedValues();
            if( values.isEmpty() )
            {
                // Ignore empty contains all spec
                return;
            }
            Object firstValue = values.iterator().next();
            if( firstValue instanceof ValueComposite )
            {
                // Query by complex property "example value"
                throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                                                         + "queries, ie. queries by 'example value'." );
            }
            else if( COMPLEX_TYPE_SUPPORTS.get( firstValue.getClass() ) != null )
            {
                ComplexTypeSupport support = COMPLEX_TYPE_SUPPORTS.get( firstValue.getClass() );
                queryBuilder.must( support.containsAll( spec, variables ) );
            }
            else
            {
                String name = spec.collectionProperty().toString();
                BoolQueryBuilder contAllBuilder = boolQuery();
                for( Object value : values )
                {
                    contAllBuilder.must( termQuery( name, resolveVariable( value, variables ) ) );
                }
                queryBuilder.must( contAllBuilder );
            }
        }

        private void processContainsSpecification( BoolQueryBuilder queryBuilder,
                                                   ContainsPredicate<?> spec,
                                                   Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ContainsSpecification {}", spec );
            String name = spec.collectionProperty().toString();
            if( spec.value() instanceof ValueComposite )
            {
                // Query by complex property "example value"
                throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                                                         + "queries, ie. queries by 'example value'." );
            }
            else if( COMPLEX_TYPE_SUPPORTS.get( spec.value().getClass() ) != null )
            {
                ComplexTypeSupport support = COMPLEX_TYPE_SUPPORTS.get( spec.value().getClass() );
                queryBuilder.must( support.contains( spec, variables ) );
            }
            else
            {
                Object value = resolveVariable( spec.value(), variables );
                queryBuilder.must( termQuery( name, value ) );
            }
        }

        private void processMatchesSpecification( BoolQueryBuilder queryBuilder,
                                                  MatchesPredicate spec,
                                                  Map<String, Object> variables )
        {
            LOGGER.trace( "Processing MatchesSpecification {}", spec );
            String name = spec.property().toString();
            String regexp = resolveVariable( spec.regexp(), variables ).toString();
            queryBuilder.must( regexpQuery( name, regexp ) );
        }

        private void processPropertyNotNullSpecification( BoolQueryBuilder queryBuilder,
                                                          PropertyNotNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing PropertyNotNullSpecification {}", spec );
            queryBuilder.must( existsQuery( spec.property().toString() ) );
        }

        private void processPropertyNullSpecification( BoolQueryBuilder queryBuilder,
                                                       PropertyNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing PropertyNullSpecification {}", spec );
            queryBuilder.mustNot( existsQuery( ( spec.property().toString() ) ) );
        }

        private void processAssociationNotNullSpecification( BoolQueryBuilder queryBuilder,
                                                             AssociationNotNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing AssociationNotNullSpecification {}", spec );
            queryBuilder.must( existsQuery( spec.association().toString() + ".identity" ) );
        }

        private void processAssociationNullSpecification( BoolQueryBuilder queryBuilder,
                                                          AssociationNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing AssociationNullSpecification {}", spec );
            queryBuilder.mustNot( existsQuery( ( spec.association().toString() + ".identity" ) ) );
        }

        private void processManyAssociationContainsSpecification( BoolQueryBuilder queryBuilder,
                                                                  ManyAssociationContainsPredicate<?> spec,
                                                                  Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ManyAssociationContainsSpecification {}", spec );
            String name = spec.manyAssociation().toString() + ".identity";
            Object value = resolveVariable( spec.value(), variables );
            queryBuilder.must( termQuery( name, value ) );
        }

        private void processNamedAssociationContainsSpecification( BoolQueryBuilder queryBuilder,
                                                                   NamedAssociationContainsPredicate<?> spec,
                                                                   Map<String, Object> variables )
        {
            LOGGER.trace( "Processing NamedAssociationContainsSpecification {}", spec );
            String name = spec.namedAssociation().toString() + ".identity";
            Object value = resolveVariable( spec.value(), variables );
            queryBuilder.must( termQuery( name, value ) );
        }

        private void processNamedAssociationContainsNameSpecification( BoolQueryBuilder queryBuilder,
                                                                       NamedAssociationContainsNamePredicate<?> spec,
                                                                       Map<String, Object> variables )
        {
            LOGGER.trace( "Processing NamedAssociationContainsNameSpecification {}", spec );
            String name = spec.namedAssociation().toString() + "._named";
            Object value = resolveVariable( spec.name(), variables );
            queryBuilder.must( termQuery( name, value ) );
        }
    }
}
