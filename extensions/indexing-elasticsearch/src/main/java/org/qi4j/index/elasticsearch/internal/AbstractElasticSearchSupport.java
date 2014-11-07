/*
 * Copyright 2012 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.elasticsearch.internal;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractElasticSearchSupport
        implements ElasticSearchSupport
{

    protected static final Logger LOGGER = LoggerFactory.getLogger( ElasticSearchSupport.class );

    protected static final String DEFAULT_CLUSTER_NAME = "qi4j_cluster";

    protected static final String DEFAULT_INDEX_NAME = "qi4j_index";

    protected static final String ENTITIES_TYPE = "qi4j_entities";

    protected Client client;

    protected String index;

    protected boolean indexNonAggregatedAssociations;

    @Structure
    Module module;

    @Override
    public final void activateService()
            throws Exception
    {
        activateElasticSearch();

        // Wait for yellow status: the primary shard is allocated but replicas are not
        client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

        if ( !client.admin().indices().prepareExists( index ).setIndices( index ).execute().actionGet().isExists() ) {
            // Create empty index
            LOGGER.info( "Will create '{}' index as it does not exists.", index );
            ImmutableSettings.Builder indexSettings = ImmutableSettings.settingsBuilder().loadFromSource( XContentFactory.jsonBuilder().
                    startObject().
                    startObject( "analysis" ).
                    startObject( "analyzer" ).
                    //
                    startObject( "default" ).
                    field( "type", "keyword" ). // Globally disable analysis, content is treated as a single keyword
                    endObject().
                    //
                    endObject().
                    endObject().
                    endObject().
                    string() );
            client.admin().indices().prepareCreate( index ).
                    setIndex( index ).
                    setSettings( indexSettings ).
                    execute().
                    actionGet();
            LOGGER.info( "Index '{}' created.", index );
        }

        // getKnownMappings();

        LOGGER.info( "Index/Query connected to Elastic Search" );
    }

    private void getKnownMappings() throws Exception {
/**
        GetMappingsRequest getMappingsRequest = new GetMappingsRequest().indices(DEFAULT_INDEX_NAME);
        client.admin().indices().getMappings(getMappingsRequest).actionGet();
*/

        GetMappingsResponse mappingsResponse = client().admin().indices().prepareGetMappings(DEFAULT_INDEX_NAME).execute().get();
        // GetFieldMappingsResponse response = client().admin().indices().prepareGetFieldMappings(DEFAULT_INDEX_NAME).get();

        GetFieldMappingsResponse mappingsResponse1 = client().admin().indices().prepareGetFieldMappings(DEFAULT_INDEX_NAME).setTypes(ENTITIES_TYPE).execute().get();
        // System.out.println("Known mappings.. " + mappingsResponse1. getMappings().size() );

        GetMappingsResponse a = client().admin().indices().prepareGetMappings(DEFAULT_INDEX_NAME).execute().get();

        GetMappingsResponse response1 = client().admin().indices().prepareGetMappings().execute().actionGet();
/**
        System.out.println("response1 " + response1.mappings().get(DEFAULT_INDEX_NAME).get(ENTITIES_TYPE).sourceAsMap().size());

        Map mappings = response1.mappings().get(DEFAULT_INDEX_NAME).get(ENTITIES_TYPE). sourceAsMap();

        System.out.println(response1.mappings().get(DEFAULT_INDEX_NAME).get(ENTITIES_TYPE).source().string());


        // GetFieldMappingsResponse response = client().admin().indices().prepareGetFieldMappings(DEFAULT_INDEX_NAME).setTypes(ENTITIES_TYPE).execute().get();

        // GetFieldMappingsResponse.FieldMappingMetaData fields = response.fieldMappings(DEFAULT_INDEX_NAME, ENTITIES_TYPE, "location");

        GetFieldMappingsResponse response = client().admin().indices().prepareGetFieldMappings(DEFAULT_INDEX_NAME).setTypes(ENTITIES_TYPE).setFields("location", "placeOfBirth.location").get();
        System.out.println("ABC " + response.fieldMappings(DEFAULT_INDEX_NAME, ENTITIES_TYPE, "location").fullName() );


        // response1.mappings().get(DEFAULT_INDEX_NAME).get(ENTITIES_TYPE

                System.out.println("Known mappings1 " + mappingsResponse1.mappings().size());
        System.out.println("Known mappings " + mappingsResponse.mappings().size());
        // System.out.println(response);

        List<Object> list = getValues(mappings);

        System.out.println(list.size());
*/
    }


    // http://stackoverflow.com/questions/17136138/how-to-make-elasticsearch-add-the-timestamp-field-to-every-document-in-all-indic


    public List<Object> getValues(Map<String, Object> map) {

        List<Object> retVal = new ArrayList<Object>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();

            if (value instanceof Map) {
                retVal.addAll(getValues((Map) value));
            } else {
                System.out.println(key + ":" + value);
                retVal.add(value);
            }
        }

        return retVal;
    }



    protected abstract void activateElasticSearch()
            throws Exception;

    @Override
    public final void passivateService()
            throws Exception
    {
        client.close();
        client = null;
        index = null;
        indexNonAggregatedAssociations = false;
        passivateElasticSearch();
    }

    protected void passivateElasticSearch()
            throws Exception
    {
        // NOOP
    }

    @Override
    public final Client client()
    {
        return client;
    }

    @Override
    public final String index()
    {
        return index;
    }

    @Override
    public final String entitiesType()
    {
        return ENTITIES_TYPE;
    }

    @Override
    public final boolean indexNonAggregatedAssociations()
    {
        return indexNonAggregatedAssociations;
    }

    @Override
    public final  Module getModule() { return module;}

}
