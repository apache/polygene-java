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
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.index.elasticsearch.ElasticSearchConfiguration;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
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

    protected SpatialConfiguration.Configuration spatialConfiguration;


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

    protected void defaultSpatialConfiguration(ElasticSearchConfiguration configuration)
    {

        if (  (configuration.spatial().get() == null) )
        {
            SpatialConfiguration.Configuration cConfig = module.newValueBuilder(SpatialConfiguration.Configuration.class).prototype();

            SpatialConfiguration.IndexerConfiguration cIndexer = module.newValueBuilder(SpatialConfiguration.IndexerConfiguration.class).prototype();
            SpatialConfiguration.IndexingMethod cIndexingMethod = module.newValueBuilder(SpatialConfiguration.IndexingMethod.class).prototype();
            SpatialConfiguration.ProjectionSupport cProjectionIndexerSupport = module.newValueBuilder(SpatialConfiguration.ProjectionSupport.class).prototype();

            SpatialConfiguration.FinderConfiguration cFinder = module.newValueBuilder(SpatialConfiguration.FinderConfiguration.class).prototype();
            SpatialConfiguration.ProjectionSupport cProjectionFinderSupport = module.newValueBuilder(SpatialConfiguration.ProjectionSupport.class).prototype();



            cIndexingMethod.Type().set(SpatialConfiguration.INDEXING_METHOD.GEO_POINT);
            cIndexingMethod.Precision().set("2m");

            cProjectionIndexerSupport.ConversionEnabled().set(true);
            cProjectionIndexerSupport.ConversionAccuracy().set("2m");

            // Setup Indexer
            cIndexer.Method().set(cIndexingMethod);
            cIndexer.Projection().set(cProjectionIndexerSupport);


            cProjectionFinderSupport.ConversionEnabled().set(true);
            cProjectionFinderSupport.ConversionAccuracy().set("2m");

            // Setup Finder
            cFinder.Projection().set(cProjectionFinderSupport);

            // Setup Configuration
            cConfig.Enabled().set(true);
            cConfig.Indexer().set(cIndexer);
            cConfig.Finder().set(cFinder);

            spatialConfiguration = cConfig;

        } else
        {
            // config available
            spatialConfiguration = configuration.spatial().get();
        }

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
    public final SpatialConfiguration.Configuration spatialConfiguration()
    {
        return spatialConfiguration;
    }


    @Override
    public final  Module getModule() { return module;}

}
