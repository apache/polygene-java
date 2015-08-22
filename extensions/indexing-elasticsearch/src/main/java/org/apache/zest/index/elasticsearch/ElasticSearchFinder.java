/*
 * Copyright 2012-2014 Paul Merlin.
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
package org.apache.zest.index.elasticsearch;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
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
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.query.grammar.AndPredicate;
import org.apache.zest.api.query.grammar.AssociationNotNullPredicate;
import org.apache.zest.api.query.grammar.AssociationNullPredicate;
import org.apache.zest.api.query.grammar.BinaryPredicate;
import org.apache.zest.api.query.grammar.ComparisonPredicate;
import org.apache.zest.api.query.grammar.ContainsAllPredicate;
import org.apache.zest.api.query.grammar.ContainsPredicate;
import org.apache.zest.api.query.grammar.EqPredicate;
import org.apache.zest.api.query.grammar.GePredicate;
import org.apache.zest.api.query.grammar.GtPredicate;
import org.apache.zest.api.query.grammar.LePredicate;
import org.apache.zest.api.query.grammar.LtPredicate;
import org.apache.zest.api.query.grammar.ManyAssociationContainsPredicate;
import org.apache.zest.api.query.grammar.MatchesPredicate;
import org.apache.zest.api.query.grammar.NamedAssociationContainsNamePredicate;
import org.apache.zest.api.query.grammar.NamedAssociationContainsPredicate;
import org.apache.zest.api.query.grammar.NePredicate;
import org.apache.zest.api.query.grammar.Notpredicate;
import org.apache.zest.api.query.grammar.OrPredicate;
import org.apache.zest.api.query.grammar.OrderBy;
import org.apache.zest.api.query.grammar.PropertyNotNullPredicate;
import org.apache.zest.api.query.grammar.PropertyNullPredicate;
import org.apache.zest.api.query.grammar.QuerySpecification;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.functional.Iterables;
import org.apache.zest.index.elasticsearch.ElasticSearchFinderSupport.ComplexTypeSupport;
import org.apache.zest.spi.query.EntityFinder;
import org.apache.zest.spi.query.EntityFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.existsFilter;
import static org.elasticsearch.index.query.FilterBuilders.missingFilter;
import static org.elasticsearch.index.query.FilterBuilders.notFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.FilterBuilders.regexpFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.wrapperQuery;
import static org.apache.zest.index.elasticsearch.ElasticSearchFinderSupport.resolveVariable;

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
        public Iterable<EntityReference> findEntities( Class<?> resultType,
                                                       Predicate<Composite> whereClause,
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

            return Iterables.map( new Function<SearchHit, EntityReference>()
            {
                @Override
                public EntityReference apply( SearchHit from )
                {
                    return EntityReference.parseEntityReference( from.id() );
                }

            }, response.getHits() );
        }

        @Override
        public EntityReference findEntity( Class<?> resultType,
                                           Predicate<Composite> whereClause,
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

            processSpecification( filterBuilder, spec, variables );
            return matchAllQuery();
        }

        private void processSpecification( FilterBuilder filterBuilder,
                                           Predicate<Composite> spec,
                                           Map<String, Object> variables )
            throws EntityFinderException
        {
            if( spec instanceof BinaryPredicate )
            {
                BinaryPredicate binSpec = (BinaryPredicate) spec;
                processBinarySpecification( filterBuilder, binSpec, variables );
            }
            else if( spec instanceof Notpredicate )
            {
                Notpredicate notSpec = (Notpredicate) spec;
                processNotSpecification( filterBuilder, notSpec, variables );
            }
            else if( spec instanceof ComparisonPredicate )
            {
                ComparisonPredicate<?> compSpec = (ComparisonPredicate<?>) spec;
                processComparisonSpecification( filterBuilder, compSpec, variables );
            }
            else if( spec instanceof ContainsAllPredicate )
            {
                ContainsAllPredicate<?> contAllSpec = (ContainsAllPredicate) spec;
                processContainsAllSpecification( filterBuilder, contAllSpec, variables );
            }
            else if( spec instanceof ContainsPredicate )
            {
                ContainsPredicate<?> contSpec = (ContainsPredicate) spec;
                processContainsSpecification( filterBuilder, contSpec, variables );
            }
            else if( spec instanceof MatchesPredicate )
            {
                MatchesPredicate matchSpec = (MatchesPredicate) spec;
                processMatchesSpecification( filterBuilder, matchSpec, variables );
            }
            else if( spec instanceof PropertyNotNullPredicate )
            {
                PropertyNotNullPredicate<?> propNotNullSpec = (PropertyNotNullPredicate) spec;
                processPropertyNotNullSpecification( filterBuilder, propNotNullSpec );
            }
            else if( spec instanceof PropertyNullPredicate )
            {
                PropertyNullPredicate<?> propNullSpec = (PropertyNullPredicate) spec;
                processPropertyNullSpecification( filterBuilder, propNullSpec );
            }
            else if( spec instanceof AssociationNotNullPredicate )
            {
                AssociationNotNullPredicate<?> assNotNullSpec = (AssociationNotNullPredicate) spec;
                processAssociationNotNullSpecification( filterBuilder, assNotNullSpec );
            }
            else if( spec instanceof AssociationNullPredicate )
            {
                AssociationNullPredicate<?> assNullSpec = (AssociationNullPredicate) spec;
                processAssociationNullSpecification( filterBuilder, assNullSpec );
            }
            else if( spec instanceof ManyAssociationContainsPredicate )
            {
                ManyAssociationContainsPredicate<?> manyAssContSpec = (ManyAssociationContainsPredicate) spec;
                processManyAssociationContainsSpecification( filterBuilder, manyAssContSpec, variables );
            }
            else if( spec instanceof NamedAssociationContainsPredicate )
            {

                NamedAssociationContainsPredicate<?> namedAssContSpec = (NamedAssociationContainsPredicate) spec;
                processNamedAssociationContainsSpecification( filterBuilder, namedAssContSpec, variables );

            }
            else if( spec instanceof NamedAssociationContainsNamePredicate )
            {

                NamedAssociationContainsNamePredicate<?> namedAssContNameSpec = (NamedAssociationContainsNamePredicate) spec;
                processNamedAssociationContainsNameSpecification( filterBuilder, namedAssContNameSpec, variables );

            }
            else
            {
                throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search "
                                                         + "(New Query API support missing?): "
                                                         + spec.getClass() + ": " + spec );
            }
        }

        private static void addFilter( FilterBuilder filter, FilterBuilder into )
        {
            if( into instanceof AndFilterBuilder )
            {
                ( (AndFilterBuilder) into ).add( filter );
            }
            else if( into instanceof OrFilterBuilder )
            {
                ( (OrFilterBuilder) into ).add( filter );
            }
            else
            {
                throw new UnsupportedOperationException( "FilterBuilder is nor an AndFB nor an OrFB, cannot continue." );
            }
        }

        private void processBinarySpecification( FilterBuilder filterBuilder,
                                                 BinaryPredicate spec,
                                                 Map<String, Object> variables )
            throws EntityFinderException
        {
            LOGGER.trace( "Processing BinarySpecification {}", spec );
            Iterable<Predicate<Composite>> operands = spec.operands();

            if( spec instanceof AndPredicate )
            {
                AndFilterBuilder andFilterBuilder = new AndFilterBuilder();
                for( Predicate<Composite> operand : operands )
                {
                    processSpecification( andFilterBuilder, operand, variables );
                }
                addFilter( andFilterBuilder, filterBuilder );
            }
            else if( spec instanceof OrPredicate )
            {
                OrFilterBuilder orFilterBuilder = new OrFilterBuilder();
                for( Predicate<Composite> operand : operands )
                {
                    processSpecification( orFilterBuilder, operand, variables );
                }
                addFilter( orFilterBuilder, filterBuilder );
            }
            else
            {
                throw new UnsupportedOperationException( "Binary Query specification is nor an AndSpecification "
                                                         + "nor an OrSpecification, cannot continue." );
            }
        }

        private void processNotSpecification( FilterBuilder filterBuilder,
                                              Notpredicate spec,
                                              Map<String, Object> variables )
            throws EntityFinderException
        {
            LOGGER.trace( "Processing NotSpecification {}", spec );
            AndFilterBuilder operandFilter = new AndFilterBuilder();
            processSpecification( operandFilter, spec.operand(), variables );
            addFilter( notFilter( operandFilter ), filterBuilder );
        }

        private void processComparisonSpecification( FilterBuilder filterBuilder,
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
                addFilter( support.comparison( spec, variables ), filterBuilder );
            }
            else
            {
                // Query by simple property value
                String name = spec.property().toString();
                Object value = resolveVariable( spec.value(), variables );
                if( spec instanceof EqPredicate )
                {
                    addFilter( termFilter( name, value ), filterBuilder );
                }
                else if( spec instanceof NePredicate )
                {
                    addFilter( andFilter( existsFilter( name ),
                                          notFilter( termFilter( name, value ) ) ),
                               filterBuilder );
                }
                else if( spec instanceof GePredicate )
                {
                    addFilter( rangeFilter( name ).gte( value ), filterBuilder );
                }
                else if( spec instanceof GtPredicate )
                {
                    addFilter( rangeFilter( name ).gt( value ), filterBuilder );
                }
                else if( spec instanceof LePredicate )
                {
                    addFilter( rangeFilter( name ).lte( value ), filterBuilder );
                }
                else if( spec instanceof LtPredicate )
                {
                    addFilter( rangeFilter( name ).lt( value ), filterBuilder );
                }
                else
                {
                    throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search "
                                                             + "(New Query API support missing?): "
                                                             + spec.getClass() + ": " + spec );
                }
            }
        }

        private void processContainsAllSpecification( FilterBuilder filterBuilder,
                                                      ContainsAllPredicate<?> spec,
                                                      Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ContainsAllSpecification {}", spec );
            Object firstValue = Iterables.first( spec.containedValues() );
            if( firstValue instanceof ValueComposite )
            {
                // Query by complex property "example value"
                throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                                                         + "queries, ie. queries by 'example value'." );
            }
            else if( COMPLEX_TYPE_SUPPORTS.get( firstValue.getClass() ) != null )
            {
                ComplexTypeSupport support = COMPLEX_TYPE_SUPPORTS.get( firstValue.getClass() );
                addFilter( support.containsAll( spec, variables ), filterBuilder );
            }
            else
            {
                String name = spec.collectionProperty().toString();
                AndFilterBuilder contAllFilter = new AndFilterBuilder();
                for( Object value : spec.containedValues() )
                {
                    contAllFilter.add( termFilter( name, resolveVariable( value, variables ) ) );
                }
                addFilter( contAllFilter, filterBuilder );
            }
        }

        private void processContainsSpecification( FilterBuilder filterBuilder,
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
                addFilter( support.contains( spec, variables ), filterBuilder );
            }
            else
            {
                Object value = resolveVariable( spec.value(), variables );
                addFilter( termFilter( name, value ), filterBuilder );
            }
        }

        private void processMatchesSpecification( FilterBuilder filterBuilder,
                                                  MatchesPredicate spec,
                                                  Map<String, Object> variables )
        {
            LOGGER.trace( "Processing MatchesSpecification {}", spec );
            String name = spec.property().toString();
            String regexp = resolveVariable( spec.regexp(), variables ).toString();
            addFilter( regexpFilter( name, regexp ), filterBuilder );
        }

        private void processPropertyNotNullSpecification( FilterBuilder filterBuilder,
                                                          PropertyNotNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing PropertyNotNullSpecification {}", spec );
            addFilter( existsFilter( spec.property().toString() ), filterBuilder );
        }

        private void processPropertyNullSpecification( FilterBuilder filterBuilder,
                                                       PropertyNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing PropertyNullSpecification {}", spec );
            addFilter( missingFilter( spec.property().toString() ), filterBuilder );
        }

        private void processAssociationNotNullSpecification( FilterBuilder filterBuilder,
                                                             AssociationNotNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing AssociationNotNullSpecification {}", spec );
            addFilter( existsFilter( spec.association().toString() + ".identity" ), filterBuilder );
        }

        private void processAssociationNullSpecification( FilterBuilder filterBuilder,
                                                          AssociationNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing AssociationNullSpecification {}", spec );
            addFilter( missingFilter( spec.association().toString() + ".identity" ), filterBuilder );
        }

        private void processManyAssociationContainsSpecification( FilterBuilder filterBuilder,
                                                                  ManyAssociationContainsPredicate<?> spec,
                                                                  Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ManyAssociationContainsSpecification {}", spec );
            String name = spec.manyAssociation().toString() + ".identity";
            Object value = resolveVariable( spec.value(), variables );
            addFilter( termFilter( name, value ), filterBuilder );
        }

        private void processNamedAssociationContainsSpecification( FilterBuilder filterBuilder,
                                                                   NamedAssociationContainsPredicate<?> spec,
                                                                   Map<String, Object> variables )
        {
            LOGGER.trace( "Processing NamedAssociationContainsSpecification {}", spec );
            String name = spec.namedAssociation().toString() + ".identity";
            Object value = resolveVariable( spec.value(), variables );
            addFilter( termFilter( name, value ), filterBuilder );
        }

        private void processNamedAssociationContainsNameSpecification( FilterBuilder filterBuilder,
                                                                       NamedAssociationContainsNamePredicate<?> spec,
                                                                       Map<String, Object> variables )
        {
            LOGGER.trace( "Processing NamedAssociationContainsNameSpecification {}", spec );
            String name = spec.namedAssociation().toString() + "._named";
            Object value = resolveVariable( spec.name(), variables );
            addFilter( termFilter( name, value ), filterBuilder );
        }
    }

}
