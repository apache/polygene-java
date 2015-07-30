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
package org.apache.zest.index.elasticsearch;

import java.io.File;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.apache.zest.library.fileconfig.FileConfigurationOverride;
import org.apache.zest.library.fileconfig.FileConfigurationService;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.indexing.AbstractComplexQueryTest;
import org.apache.zest.test.util.DelTreeAfter;

import static org.apache.zest.test.util.Assume.assumeNoIbmJdk;

@Ignore( "ElasticSearch Index/Query do not support Complex Queries, ie. queries by 'example values'" )
public class ElasticSearchComplexQueryTest
    extends AbstractComplexQueryTest
{

    private static final File DATA_DIR = new File( "build/tmp/es-complex-query-test" );
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
        new EntityTestAssembler().assemble( config );

        // Index/Query
        new ESFilesystemIndexQueryAssembler().
            withConfig( config, Visibility.layer ).
            assemble( module );
        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
            withData( new File( DATA_DIR, "zest-data" ) ).
            withLog( new File( DATA_DIR, "zest-logs" ) ).
            withTemporary( new File( DATA_DIR, "zest-temp" ) );
        module.services( FileConfigurationService.class ).setMetaInfo( override );
    }

    @Override
    public void showNetwork()
    {
        // IndexExporter not supported by ElasticSearch
    }

}
