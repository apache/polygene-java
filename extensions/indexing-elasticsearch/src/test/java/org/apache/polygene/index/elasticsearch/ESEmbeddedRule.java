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
 */
package org.apache.polygene.index.elasticsearch;

import java.util.Locale;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.index.elasticsearch.assembly.ESFilesystemIndexQueryAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationOverride;
import org.apache.polygene.test.EntityTestAssembler;
import org.elasticsearch.client.Client;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Embedded Elasticsearch JUnit Rule.
 *
 * Starting from Elasticsearch 5, startup is way slower.
 * Reuse an embedded instance across tests.
 */
public class ESEmbeddedRule implements TestRule
{
    private final TemporaryFolder tmpDir;
    private Client client;

    public ESEmbeddedRule( TemporaryFolder tmpDir )
    {
        this.tmpDir = tmpDir;
    }

    public Client client()
    {
        client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
        return client;
    }

    public String indexName( String className, String methodName )
    {
        String indexName = className;
        if( methodName != null )
        {
            indexName += '-' + methodName;
        }
        return indexName.toLowerCase( Locale.US );
    }

    @Override
    public Statement apply( final Statement base, final Description description )
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                String name = indexName( description.getClassName(), description.getMethodName() );
                SingletonAssembler assembler = activateEmbeddedElasticsearch( name );
                Application application = assembler.application();
                client = findClient( assembler.module() );
                try
                {
                    base.evaluate();
                }
                finally
                {
                    application.passivate();
                    client.close();
                    client = null;
                }
            }
        };
    }

    private SingletonAssembler activateEmbeddedElasticsearch( final String name )
    {
        try
        {
            return new SingletonAssembler()
            {
                @Override
                public void assemble( final ModuleAssembly module ) throws AssemblyException
                {
                    module.layer().application().setName( name );
                    ModuleAssembly config = module.layer().module( "config" );
                    new EntityTestAssembler().assemble( config );
                    new EntityTestAssembler().assemble( module );
                    new FileConfigurationAssembler()
                        .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
                        .assemble( module );
                    new ESFilesystemIndexQueryAssembler()
                        .identifiedBy( name )
                        .withConfig( config, Visibility.layer )
                        .assemble( module );
                }
            };
        }
        catch( ActivationException | AssemblyException ex )
        {
            throw new RuntimeException( "Embedded Elasticsearch Rule - Failed to activate", ex );
        }
    }

    private Client findClient( Module module )
    {
        Client client = module.serviceFinder().findService( ElasticSearchSupport.class ).get().client();
        if( client == null )
        {
            throw new IllegalStateException( "Embedded Elasticsearch Rule - Failed to find client" );
        }
        return client;
    }
}
