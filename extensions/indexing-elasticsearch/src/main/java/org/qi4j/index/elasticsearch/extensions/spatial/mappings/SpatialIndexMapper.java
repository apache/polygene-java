package org.qi4j.index.elasticsearch.extensions.spatial.mappings;

import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.mappings.cache.MappingsCachesTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.qi4j.index.elasticsearch.extensions.spatial.mappings.builders.SpatialMappingFactory.*;
import static org.qi4j.api.geometry.TGeometryFactory.*;


/**
 * Created by jj on 19.12.14.
 */
public class SpatialIndexMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpatialIndexMapper.class);


    public static void createIfNotExist(ElasticSearchSupport support, TGeometry geometry, String property) {

        if (!MappingsCachesTable.getMappingCache(support).exists(property)) {
            String mappingsOnServer = MappingQuery(support).get(property);
            System.out.println("Found " + mappingsOnServer);

            if (mappingsOnServer != null) {
                MappingsCachesTable.getMappingCache(support).put(property, mappingsOnServer);
            } else {

                if (TPoint(support.getModule()).isPoint(geometry) ) {

                    switch (support.indexPointMappingMethod()) {
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
    public static class MappingCache

    {

        public static boolean isMappedAsGeoShape(String index, String type, String property) {
            if (!MappingsCachesTable.getMappingCache(index, type).exists(property)) // <- No mappings yet, as no data in the index ?
                return true;

            return MappingsCachesTable.getMappingCache(index, type).get(property).toString().indexOf("type=geo_shape") > -1 ? true : false;
        }

        public static boolean isMappedAsGeoPoint(String index, String type, String property) {
            if (!MappingsCachesTable.getMappingCache(index, type).exists(property)) // <- No mappings yet, as no data in the index ?
                return true;

            return MappingsCachesTable.getMappingCache(index, type).get(property).toString().indexOf("type=geo_point") > -1 ? true : false;
        }

        public static boolean mappingExists(String index, String type, String property) {
            return MappingsCachesTable.getMappingCache(index, type).exists(property);
        }

    }


}
