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

package org.apache.polygene.library.restlet;

import org.apache.polygene.api.structure.Application;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.restlet.assembly.RestApplicationAssembler;
import org.apache.polygene.library.restlet.assembly.configuration.ConfigurationModule;
import org.apache.polygene.library.restlet.assembly.configuration.ConfigurationLayer;
import org.apache.polygene.library.restlet.assembly.connectivity.ConnectivityLayer;
import org.apache.polygene.library.restlet.assembly.domain.DomainLayer;
import org.apache.polygene.library.restlet.assembly.infrastructue.FileStorageModule;
import org.apache.polygene.library.restlet.assembly.infrastructue.InfrastructureLayer;
import org.apache.polygene.library.restlet.assembly.resource.ResourceLayer;

// START SNIPPET: app
public class TestApplication
{
    private static final String NAME = "Test Application";
    private static final String VERSION = "1.0";
    private static final Application.Mode MODE = Application.Mode.development;

    // END SNIPPET: app


    public static void main( String[] args )
        throws Exception
    {
        RestApplicationAssembler assembler = new RestApplicationAssembler( NAME, VERSION, MODE,
                                                                           ConnectivityLayer.class,
                                                                           ResourceLayer.class,
                                                                           DomainLayer.class,
                                                                           InfrastructureLayer.class,
                                                                           ConfigurationLayer.class
        );

        assembler.initialize();

        ModuleAssembly configModule = assembler.layer( ConfigurationLayer.class ).module( ConfigurationModule.NAME );
        assembler.layer( InfrastructureLayer.class,
                         new FileStorageModule( configModule ) );

        assembler.layer( DomainLayer.class,
                         new OrdersModule()
        );
        assembler.start();
        assembler.addShutdownHook();
    }
    // START SNIPPET: app
}
// END SNIPPET: app
