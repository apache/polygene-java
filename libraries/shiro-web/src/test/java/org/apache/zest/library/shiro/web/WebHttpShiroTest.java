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
package org.apache.zest.library.shiro.web;

import java.io.IOException;
import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.http.JettyConfiguration;
import org.apache.zest.library.http.JettyServiceAssembler;
import org.apache.zest.library.shiro.ini.ShiroIniConfiguration;
import org.apache.zest.library.shiro.web.assembly.HttpShiroAssembler;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.util.FreePortFinder;

public class WebHttpShiroTest
    extends AbstractZestTest
{
    private int port;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        try
        {
            ModuleAssembly configModule = module;
            new EntityTestAssembler().assemble( configModule );
            // START SNIPPET: assembly
            new JettyServiceAssembler().withConfig( configModule, Visibility.layer ).assemble( module );
            // END SNIPPET: assembly

            port = FreePortFinder.findFreePortOnLoopback();
            JettyConfiguration config = module.forMixin( JettyConfiguration.class ).declareDefaults();
            config.hostName().set( "127.0.0.1" );
            config.port().set( port );

            // START SNIPPET: assembly
            new HttpShiroAssembler().
                withConfig( configModule, Visibility.layer ).
                assemble( module );
            // END SNIPPET: assembly

            configModule.forMixin( ShiroIniConfiguration.class ).
                declareDefaults().
                iniResourcePath().set( "classpath:web-shiro.ini" );
        }
        catch( IOException ex )
        {
            throw new AssemblyException( "Unable to find free port to bind to", ex );
        }
    }

    @Test
    public void test()
    {
    }
}
