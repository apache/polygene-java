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

import java.lang.reflect.UndeclaredThrowableException;
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
import org.apache.polygene.test.TemporaryFolder;
import org.elasticsearch.client.Client;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.findFields;

/**
 * Embedded Elasticsearch JUnit Rule.
 * <p>
 * Starting from Elasticsearch 5, startup is way slower.
 * Reuse an embedded instance across tests.
 */
@ExtendWith( TemporaryFolder.class )
public class EmbeddedElasticSearchExtension
    implements BeforeAllCallback, AfterAllCallback
{
    private TemporaryFolder tmpDir;
    private Client client;
    private Application application;

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

    private SingletonAssembler activateEmbeddedElasticsearch( final String name )
    {
        try
        {
            return new SingletonAssembler(
                module -> {
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
            );
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

    @Override
    public void beforeAll( ExtensionContext context )
        throws Exception
    {
        this.tmpDir = new TemporaryFolder();
        this.tmpDir.beforeEach( context );

        String name = indexName( context.getRequiredTestClass().getSimpleName(), context.getRequiredTestMethod().getName() );
        SingletonAssembler assembler = activateEmbeddedElasticsearch( name );
        application = assembler.application();
        client = findClient( assembler.module() );
        inject( context );
    }

    private void inject( ExtensionContext context )
    {
        findFields( context.getRequiredTestClass(),
                    f -> f.getType().equals( EmbeddedElasticSearchExtension.class ), BOTTOM_UP )
            .forEach( f -> {
                try
                {
                    f.setAccessible( true );
                    f.set( context.getRequiredTestInstance(), this );
                }
                catch( IllegalAccessException e )
                {
                    throw new UndeclaredThrowableException( e );
                }
            } );
    }

    @Override
    public void afterAll( ExtensionContext context )
        throws Exception
    {
        application.passivate();
        client.close();
        client = null;
        tmpDir.afterEach( context );
    }
}
