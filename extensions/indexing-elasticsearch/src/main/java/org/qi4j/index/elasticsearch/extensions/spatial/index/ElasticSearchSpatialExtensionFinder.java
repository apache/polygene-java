package org.qi4j.index.elasticsearch.extensions.spatial.index;

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

import org.elasticsearch.index.query.*;
import org.qi4j.api.query.grammar.ComparisonSpecification;
import org.qi4j.api.query.grammar.ContainsAllSpecification;
import org.qi4j.api.query.grammar.ContainsSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.functions.convert.ElasticSearchSpatialConvertFinderSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates.ElasticSearchSpatialPredicateFinderSupport;
import org.qi4j.spi.query.EntityFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.index.query.FilterBuilders.geoShapeFilter;

import org.qi4j.index.elasticsearch.ElasticSearchFinderSupport;

public final class ElasticSearchSpatialExtensionFinder
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchSpatialExtensionFinder.class);

    private static final Map<Class<?>, SpatialQuerySpecSupport> SPATIAL_QUERY_EXPRESSIONS_CATALOG = new HashMap<>( 2 );

    static
    {
        SPATIAL_QUERY_EXPRESSIONS_CATALOG.put(SpatialPredicatesSpecification.class, new ElasticSearchSpatialPredicateFinderSupport());
        SPATIAL_QUERY_EXPRESSIONS_CATALOG.put(SpatialConvertSpecification.class, new ElasticSearchSpatialConvertFinderSupport());
    }



    public interface ModuleHelper {
        void setModule(Module module, ElasticSearchSupport support);
    }

    public static interface SpatialQuerySpecSupport extends ModuleHelper
    {
        void processSpecification(FilterBuilder filterBuilder, Specification<?> spec, Map<String, Object> variables)  throws EntityFinderException;
    }



    public static class SpatialSpecSupport
                implements  SpatialQuerySpecSupport {

        Module module;
        ElasticSearchSupport support;

        public void setModule(Module module, ElasticSearchSupport support)
        {
            this.module  = module;
            this.support = support;
        }


        public void processSpecification(FilterBuilder filterBuilder,
                                              Specification<?> spec,
                                              Map<String, Object> variables) {

            SpatialQuerySpecSupport spatialQuerySpecSupport = SPATIAL_QUERY_EXPRESSIONS_CATALOG.get(spec.getClass().getSuperclass());
            spatialQuerySpecSupport.setModule(module, support);

            try {
                spatialQuerySpecSupport.processSpecification(filterBuilder, spec, variables);

            } catch (Exception _ex) {
                _ex.printStackTrace();
            }


        }

    }


    public static class SpatialTypeSupport
            implements ElasticSearchFinderSupport.ComplexTypeSupport
    {

        public FilterBuilder comparison( ComparisonSpecification<?> spec, Map<String, Object> variables )
        {
            return null;
        }

        public FilterBuilder contains( ContainsSpecification<?> spec, Map<String, Object> variables )
        {
            return null;
        }

        public FilterBuilder containsAll( ContainsAllSpecification<?> spec, Map<String, Object> variables )
        {
            return null;
        }
    }




        private ElasticSearchSpatialExtensionFinder()
    {
    }

}
