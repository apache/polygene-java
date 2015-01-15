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

package org.qi4j.index.elasticsearch.extension.spatial;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.query.QueryExecutionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.ElasticSearchConfiguration;
import org.qi4j.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.library.spatial.assembly.TGeometryAssembler;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.AbstractSpatialRegressionTest;
import org.qi4j.test.util.DelTreeAfter;

import java.io.File;

import static org.qi4j.test.util.Assume.*;


public class ElasticSearchSpatialQueryTestUsingGeoShapeIndexing
        extends AbstractSpatialRegressionTest
{
    private static final File DATA_DIR = new File( "build/tmp/es-spatial-query-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }


    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );

        // Geometry support
        new TGeometryAssembler().assemble(module);

        // Config module
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        config.values(SpatialConfiguration.Configuration.class,
                SpatialConfiguration.FinderConfiguration.class,
                SpatialConfiguration.IndexerConfiguration.class,
                SpatialConfiguration.IndexingMethod.class,
                SpatialConfiguration.ProjectionSupport.class).
                visibleIn(Visibility.application);

        // Index/Query
        new ESFilesystemIndexQueryAssembler().
                withConfig(config,Visibility.layer ).
                identifiedBy("ElasticSearchGeoShapeIndexing").
                assemble(module);

        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );


        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
                withData( new File( DATA_DIR, "qi4j-data" ) ).
                withLog( new File( DATA_DIR, "qi4j-logs" ) ).
                withTemporary( new File( DATA_DIR, "qi4j-temp" ) );
        module.services( FileConfigurationService.class ).
                setMetaInfo( override );


        config.services(FileConfigurationService.class)
                .setMetaInfo(override)
                .visibleIn(Visibility.application);
    }


    @Test(expected=QueryExecutionException.class)
    @Override
    public void script01c()
            throws EntityFinderException
    {
       super.script01c(); // <- no orderBy() support for GeoShapes
    }

    @Test(expected=QueryExecutionException.class)
    @Override
    public void script01d()
            throws EntityFinderException
    {
        super.script01d(); // <- no orderBy() support for GeoShapes
    }

    @Test(expected=QueryExecutionException.class)
    @Override
    public void script01e()
            throws EntityFinderException
    {
        super.script01e(); // <- no orderBy() support for GeoShapes
    }

    @Test(expected=QueryExecutionException.class)
    @Override
    public void script01f()
            throws EntityFinderException
    {
        super.script01f(); // <- no orderBy() support for GeoShapes
    }

    @Test(expected=QueryExecutionException.class)
    @Override
    public void script03c()
            throws EntityFinderException
    {
        super.script03c(); // <- no orderBy() support for GeoShapes
    }

}
