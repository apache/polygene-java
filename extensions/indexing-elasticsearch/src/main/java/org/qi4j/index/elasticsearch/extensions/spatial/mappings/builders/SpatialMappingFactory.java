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

package org.qi4j.index.elasticsearch.extensions.spatial.mappings.builders;

import org.qi4j.index.elasticsearch.ElasticSearchSupport;

/**
 * Created by jj on 19.12.14.
 */
public class SpatialMappingFactory
{

    public static GeoPointBuilder GeoPointMapping(ElasticSearchSupport support)
    {
        return new GeoPointBuilder(support);
    }

    public static GeoShapeBuilder GeoShapeMapping(ElasticSearchSupport support)
    {
        return new GeoShapeBuilder(support);
    }

    public static MappingQueryBuilder MappingQuery(ElasticSearchSupport support)
    {
        return new MappingQueryBuilder(support);
    }

}
