package org.qi4j.index.elasticsearch.extensions.spatial.mappings.old;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;

import static org.qi4j.index.elasticsearch.extensions.spatial.mappings.old.ElasticSearchMappingsCache.MappingsCache;

/**
 * Created by jj on 06.11.14.
 */
public class ElasticSearchMappingsHelper {


    private ElasticSearchSupport support;

    private String index;
    private String type;



    public static ElasticSearchMappingsHelper Mappings(ElasticSearchSupport support) {
        return new ElasticSearchMappingsHelper(support);
    }

    public  ElasticSearchMappingsHelper(ElasticSearchSupport support) {
        this.support = support;
    }

    public ElasticSearchMappingsHelper onIndex(String index) {
        this.index = index;
        return this;
    }

    public ElasticSearchMappingsHelper andType(String  type) {
        this.type = type;
        return this;
    }

    public GetFieldMappingsResponse.FieldMappingMetaData getFieldMappings(String field) {

        if ( (index == null) || ( type == null) ) throw new RuntimeException("ElasticSearch Index or Type not defined");

        GetFieldMappingsResponse response = support.client().admin().indices()
                .prepareGetFieldMappings(index)
                .setTypes(type)
                .setFields(field).get();

        if (response != null &&
                response.fieldMappings(index, type, field) != null &&
                !response.fieldMappings(index, type, field).isNull() &&
                response.fieldMappings(index, type, field).fullName().equals(field)) {

            return response.fieldMappings(index, type, field);
        } else
            return null;
    }




    public boolean existsFieldMapping(String field) {

        GetFieldMappingsResponse.FieldMappingMetaData fieldMappingMetaData = getFieldMappings(field);

        if (fieldMappingMetaData != null)
        {
            MappingsCache(support).put(field, fieldMappingMetaData.sourceAsMap().toString() );
            return true;
        }

        else
            return false;
    }

    // JJ TODO make sure that the cache has always latest/valid status. Idea - use the ttl to invalidate the cache.
    public boolean existsMapping(String property)
    {
        return MappingsCache(support).exists(property);
    }

    public boolean addFieldMappings(String propertyWithDepth, String mappingJSON) {

        System.out.println("addFieldMappings " + propertyWithDepth + " json " + mappingJSON);

        PutMappingResponse ESSpatialMappingPUTResponse = support.client().admin().indices()
                .preparePutMapping(index).setType(type)
                .setSource(mappingJSON)
                .execute().actionGet();

        if (ESSpatialMappingPUTResponse.isAcknowledged()) {
            MappingsCache(support).put(propertyWithDepth, getFieldMappings(propertyWithDepth).sourceAsMap().toString());
            return true;
        } else
            return false;

    }

    public boolean isGeoShape(String property)
    {
        if (!MappingsCache(support).exists(property)) // <- No mappings yet, as no data in the index ?
            return true;

        return MappingsCache(support).get(property).toString().indexOf("type=geo_shape") > -1 ? true : false;
    }

    public boolean isGeoPoint(String property)
    {
        if (!MappingsCache(support).exists(property)) // <- No mappings yet, as no data in the index ?
            return true;

        return MappingsCache(support).get(property).toString().indexOf("type=geo_point") > -1 ? true : false;
    }





}


