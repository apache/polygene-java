/*
 * Copyright 2014 Jiri Jetmar.
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
package org.qi4j.index.elasticsearch.extensions.spatial;


import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.query.grammar.ComparisonSpecification;
import org.qi4j.api.query.grammar.ContainsAllSpecification;
import org.qi4j.api.query.grammar.ContainsSpecification;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.ElasticSearchFinderSupport;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.functions.convert.ConvertFinderSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates.PredicateFinderSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.InternalUtils;
import org.qi4j.index.elasticsearch.extensions.spatial.mappings.SpatialIndexMapper;
import org.qi4j.spi.query.EntityFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class ElasticSearchSpatialFinder
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchSpatialFinder.class);

    private static final Map<Class<?>, SpatialQuerySpecSupport> SPATIAL_QUERY_EXPRESSIONS_CATALOG = new HashMap<>(2);

    static
    {
        SPATIAL_QUERY_EXPRESSIONS_CATALOG.put(SpatialPredicatesSpecification.class, new PredicateFinderSupport());
        SPATIAL_QUERY_EXPRESSIONS_CATALOG.put(SpatialConvertSpecification.class, new ConvertFinderSupport());
    }


    private ElasticSearchSpatialFinder()
    {
    }

    public interface Support
    {
        SpatialQuerySpecSupport support(Module module, ElasticSearchSupport support);
    }


    public static interface SpatialQuerySpecSupport extends Support
    {
        void processSpecification(FilterBuilder filterBuilder, Specification<?> spec, Map<String, Object> variables) throws EntityFinderException;
    }

    public static class SpatialSpecSupport
            implements SpatialQuerySpecSupport
    {
        private Module module;
        private ElasticSearchSupport support;

        public SpatialQuerySpecSupport support(Module module, ElasticSearchSupport support)
        {
            this.module = module;
            this.support = support;
            return this;
        }


        public void processSpecification(FilterBuilder filterBuilder,
                                         Specification<?> spec,
                                         Map<String, Object> variables)
                throws EntityFinderException
        {
            SPATIAL_QUERY_EXPRESSIONS_CATALOG.get(spec.getClass().getSuperclass()).support(module, support).processSpecification(filterBuilder, spec, variables);
        }

    }

    public static class SpatialTypeSupport
            implements ElasticSearchFinderSupport.ComplexTypeSupport
    {

        private Module module;
        private ElasticSearchSupport support;

        public ElasticSearchFinderSupport.ComplexTypeSupport support(Module module, ElasticSearchSupport support)
        {
            this.module = module;
            this.support = support;

            return this;
        }


        public FilterBuilder comparison(ComparisonSpecification<?> spec, Map<String, Object> variables)
        {
            throw new RuntimeException("Unsupported operation");
        }

        public FilterBuilder contains(ContainsSpecification<?> spec, Map<String, Object> variables)
        {
            throw new RuntimeException("Unsupported operation");
        }

        public FilterBuilder containsAll(ContainsAllSpecification<?> spec, Map<String, Object> variables)
        {
            throw new RuntimeException("Unsupported operation");
        }

        public void orderBy(SearchRequestBuilder request, Specification<Composite> whereClause, OrderBy orderBySegment, Map<String, Object> variables) throws EntityFinderException
        {
            if (!TPoint.class.isAssignableFrom(InternalUtils.classOfPropertyType(orderBySegment.property())))
            {
                throw new EntityFinderException("Ordering can only be done on TPoints.. TODO");
            }

            if (!SpatialIndexMapper.IndexMappingCache.isMappedAsGeoPoint(support.index(), support.entitiesType(), orderBySegment.property().toString()))
            {
                throw new EntityFinderException("OrderBy is only supported when GEO_POINT indexing is used");
            }

            GeoDistanceSortBuilder geoDistanceSortBuilder = new GeoDistanceSortBuilder(orderBySegment.property().toString());
            geoDistanceSortBuilder.point(orderBySegment.getCentre().y(), orderBySegment.getCentre().x());

            geoDistanceSortBuilder.order(orderBySegment.order() == OrderBy.Order.ASCENDING ? SortOrder.ASC : SortOrder.DESC);
            request.addSort(geoDistanceSortBuilder.geoDistance(GeoDistance.SLOPPY_ARC).sortMode("avg").unit(DistanceUnit.METERS));
        }
    }

}
