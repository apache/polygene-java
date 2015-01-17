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
import org.qi4j.api.query.grammar.extensions.spatial.convert.SpatialConvertSpecification;
import org.qi4j.api.structure.Module;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.internal.AbstractElasticSearchSpatialFunction;
import org.qi4j.spi.query.EntityFinderException;

import java.util.Map;

import static org.qi4j.library.spatial.formats.conversions.TConversions.Convert;


public class ST_GeometryFromText extends AbstractElasticSearchSpatialFunction implements ConvertFinderSupport.ConvertSpecification
{
    public void processSpecification(FilterBuilder filterBuilder, SpatialConvertSpecification<?> spec, Map<String, Object> variables) throws EntityFinderException
    {
        try
        {
            spec.setGeometry(Convert(module).from(spec.property()).toTGeometry());
        } catch (Exception _ex)
        {
            throw new EntityFinderException(_ex);
        }
    }

    public ConvertFinderSupport.ConvertSpecification support(Module module, ElasticSearchSupport support)
    {
        this.module = module;
        this.support = support;
        return this;
    }
}
