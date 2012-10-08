/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.shiro.web;

import java.io.IOException;
import java.util.Collections;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.http.JettyConfiguration;
import org.qi4j.library.http.JettyServiceAssembler;
import org.qi4j.library.shiro.ini.ShiroIniConfiguration;
import org.qi4j.library.shiro.web.assembly.HttpShiroAssembler;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.util.FreePortFinder;

import static javax.servlet.DispatcherType.*;
import static org.qi4j.library.http.Servlets.*;

public class WebHttpShiroTest
        extends AbstractQi4jTest
{

    private int port;

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        try {

            module.services( MemoryEntityStoreService.class );
            ModuleAssembly configModule = module;
            // START SNIPPET: assembly
            new JettyServiceAssembler().assemble( module );
            // END SNIPPET: assembly

            port = FreePortFinder.findFreePortOnLoopback();
            JettyConfiguration config = module.forMixin( JettyConfiguration.class ).declareDefaults();
            config.hostName().set( "127.0.0.1" );
            config.port().set( port );

            // START SNIPPET: assembly
            new HttpShiroAssembler().withConfig( configModule ).assemble( module );
            // END SNIPPET: assembly

            configModule.forMixin( ShiroIniConfiguration.class ).
                    declareDefaults().
                    iniResourcePath().set( "classpath:web-shiro.ini" );

            if ( false ) {
                addContextListeners( listen().
                        with( EnvironmentLoaderService.class ).
                        withInitParams( Collections.singletonMap( "shiroConfigLocations", "classpath:web-shiro.ini" ) ) ).
                        to( module );

                addFilters( filter( "/*" ).
                        through( ShiroFilterService.class ).
                        on( REQUEST, FORWARD, INCLUDE, ERROR ) ).
                        to( module );
            }

        } catch ( IOException ex ) {
            throw new AssemblyException( "Unable to find free port to bind to", ex );
        }

    }

    @Test
    public void test()
    {
    }

}
