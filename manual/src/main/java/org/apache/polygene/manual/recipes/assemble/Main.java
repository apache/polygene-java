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
package org.apache.polygene.manual.recipes.assemble;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.bootstrap.*;

// START SNIPPET: main
public class Main
{
    private static Energy4Java polygene;
    private static Application application;

    public static void main( String[] args )
            throws Exception
    {
        // Bootstrap Polygene Runtime
        // Create a Polygene Runtime
        polygene = new Energy4Java();

        // Instantiate the Application Model.
        application = polygene.newApplication(
            factory ->
            {
                ApplicationAssembly assembly = factory.newApplicationAssembly();
                LayerAssembly runtime = createRuntimeLayer( assembly );
                LayerAssembly designer = createDesignerLayer( assembly );
                LayerAssembly domain = createDomainLayer( assembly );
                LayerAssembly messaging = createMessagingLayer( assembly );
                LayerAssembly persistence = createPersistenceLayer( assembly );

                // declare structure between layers
                domain.uses( messaging );
                domain.uses( persistence );
                designer.uses( persistence );
                designer.uses( domain );
                runtime.uses( domain );

                return assembly;
            } );

        // We need to handle shutdown.
        installShutdownHook();

        // Activate the Application Runtime.
        application.activate();
    }

// END SNIPPET: main
    private static LayerAssembly createRuntimeLayer( ApplicationAssembly app )
    {
        return app.layer( "runtime-layer" );
    }

    private static LayerAssembly createDesignerLayer( ApplicationAssembly app )
    {
        return app.layer( "designer-layer" );
    }

    private static LayerAssembly createMessagingLayer( ApplicationAssembly app )
    {
        return app.layer( "messaging-layer" );
    }

    private static LayerAssembly createPersistenceLayer( ApplicationAssembly app )
    {
        return app.layer( "persistence-layer" );
    }

// START SNIPPET: domainLayer
    private static LayerAssembly createDomainLayer( ApplicationAssembly app )
    {
        LayerAssembly layer = app.layer("domain-layer");
        createAccountModule( layer );
        createInventoryModule( layer );
        createReceivablesModule( layer );
        createPayablesModule( layer );
        return layer;
    }

// END SNIPPET: domainLayer
// START SNIPPET: accountModule
    private static void createAccountModule( LayerAssembly layer )
    {
        ModuleAssembly module = layer.module("account-module");

        module.entities(AccountEntity.class, EntryEntity.class);

        module.addServices(
                AccountRepositoryService.class,
                AccountFactoryService.class,
                EntryFactoryService.class,
                EntryRepositoryService.class
        ).visibleIn( Visibility.layer );
    }

// END SNIPPET: accountModule
    private static void createPayablesModule( LayerAssembly layer )
    {
        layer.module("payables-module");
    }

    private static void createInventoryModule( LayerAssembly layer )
    {
        layer.module("inventory-module");
    }

    private static void createReceivablesModule( LayerAssembly layer )
    {
        layer.module("receivables-module");
    }

// START SNIPPET: shutdown
    private static void installShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
        {
            public void run()
            {
                if( application != null )
                {
                    try
                    {
                        application.passivate();
                    }
                    catch( Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        }) );
    }
// END SNIPPET: shutdown
// START SNIPPET: main
}

// END SNIPPET: main
interface AccountEntity {}
interface EntryEntity {}

interface AccountFactoryService {}
interface EntryFactoryService {}
interface EntryRepositoryService {}
interface AccountRepositoryService {}
