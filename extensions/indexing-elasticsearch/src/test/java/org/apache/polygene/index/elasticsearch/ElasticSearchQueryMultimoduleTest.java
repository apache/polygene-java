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
package org.apache.polygene.index.elasticsearch;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.elasticsearch.assembly.ESClientIndexQueryAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationOverride;
import org.apache.polygene.test.EntityTestAssembler;

public class ElasticSearchQueryMultimoduleTest extends ElasticSearchQueryTest
{
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
        new ESClientIndexQueryAssembler( ELASTIC_SEARCH.client() )
            .withConfig( config, Visibility.application )
            .visibleIn( Visibility.layer )
            .assemble( module );
        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.index().set( ELASTIC_SEARCH.indexName( ElasticSearchQueryTest.class.getName(),
                                                        testName.getMethodName() ) );
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );
    }
}
