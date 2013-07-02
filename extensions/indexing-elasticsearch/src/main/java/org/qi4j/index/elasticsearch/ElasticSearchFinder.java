/*
 * Copyright 2012 Paul Merlin.
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
package org.qi4j.index.elasticsearch;

import java.util.Map;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.grammar.*;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.wrapperQuery;


@Mixins( ElasticSearchFinder.Mixin.class )
public interface ElasticSearchFinder
        extends EntityFinder
{

    class Mixin
            implements EntityFinder
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( ElasticSearchFinder.class );

        @This
        private ElasticSearchSupport support;

        @Override
        public Iterable<EntityReference> findEntities( Class<?> resultType,
                                                       Specification<Composite> whereClause,
                                                       OrderBy[] orderBySegments,
                                                       Integer firstResult, Integer maxResults,
                                                       Map<String, Object> variables )
                throws EntityFinderException
        {
            // Prepare request
            SearchRequestBuilder request = support.client().prepareSearch( support.index() );

            AndFilterBuilder filterBuilder = baseFilters( resultType );
            QueryBuilder queryBuilder = processWhereSpecification( filterBuilder, whereClause, variables );


            request.setQuery( filteredQuery( queryBuilder, filterBuilder ) );
            if ( firstResult != null ) {
                request.setFrom( firstResult );
            }
            if ( maxResults != null ) {
                request.setSize( maxResults );
            } else {
                //request.setSize( Integer.MAX_VALUE ); // TODO Use scrolls?
            }
            if ( orderBySegments != null ) {
                for ( OrderBy order : orderBySegments ) {
                    request.addSort( order.property().toString(),
                                     order.order() == OrderBy.Order.ASCENDING ? SortOrder.ASC : SortOrder.DESC );
                }
            }

            // Log
            LOGGER.debug( "Will search Entities: {}", request );

            // Execute
            SearchResponse response = request.execute().actionGet();

            return Iterables.map( new Function<SearchHit, EntityReference>()
            {
                @Override
                public EntityReference map( SearchHit from )
                {
                    return EntityReference.parseEntityReference( from.id() );
                }

            }, response.getHits() );
        }

        @Override
        public EntityReference findEntity( Class<?> resultType,
                                           Specification<Composite> whereClause,
                                           Map<String, Object> variables )
                throws EntityFinderException
        {
            // Prepare request
            SearchRequestBuilder request = support.client().prepareSearch( support.index() );

            AndFilterBuilder filterBuilder = baseFilters( resultType );
            QueryBuilder queryBuilder = processWhereSpecification( filterBuilder, whereClause, variables );

            request.setQuery( filteredQuery( queryBuilder, filterBuilder ) );
            request.setSize( 1 );

            // Log
            LOGGER.debug( "Will search Entity: {}", request );

            // Execute
            SearchResponse response = request.execute().actionGet();

            if ( response.getHits().totalHits() == 1 ) {
                return EntityReference.parseEntityReference( response.getHits().getAt( 0 ).id() );
            }

            return null;
        }

        @Override
        public long countEntities( Class<?> resultType,
                                   Specification<Composite> whereClause,
                                   Map<String, Object> variables )
                throws EntityFinderException
        {
            // Prepare request
            CountRequestBuilder request = support.client().prepareCount( support.index() );

            AndFilterBuilder filterBuilder = baseFilters( resultType );
            QueryBuilder queryBuilder = processWhereSpecification( filterBuilder, whereClause, variables );

            request.setQuery( filteredQuery( queryBuilder, filterBuilder ) );

            // Log
            LOGGER.debug( "Will count Entities: {}", request );

            // Execute
            CountResponse count = request.execute().actionGet();

            return count.getCount();
        }

        private static AndFilterBuilder baseFilters( Class<?> resultType )
        {
            return andFilter( termFilter( "_types", resultType.getName() ) );
        }

        private QueryBuilder processWhereSpecification( AndFilterBuilder filterBuilder,
                                                        Specification<Composite> spec,
                                                        Map<String, Object> variables )
                throws EntityFinderException
        {
            if ( spec == null ) {
                return matchAllQuery();
            }

            if ( spec instanceof QuerySpecification ) {
                return wrapperQuery( ( ( QuerySpecification ) spec ).query() );
            }

            processSpecification( filterBuilder, spec, variables );
            return matchAllQuery();
        }

        private void processSpecification( FilterBuilder filterBuilder,
                                           Specification<Composite> spec,
                                           Map<String, Object> variables )
                throws EntityFinderException
        {
            if ( spec instanceof BinarySpecification ) {

                BinarySpecification binSpec = ( BinarySpecification ) spec;
                processBinarySpecification( filterBuilder, binSpec, variables );

            } else if ( spec instanceof NotSpecification ) {

                NotSpecification notSpec = ( NotSpecification ) spec;
                processNotSpecification( filterBuilder, notSpec, variables );

            } else if ( spec instanceof EqSpecification || spec instanceof NeSpecification ) {

                ComparisonSpecification<?> compSpec = ( ComparisonSpecification<?> ) spec;
                processEqualitySpecification( filterBuilder, compSpec, variables );

            } else if ( spec instanceof ComparisonSpecification ) {

                ComparisonSpecification<?> compSpec = ( ComparisonSpecification<?> ) spec;
                processComparisonSpecification( filterBuilder, compSpec, variables );

            } else if ( spec instanceof ContainsAllSpecification ) {

                ContainsAllSpecification<?> contAllSpec = ( ContainsAllSpecification ) spec;
                processContainsAllSpecification( filterBuilder, contAllSpec, variables );

            } else if ( spec instanceof ContainsSpecification ) {

                ContainsSpecification<?> contSpec = ( ContainsSpecification ) spec;
                processContainsSpecification( filterBuilder, contSpec, variables );

            } else if ( spec instanceof MatchesSpecification ) {

                MatchesSpecification matchSpec = ( MatchesSpecification ) spec;
                processMatchesSpecification( filterBuilder, matchSpec, variables );

            } else if ( spec instanceof PropertyNotNullSpecification ) {

                PropertyNotNullSpecification<?> propNotNullSpec = ( PropertyNotNullSpecification ) spec;
                processPropertyNotNullSpecification( filterBuilder, propNotNullSpec );

            } else if ( spec instanceof PropertyNullSpecification ) {

                PropertyNullSpecification<?> propNullSpec = ( PropertyNullSpecification ) spec;
                processPropertyNullSpecification( filterBuilder, propNullSpec );

            } else if ( spec instanceof AssociationNotNullSpecification ) {

                AssociationNotNullSpecification<?> assNotNullSpec = ( AssociationNotNullSpecification ) spec;
                processAssociationNotNullSpecification( filterBuilder, assNotNullSpec );

            } else if ( spec instanceof AssociationNullSpecification ) {

                AssociationNullSpecification<?> assNullSpec = ( AssociationNullSpecification ) spec;
                processAssociationNullSpecification( filterBuilder, assNullSpec );

            } else if ( spec instanceof ManyAssociationContainsSpecification ) {

                ManyAssociationContainsSpecification<?> manyAssContSpec = ( ManyAssociationContainsSpecification ) spec;
                processManyAssociationContainsSpecification( filterBuilder, manyAssContSpec, variables );

            } else {

                throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search: "
                                                         + spec.getClass() + ": " + spec );

            }
        }

        private void addFilter( FilterBuilder filter, FilterBuilder into )
        {
            if ( into instanceof AndFilterBuilder ) {
                ( ( AndFilterBuilder ) into ).add( filter );
            } else if ( into instanceof OrFilterBuilder ) {
                ( ( OrFilterBuilder ) into ).add( filter );
            } else {
                throw new UnsupportedOperationException( "FilterBuilder is nor an AndFB nor an OrFB, cannot continue." );
            }
        }

        private String toString( Object value, Map<String, Object> variables )
        {
            if ( value == null ) {
                return null;
            }
            if ( value instanceof Variable ) {
                Variable var = ( Variable ) value;
                Object realValue = variables.get( var.variableName() );
                if ( realValue == null ) {
                    throw new IllegalArgumentException( "Variable " + var.variableName() + " not bound" );
                }
                return realValue.toString();
            }
            return value.toString();
        }

        private void processBinarySpecification( FilterBuilder filterBuilder,
                                                 BinarySpecification spec,
                                                 Map<String, Object> variables )
                throws EntityFinderException
        {
            LOGGER.trace( "Processing BinarySpecification {}", spec );
            Iterable<Specification<Composite>> operands = spec.operands();

            if ( spec instanceof AndSpecification ) {

                AndFilterBuilder andFilterBuilder = new AndFilterBuilder();
                for ( Specification<Composite> operand : operands ) {

                    processSpecification( andFilterBuilder, operand, variables );

                }
                addFilter( andFilterBuilder, filterBuilder );

            } else if ( spec instanceof OrSpecification ) {

                OrFilterBuilder orFilterBuilder = new OrFilterBuilder();
                for ( Specification<Composite> operand : operands ) {

                    processSpecification( orFilterBuilder, operand, variables );

                }
                addFilter( orFilterBuilder, filterBuilder );

            } else {
                throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search: "
                                                         + spec.getClass() + ": " + spec );
            }
        }

        private void processNotSpecification( FilterBuilder filterBuilder,
                                              NotSpecification spec,
                                              Map<String, Object> variables )
                throws EntityFinderException
        {
            LOGGER.trace( "Processing NotSpecification {}", spec );
            AndFilterBuilder operandFilter = new AndFilterBuilder();
            processSpecification( operandFilter, spec.operand(), variables );
            addFilter( notFilter( operandFilter ), filterBuilder );
        }

        private void processEqualitySpecification( FilterBuilder filterBuilder,
                                                   ComparisonSpecification<?> spec,
                                                   Map<String, Object> variables )
                throws EntityFinderException
        {
            LOGGER.trace( "Processing EqualitySpecification {}", spec );
            String name = spec.property().toString();

            if ( spec.value() instanceof ValueComposite ) {

                // Query by complex property "example value"
                ValueComposite value = ( ValueComposite ) spec.value();
                ValueDescriptor valueDescriptor = ( ValueDescriptor ) Qi4j.FUNCTION_DESCRIPTOR_FOR.map( value );

                throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                                                         + "queries, ie. queries by 'example value'." );

            } else {

                // Query by simple property value
                String value = toString( spec.value(), variables );
                if ( spec instanceof EqSpecification ) {

                    addFilter( termFilter( name, value ), filterBuilder );

                } else if ( spec instanceof NeSpecification ) {

                    addFilter( notFilter( termFilter( name, value ) ), filterBuilder );
                }
            }
        }

        private void processComparisonSpecification( FilterBuilder filterBuilder,
                                                     ComparisonSpecification<?> spec,
                                                     Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ComparisonSpecification {}", spec );
            String name = spec.property().toString();
            String value = toString( spec.value(), variables );

            if ( spec instanceof GeSpecification ) {

                addFilter( rangeFilter( name ).from( value ).includeLower( true ), filterBuilder );

            } else if ( spec instanceof GtSpecification ) {

                addFilter( rangeFilter( name ).from( value ).includeLower( false ), filterBuilder );

            } else if ( spec instanceof LeSpecification ) {

                addFilter( rangeFilter( name ).to( value ).includeUpper( true ), filterBuilder );

            } else if ( spec instanceof LtSpecification ) {

                addFilter( rangeFilter( name ).to( value ).includeUpper( false ), filterBuilder );

            } else {

                throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search: "
                                                         + spec.getClass() + ": " + spec );

            }
        }

        private void processContainsAllSpecification( FilterBuilder filterBuilder,
                                                      ContainsAllSpecification<?> spec,
                                                      Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ContainsAllSpecification {}", spec );
            String name = spec.collectionProperty().toString();
            AndFilterBuilder contAllFilter = new AndFilterBuilder();
            for ( Object value : spec.containedValues() ) {
                if ( value instanceof ValueComposite ) {

                    // Query by complex property "example value"
                    ValueComposite valueComposite = ( ValueComposite ) value;
                    ValueDescriptor valueDescriptor = ( ValueDescriptor ) Qi4j.FUNCTION_DESCRIPTOR_FOR.map( valueComposite );
                    throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                                                             + "queries, ie. queries by 'example value'." );

                } else {

                    contAllFilter.add( termFilter( name, toString( value, variables ) ) );

                }
            }
            addFilter( contAllFilter, filterBuilder );
        }

        private void processContainsSpecification( FilterBuilder filterBuilder,
                                                   ContainsSpecification<?> spec,
                                                   Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ContainsSpecification {}", spec );
            String name = spec.collectionProperty().toString();
            if ( spec.value() instanceof ValueComposite ) {

                // Query by complex property "example value"
                ValueComposite value = ( ValueComposite ) spec.value();
                ValueDescriptor valueDescriptor = ( ValueDescriptor ) Qi4j.FUNCTION_DESCRIPTOR_FOR.map( value );
                throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                                                         + "queries, ie. queries by 'example value'." );

            } else {

                String value = toString( spec.value(), variables );
                addFilter( termFilter( name, value ), filterBuilder );

            }
        }

        private void processMatchesSpecification( FilterBuilder filterBuilder,
                                                  MatchesSpecification spec,
                                                  Map<String, Object> variables )
        {
            LOGGER.trace( "Processing MatchesSpecification {}", spec );
            String name = spec.property().toString();
            String regexp = toString( spec.regexp(), variables );
            addFilter( regexpFilter(name , regexp ), filterBuilder );
        }

        private void processPropertyNotNullSpecification( FilterBuilder filterBuilder,
                                                          PropertyNotNullSpecification<?> spec )
        {
            LOGGER.trace( "Processing PropertyNotNullSpecification {}", spec );
            addFilter( existsFilter( spec.property().toString() ), filterBuilder );
        }

        private void processPropertyNullSpecification( FilterBuilder filterBuilder,
                                                       PropertyNullSpecification<?> spec )
        {
            LOGGER.trace( "Processing PropertyNullSpecification {}", spec );
            addFilter( missingFilter( spec.property().toString() ), filterBuilder );
        }

        private void processAssociationNotNullSpecification( FilterBuilder filterBuilder,
                                                             AssociationNotNullSpecification<?> spec )
        {
            LOGGER.trace( "Processing AssociationNotNullSpecification {}", spec );
            addFilter( existsFilter( spec.association().toString() + ".identity" ), filterBuilder );
        }

        private void processAssociationNullSpecification( FilterBuilder filterBuilder,
                                                          AssociationNullSpecification<?> spec )
        {
            LOGGER.trace( "Processing AssociationNullSpecification {}", spec );
            addFilter( missingFilter( spec.association().toString() + ".identity" ), filterBuilder );
        }

        private void processManyAssociationContainsSpecification( FilterBuilder filterBuilder,
                                                                  ManyAssociationContainsSpecification<?> spec,
                                                                  Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ManyAssociationContainsSpecification {}", spec );
            String name = spec.manyAssociation().toString() + ".identity";
            String value = toString( spec.value(), variables );
            addFilter( termFilter( name, value ), filterBuilder );
        }

    }

}
