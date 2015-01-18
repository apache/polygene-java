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
package org.qi4j.index.elasticsearch;

import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.query.grammar.*;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.ElasticSearchFinderSupport.*;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinder;
import org.qi4j.index.elasticsearch.extensions.spatial.functions.convert.ConvertFinderSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates.PredicateFinderSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.InternalUtils;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.qi4j.index.elasticsearch.ElasticSearchFinderSupport.*;
import static org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinder.*;

@Mixins( ElasticSearchFinder.Mixin.class )
public interface ElasticSearchFinder
    extends EntityFinder
{
    class Mixin
        implements EntityFinder
    {
        private static final Logger LOGGER = LoggerFactory.getLogger( ElasticSearchFinder.class );

        private static final Map<Class<?>, ComplexTypeSupport> COMPLEX_TYPE_SUPPORTS = new HashMap<>( 9 );
        public static final Map<Class<?>, SpatialQuerySpecSupport> EXTENDED_SPEC_SUPPORTS = new HashMap<>( 2 );

        // Spec Support
        static
        {
            SpatialQuerySpecSupport spatialSpecSupport = new SpatialSpecSupport();
            EXTENDED_SPEC_SUPPORTS.put(SpatialPredicatesSpecification.class, spatialSpecSupport);
            EXTENDED_SPEC_SUPPORTS.put(SpatialConvertSpecification.class, spatialSpecSupport);
        }
        // Type Support
        static
        {
            ComplexTypeSupport spatialTypeSupport = new ElasticSearchSpatialFinder.SpatialTypeSupport();
            COMPLEX_TYPE_SUPPORTS.put( TGeometry.class, spatialTypeSupport );
            COMPLEX_TYPE_SUPPORTS.put( TPoint.class, spatialTypeSupport );
            COMPLEX_TYPE_SUPPORTS.put( TMultiPoint.class, spatialTypeSupport );
            COMPLEX_TYPE_SUPPORTS.put( TLineString.class, spatialTypeSupport );
            COMPLEX_TYPE_SUPPORTS.put( TLinearRing.class, spatialTypeSupport );
            COMPLEX_TYPE_SUPPORTS.put( TPolygon.class, spatialTypeSupport );
            COMPLEX_TYPE_SUPPORTS.put( TMultiPolygon.class, spatialTypeSupport );
            COMPLEX_TYPE_SUPPORTS.put( TFeature.class, spatialTypeSupport );
            COMPLEX_TYPE_SUPPORTS.put( TFeatureCollection.class, spatialTypeSupport );
        }



        @This
        private ElasticSearchSupport support;

        @Structure
        private Module module;

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
                for( OrderBy orderBySegment : orderBySegments ) {

                    if (COMPLEX_TYPE_SUPPORTS.get(InternalUtils.classOfPropertyType(orderBySegment.property())) != null) {
                        COMPLEX_TYPE_SUPPORTS.get(InternalUtils.classOfPropertyType(orderBySegment.property())).support(module, support).orderBy(request, whereClause, orderBySegment, variables);
                    } else {
                        request.addSort(orderBySegment.property().toString(),
                                orderBySegment.order() == OrderBy.Order.ASCENDING ? SortOrder.ASC : SortOrder.DESC);
                    }
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

            if( response.getHits().totalHits() == 1 )
            {
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
                                           Specification<Composite> spec,
                                           Map<String, Object> variables )
            throws EntityFinderException
        {
            if( spec instanceof BinarySpecification )
            {
                BinarySpecification binSpec = (BinarySpecification) spec;
                processBinarySpecification( filterBuilder, binSpec, variables );
            }
            else if( spec instanceof NotSpecification )
            {
                NotSpecification notSpec = (NotSpecification) spec;
                processNotSpecification( filterBuilder, notSpec, variables );
            }
            else if( spec instanceof ComparisonSpecification )
            {
                ComparisonSpecification<?> compSpec = (ComparisonSpecification<?>) spec;
                processComparisonSpecification( filterBuilder, compSpec, variables );
            }
            else if( spec instanceof ContainsAllSpecification )
            {
                ContainsAllSpecification<?> contAllSpec = (ContainsAllSpecification) spec;
                processContainsAllSpecification( filterBuilder, contAllSpec, variables );
            }
            else if( spec instanceof ContainsSpecification )
            {
                ContainsSpecification<?> contSpec = (ContainsSpecification) spec;
                processContainsSpecification( filterBuilder, contSpec, variables );
            }
            else if( spec instanceof MatchesSpecification )
            {
                MatchesSpecification matchSpec = (MatchesSpecification) spec;
                processMatchesSpecification( filterBuilder, matchSpec, variables );
            }
            else if( spec instanceof PropertyNotNullSpecification )
            {
                PropertyNotNullSpecification<?> propNotNullSpec = (PropertyNotNullSpecification) spec;
                processPropertyNotNullSpecification( filterBuilder, propNotNullSpec );
            }
            else if( spec instanceof PropertyNullSpecification )
            {
                PropertyNullSpecification<?> propNullSpec = (PropertyNullSpecification) spec;
                processPropertyNullSpecification( filterBuilder, propNullSpec );
            }
            else if( spec instanceof AssociationNotNullSpecification )
            {
                AssociationNotNullSpecification<?> assNotNullSpec = (AssociationNotNullSpecification) spec;
                processAssociationNotNullSpecification( filterBuilder, assNotNullSpec );
            }
            else if( spec instanceof AssociationNullSpecification )
            {
                AssociationNullSpecification<?> assNullSpec = (AssociationNullSpecification) spec;
                processAssociationNullSpecification( filterBuilder, assNullSpec );
            }
            else if( spec instanceof ManyAssociationContainsSpecification )
            {
                ManyAssociationContainsSpecification<?> manyAssContSpec = (ManyAssociationContainsSpecification) spec;
                processManyAssociationContainsSpecification( filterBuilder, manyAssContSpec, variables );
            }
            else if( spec instanceof NamedAssociationContainsSpecification )
            {

                NamedAssociationContainsSpecification<?> namedAssContSpec = (NamedAssociationContainsSpecification) spec;
                processNamedAssociationContainsSpecification( filterBuilder, namedAssContSpec, variables );

            }
            else if( spec instanceof NamedAssociationContainsNameSpecification )
            {

                NamedAssociationContainsNameSpecification<?> namedAssContNameSpec = (NamedAssociationContainsNameSpecification) spec;
                processNamedAssociationContainsNameSpecification( filterBuilder, namedAssContNameSpec, variables );

            }
            else if( spec instanceof SpatialPredicatesSpecification )
            {
                SpatialPredicatesSpecification<?> spatialPredicatesSpec = (SpatialPredicatesSpecification)spec;
                processSpatialPredicatesSpecification(filterBuilder, spatialPredicatesSpec, variables );
            }
            else if( spec instanceof SpatialConvertSpecification )
            {
                SpatialConvertSpecification<?> spatialConvertSpec = (SpatialConvertSpecification)spec;
                processSpatialConvertSpecification(filterBuilder, spatialConvertSpec, variables);
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
                                                 BinarySpecification spec,
                                                 Map<String, Object> variables )
            throws EntityFinderException
        {
            LOGGER.trace( "Processing BinarySpecification {}", spec );
            Iterable<Specification<Composite>> operands = spec.operands();

            if( spec instanceof AndSpecification )
            {
                AndFilterBuilder andFilterBuilder = new AndFilterBuilder();
                for( Specification<Composite> operand : operands )
                {
                    processSpecification( andFilterBuilder, operand, variables );
                }
                addFilter( andFilterBuilder, filterBuilder );
            }
            else if( spec instanceof OrSpecification )
            {
                OrFilterBuilder orFilterBuilder = new OrFilterBuilder();
                for( Specification<Composite> operand : operands )
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
                                              NotSpecification spec,
                                              Map<String, Object> variables )
            throws EntityFinderException
        {
            LOGGER.trace( "Processing NotSpecification {}", spec );
            AndFilterBuilder operandFilter = new AndFilterBuilder();
            processSpecification( operandFilter, spec.operand(), variables );
            addFilter( notFilter( operandFilter ), filterBuilder );
        }

        private void processComparisonSpecification( FilterBuilder filterBuilder,
                                                     ComparisonSpecification<?> spec,
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
                if( spec instanceof EqSpecification )
                {
                    addFilter( termFilter( name, value ), filterBuilder );
                }
                else if( spec instanceof NeSpecification )
                {
                    addFilter( andFilter( existsFilter( name ),
                                          notFilter( termFilter( name, value ) ) ),
                               filterBuilder );
                }
                else if( spec instanceof GeSpecification )
                {
                    addFilter( rangeFilter( name ).gte( value ), filterBuilder );
                }
                else if( spec instanceof GtSpecification )
                {
                    addFilter( rangeFilter( name ).gt( value ), filterBuilder );
                }
                else if( spec instanceof LeSpecification )
                {
                    addFilter( rangeFilter( name ).lte( value ), filterBuilder );
                }
                else if( spec instanceof LtSpecification )
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
                                                      ContainsAllSpecification<?> spec,
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
                                                   ContainsSpecification<?> spec,
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
                                                  MatchesSpecification spec,
                                                  Map<String, Object> variables )
        {
            LOGGER.trace( "Processing MatchesSpecification {}", spec );
            String name = spec.property().toString();
            String regexp = resolveVariable( spec.regexp(), variables ).toString();
            addFilter( regexpFilter( name, regexp ), filterBuilder );
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
            Object value = resolveVariable( spec.value(), variables );
            addFilter( termFilter( name, value ), filterBuilder );
        }

        private void processNamedAssociationContainsSpecification( FilterBuilder filterBuilder,
                                                                   NamedAssociationContainsSpecification<?> spec,
                                                                   Map<String, Object> variables )
        {
            LOGGER.trace( "Processing NamedAssociationContainsSpecification {}", spec );
            String name = spec.namedAssociation().toString() + ".identity";
            Object value = resolveVariable( spec.value(), variables );
            addFilter( termFilter( name, value ), filterBuilder );
        }

        private void processNamedAssociationContainsNameSpecification( FilterBuilder filterBuilder,
                                                                       NamedAssociationContainsNameSpecification<?> spec,
                                                                       Map<String, Object> variables )
        {
            LOGGER.trace( "Processing NamedAssociationContainsNameSpecification {}", spec );
            String name = spec.namedAssociation().toString() + "._named";
            Object value = resolveVariable( spec.name(), variables );
            addFilter( termFilter( name, value ), filterBuilder );
        }

        private void processSpatialPredicatesSpecification( FilterBuilder filterBuilder,
                                                            SpatialPredicatesSpecification<?> spec,
                                                            Map<String, Object> variables )
                throws EntityFinderException
        {
            LOGGER.trace("Processing SpatialPredicatesSpecification {}", spec);
            EXTENDED_SPEC_SUPPORTS.get( spec.getClass().getSuperclass() ).support(module, support).processSpecification(filterBuilder, spec, variables);
        }

        private void processSpatialConvertSpecification( FilterBuilder filterBuilder,
                                                         SpatialConvertSpecification<?> spec,
                                                         Map<String, Object> variables )
                throws EntityFinderException
        {
            LOGGER.trace("Processing SpatialConvertSpecification {}", spec);
            EXTENDED_SPEC_SUPPORTS.get( spec.getClass().getSuperclass() ).support(module, support).processSpecification(filterBuilder, spec, variables);
        }
    }

}
