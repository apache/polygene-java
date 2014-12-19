package org.qi4j.index.elasticsearch.extensions.spatial.mappings.builders;

import org.qi4j.index.elasticsearch.ElasticSearchSupport;

/**
 * Created by jj on 19.12.14.
 */
public class SpatialMappingFactory {

    public static GeoPointBuilder GeoPointMapping(ElasticSearchSupport support) {
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
