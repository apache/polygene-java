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

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        LOGGER.info( "Index/Query connected to Elastic Search" );
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

}
