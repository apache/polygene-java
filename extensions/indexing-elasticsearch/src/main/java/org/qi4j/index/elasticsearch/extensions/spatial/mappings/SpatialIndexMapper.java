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

package org.qi4j.index.elasticsearch.extensions.spatial.mappings;

import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.index.elasticsearch.extensions.spatial.mappings.cache.MappingsCachesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.api.geometry.TGeometryFactory.TPoint;
import static org.qi4j.index.elasticsearch.extensions.spatial.mappings.builders.SpatialMappingFactory.*;

public class SpatialIndexMapper
{

    private static final Logger LOGGER = LoggerFactory.getLogger(SpatialIndexMapper.class);


    public static void createIfNotExist(ElasticSearchSupport support, TGeometry geometry, String property)
    {
        if (!MappingsCachesTable.getMappingCache(support).exists(property))
        {
            String mappingsOnServer = MappingQuery(support).get(property);

            if (mappingsOnServer != null)
            {
                // TODO JJ check mappings : configuration versus server-side settings
                MappingsCachesTable.getMappingCache(support).put(property, mappingsOnServer);
            } else
            {
                if (TPoint(support.getModule()).isPoint(geometry))
                {
                    switch (SpatialConfiguration.getMethod(support.spatialConfiguration()))
                    {
                        case GEO_POINT:
                            GeoPointMapping(support).create(property);
                            break;
                        case GEO_SHAPE:
                            GeoShapeMapping(support).create(property);
                            break;
                        default:
                            throw new RuntimeException("Unknown Point Maping Type.");
                    }
                } else
                {
                    GeoShapeMapping(support).create(property);
                }
            }
        }
    }


    /**
     * Dedicated Cache Operations. No mutable operations on server-side mappings.
     */
    public static class IndexMappingCache

    {

        public static boolean isMappedAsGeoShape(String index, String type, String property)
        {
            if (!MappingsCachesTable.getMappingCache(index, type).exists(property)) // <- No mappings yet, as no data in the index ?
                return false;
            return MappingsCachesTable.getMappingCache(index, type).get(property).toString().indexOf("type=geo_shape") > -1 ? true : false;
        }

        public static boolean isMappedAsGeoPoint(String index, String type, String property)
        {
            if (!MappingsCachesTable.getMappingCache(index, type).exists(property)) // <- No mappings yet, as no data in the index ?
                return false;
            return MappingsCachesTable.getMappingCache(index, type).get(property).toString().indexOf("type=geo_point") > -1 ? true : false;
        }

        public static boolean mappingExists(String index, String type, String property)
        {
            return MappingsCachesTable.getMappingCache(index, type).exists(property);
        }

        public static void clear()
        {
            MappingsCachesTable.clear();
        }

    }


}
