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

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.mappings.cache.MappingsCachesTable;

public class AbstractBuilder
{

    protected ElasticSearchSupport support;

    public String get(String field)
    {
        if (!isValid()) throw new RuntimeException("ElasticSearch Index or Type not defined");

        String index = support.index();
        String type = support.entitiesType();

        GetFieldMappingsResponse response = support.client().admin().indices()
                .prepareGetFieldMappings(index)
                .setTypes(type)
                .setFields(field).get();

        if (response != null &&
                response.fieldMappings(index, type, field) != null &&
                !response.fieldMappings(index, type, field).isNull() &&
                response.fieldMappings(index, type, field).fullName().equals(field))
        {

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
        if (!isValid()) throw new RuntimeException("ElasticSearch Index or Type not defined");

        String index = support.index();
        String type = support.entitiesType();

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
