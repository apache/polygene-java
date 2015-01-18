/*
 * Copyright 2012-2014 Paul Merlin.
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
package org.qi4j.index.elasticsearch;

import java.io.File;
import java.io.IOException;

import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.geometry.TUnit;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.AbstractQueryTest;
import org.qi4j.test.indexing.model.City;
import org.qi4j.test.indexing.model.Person;
import org.qi4j.test.util.DelTreeAfter;

import static org.joda.time.DateTimeZone.UTC;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_GeometryFromText;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Within;
import static org.qi4j.test.util.Assume.assumeNoIbmJdk;

public class ElasticSearchQueryTest
    extends AbstractQueryTest
{

    private static final File DATA_DIR = new File( "build/tmp/es-query-test" );
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

        // Config module
        ModuleAssembly config = module.layer().module( "config" );
        config.values(SpatialConfiguration.Configuration.class,
                SpatialConfiguration.FinderConfiguration.class,
                SpatialConfiguration.IndexerConfiguration.class,
                SpatialConfiguration.IndexingMethod.class,
                SpatialConfiguration.ProjectionSupport.class).
                visibleIn(Visibility.application);
        new EntityTestAssembler().assemble( config );

        // Index/Query
        new ESFilesystemIndexQueryAssembler().
            withConfig( config, Visibility.layer ).
            assemble( module );
        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
            withData( new File( DATA_DIR, "qi4j-data" ) ).
            withLog( new File( DATA_DIR, "qi4j-logs" ) ).
            withTemporary( new File( DATA_DIR, "qi4j-temp" ) );
        module.services( FileConfigurationService.class ).
            setMetaInfo( override );
    }

    @Test
    @Ignore( "IndexExporter not supported by ElasticSearch Indexing" )
    @Override
    public void showNetwork()
        throws IOException
    {
        super.showNetwork();
    }

    @Test
    @Ignore( "oneOf() Query Expression not supported by ElasticSearch Indexing" )
    @Override
    public void script23()
        throws EntityFinderException
    {
        super.script23();
    }

    @Test
    @Ignore(
         "ElasticSearch perform automatic TimeZone resolution when querying on dates, this test assert that the "
         + "underlying Index/Query engine do not."
    )
    @Override
    public void script42_DateTime()
    {
        super.script42_DateTime();
    }
}
