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

package org.apache.zest.index.elasticsearch;

import java.io.IOException;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.apache.zest.library.fileconfig.FileConfigurationOverride;
import org.apache.zest.library.fileconfig.FileConfigurationService;
import org.apache.zest.spi.query.EntityFinderException;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.indexing.AbstractQueryTest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.apache.zest.test.util.Assume.assumeNoIbmJdk;

public class ElasticSearchQueryTest
    extends AbstractQueryTest
{
    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );

        // Config module
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        // Index/Query
        new ESFilesystemIndexQueryAssembler().withConfig( config, Visibility.layer ).assemble( module );
        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        module.services( FileConfigurationService.class )
              .setMetaInfo( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) );
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
