package org.qi4j.index.elasticsearch.extensions.spatial.mapping;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;

import static org.qi4j.index.elasticsearch.extensions.spatial.mapping.ElasticSearchMappingsCache.MappingsCache;

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

        if (getFieldMappings(field) != null)
            return true;
        else
            return false;
    }

    public boolean addFieldMappings(String propertyWithDepth, String mappingJSON) {

        System.out.println("addFieldMappings " + propertyWithDepth + " json " + mappingJSON);

        PutMappingResponse ESSpatialMappingPUTResponse = support.client().admin().indices()
                .preparePutMapping(index).setType(type)
                .setSource(mappingJSON)
                .execute().actionGet();

        if (ESSpatialMappingPUTResponse.isAcknowledged()) {
            MappingsCache().put(propertyWithDepth);
            return true;
        } else
            return false;



    }


}


