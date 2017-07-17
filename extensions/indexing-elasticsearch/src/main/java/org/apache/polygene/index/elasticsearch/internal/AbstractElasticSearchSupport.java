/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.index.elasticsearch.internal;

import org.apache.polygene.index.elasticsearch.ElasticSearchSupport;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractElasticSearchSupport
    implements ElasticSearchSupport
{
    protected static final Logger LOGGER = LoggerFactory.getLogger( ElasticSearchSupport.class );
    protected static final String DEFAULT_CLUSTER_NAME = "polygene_cluster";
    protected static final String DEFAULT_INDEX_NAME = "polygene_index";
    protected static final String ENTITIES_TYPE = "polygene_entities";

    protected Client client;
    protected String index;
    protected boolean indexNonAggregatedAssociations;

    @Override
    public final void activateService()
        throws Exception
    {
        activateElasticSearch();

        // Wait for yellow status: the primary shard is allocated but replicas may not be yet
        client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

        if( !client.admin().indices().prepareExists( index ).setIndices( index ).execute().actionGet().isExists() )
        {
            // Create empty index
            LOGGER.info( "Will create '{}' index as it does not exists.", index );
            Settings.Builder indexSettings = Settings.builder().loadFromSource(
                XContentFactory.jsonBuilder()
                               .startObject()
                                   .field( "refresh_interval", -1 )
                                   .startObject( "mapper" )
                                       .field( "dynamic", false )
                                   .endObject()
                                   .startObject( "analysis" )
                                       .startObject( "analyzer" )
                                           .startObject( "default" )
                                               .field( "type", "keyword" )
                                           .endObject()
                                           .endObject()
                                   .endObject()
                               .endObject()
                               .string(),
                XContentType.JSON);
            XContentBuilder mapping = XContentFactory.jsonBuilder()
                                                     .startObject()
                                                         .startObject( entitiesType() )
                                                             .startArray( "dynamic_templates" )
                                                                 .startObject()
                                                                     .startObject( entitiesType() )
                                                                         .field( "match", "*" )
                                                                         .field( "match_mapping_type", "string" )
                                                                         .startObject( "mapping" )
                                                                             .field( "type", "string" )
                                                                             .field( "index", "not_analyzed" )
                                                                         .endObject()
                                                                     .endObject()
                                                                 .endObject()
                                                             .endArray()
                                                         .endObject()
                                                     .endObject();
            client.admin().indices().prepareCreate( index )
                  .setIndex( index )
                  .setSettings( indexSettings )
                  .addMapping( entitiesType(), mapping )
                  .execute()
                  .actionGet();
            LOGGER.info( "Index '{}' created.", index );
        }

        // Ensure index is fresh
        client.admin().indices().prepareRefresh( index ).execute().actionGet();

        // Wait for yellow status: the primary shard is allocated but replicas may not be yet
        client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

        LOGGER.info( "Index/Query connected to Elastic Search" );
    }

    protected abstract void activateElasticSearch()
        throws Exception;

    @Override
    public final void passivateService()
        throws Exception
    {
        passivateClient();
        index = null;
        indexNonAggregatedAssociations = false;
        passivateElasticSearch();
    }

    protected void passivateClient()
    {
        client.close();
        client = null;
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
}
