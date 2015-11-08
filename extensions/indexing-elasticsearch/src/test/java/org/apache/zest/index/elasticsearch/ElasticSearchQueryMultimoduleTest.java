/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.zest.index.elasticsearch;

import java.io.File;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.apache.zest.library.fileconfig.FileConfigurationOverride;
import org.apache.zest.library.fileconfig.FileConfigurationService;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.util.DelTreeAfter;
import org.junit.Rule;

public class ElasticSearchQueryMultimoduleTest extends ElasticSearchQueryTest
{
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        assembleEntities( module, Visibility.module );
        assembleValues( module, Visibility.layer );

        module = module.layer().module( "module2" );
        new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( module );

        // Config module
        LayerAssembly configLayer = module.layer().application().layer( "config" );
        module.layer().uses( configLayer );
        ModuleAssembly config = configLayer.module( "config" );
        new EntityTestAssembler().assemble( config );

        // Index/Query
        new ESFilesystemIndexQueryAssembler().
            withConfig( config, Visibility.application ).visibleIn( Visibility.layer ).
            assemble( module );
        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().
            withData( new File( DATA_DIR, "zest-data" ) ).
            withLog( new File( DATA_DIR, "zest-logs" ) ).
            withTemporary( new File( DATA_DIR, "zest-temp" ) );
        module.services( FileConfigurationService.class ).
            setMetaInfo( override );
    }

}
