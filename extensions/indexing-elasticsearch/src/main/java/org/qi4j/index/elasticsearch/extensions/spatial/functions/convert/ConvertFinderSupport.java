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

package org.qi4j.index.elasticsearch.extensions.spatial.functions.convert;

import org.elasticsearch.index.query.FilterBuilder;
import org.qi4j.api.query.grammar.extensions.spatial.convert.ST_GeomFromTextSpecification;
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Specification;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.ElasticSearchSpatialFinder;
import org.qi4j.spi.query.EntityFinderException;

import java.util.HashMap;
import java.util.Map;


public class ConvertFinderSupport implements ElasticSearchSpatialFinder.SpatialQuerySpecSupport
{

    private static final Map<Class<?>, ConvertFinderSupport.ConvertSpecification> SPATIAL_CONVERT_OPERATIONS = new HashMap<>(2);

    static
    {
        SPATIAL_CONVERT_OPERATIONS.put(ST_GeomFromTextSpecification.class, new ST_GeometryFromText());
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

        if (SPATIAL_CONVERT_OPERATIONS.get(spec.getClass()) != null)
        {
            SPATIAL_CONVERT_OPERATIONS.get(spec.getClass()).support(module, support).processSpecification(filterBuilder, (SpatialConvertSpecification) spec, variables);
        } else
        {
            throw new UnsupportedOperationException("Spatial predicates specification unsupported by Elastic Search "
                    + "(New Query API support missing?): "
                    + spec.getClass() + ": " + spec);

        }

    }

    public interface Support
    {
        ConvertSpecification support(Module module, ElasticSearchSupport support);
    }

    public static interface ConvertSpecification extends Support
    {
        void processSpecification(FilterBuilder filterBuilder, SpatialConvertSpecification<?> spec, Map<String, Object> variables) throws EntityFinderException;
    }

}
