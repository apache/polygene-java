package org.qi4j.index.elasticsearch.extensions.spatial.mappings.builders;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.mappings.cache.MappingsCachesTable;

/**
 * Created by jj on 19.12.14.
 */
public class AbstractBuilder {
    protected ElasticSearchSupport support;

    public String get(String field)
    {
        if ( !isValid() ) throw new RuntimeException("ElasticSearch Index or Type not defined");

        String index = support.index(); String type = support.entitiesType();

        GetFieldMappingsResponse response = support.client().admin().indices()
                .prepareGetFieldMappings(index)
                .setTypes(type)
                .setFields(field).get();

        if (response != null &&
                response.fieldMappings(index, type, field) != null &&
                !response.fieldMappings(index, type, field).isNull() &&
                response.fieldMappings(index, type, field).fullName().equals(field)) {

            return response.fieldMappings(index, type, field).sourceAsMap().toString();
        } else
            return null;
    }



    protected boolean isValid()
    {
        return (support != null) && (support.index() != null) && (support.entitiesType() != null) ? true : false;
    }

    protected boolean put(String field, String mappingJson)
    {
        if ( !isValid() ) throw new RuntimeException("ElasticSearch Index or Type not defined");

        String index = support.index(); String type = support.entitiesType();

        PutMappingResponse ESSpatialMappingPUTResponse = support.client().admin().indices()
                .preparePutMapping(index).setType(type)
                .setSource(mappingJson)
                .execute().actionGet();

        if (ESSpatialMappingPUTResponse.isAcknowledged())
        {
            // we are reading the mapping back from server to assure that the server-side mappings always "wins"
            MappingsCachesTable.getMappingCache(support.index(), support.entitiesType()).put(field, get(field));
            return true;
        } else
            return false;
    }
}
