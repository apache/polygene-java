/*
 * Copyright (c) 2014, Jiri Jetmar. All Rights Reserved.
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

package org.qi4j.index.elasticsearch.extensions.spatial.functions.predicates;

import org.elasticsearch.index.query.FilterBuilder;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_DisjointSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_IntersectsSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.ST_WithinSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.predicate.SpatialPredicatesSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinder;
import org.qi4j.spi.query.EntityFinderException;

import java.util.HashMap;
import java.util.Map;


public class PredicateFinderSupport implements ElasticSearchSpatialFinder.SpatialQuerySpecSupport
{

    private static final Map<Class<?>, PredicateFinderSupport.PredicateSpecification> SPATIAL_PREDICATE_OPERATIONS = new HashMap<>(3);

    static
    {
        SPATIAL_PREDICATE_OPERATIONS.put(ST_WithinSpecification.class, new ST_Within());
        SPATIAL_PREDICATE_OPERATIONS.put(ST_DisjointSpecification.class, new ST_Disjoint());
        SPATIAL_PREDICATE_OPERATIONS.put(ST_IntersectsSpecification.class, new ST_Intersects());
    }

    private Module module;
    private ElasticSearchSupport support;

    public ElasticSearchSpatialFinder.SpatialQuerySpecSupport support(Module module, ElasticSearchSupport support)
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

        if (SPATIAL_PREDICATE_OPERATIONS.get(spec.getClass()) != null)
        {
            SPATIAL_PREDICATE_OPERATIONS.get(spec.getClass()).support(module, support).processSpecification(filterBuilder, (SpatialPredicatesSpecification) spec, variables);
        } else
        {
            throw new UnsupportedOperationException("Spatial predicates specification unsupported by Elastic Search "
                    + "(New Query API support missing?): "
                    + spec.getClass() + ": " + spec);
        }
    }

    public interface Support
    {
        PredicateSpecification support(Module module, ElasticSearchSupport support);
    }


    public static interface PredicateSpecification extends Support
    {
        void processSpecification(FilterBuilder filterBuilder, SpatialPredicatesSpecification<?> spec, Map<String, Object> variables) throws EntityFinderException;
    }


}


