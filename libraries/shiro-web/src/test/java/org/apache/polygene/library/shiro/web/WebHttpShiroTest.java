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
package org.apache.polygene.library.shiro.web;

import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.http.JettyConfiguration;
import org.apache.polygene.library.http.JettyServiceAssembler;
import org.apache.polygene.library.shiro.ini.ShiroIniConfiguration;
import org.apache.polygene.library.shiro.web.assembly.HttpShiroAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.util.FreePortFinder;

public class WebHttpShiroTest
    extends AbstractPolygeneTest
{
    private int port;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
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
        new HttpShiroAssembler()
            .withConfig( configModule, Visibility.layer )
            .assemble( module );
        // END SNIPPET: assembly

        configModule.forMixin( ShiroIniConfiguration.class )
                    .declareDefaults().iniResourcePath().set( "classpath:web-shiro.ini" );
    }

    @Test
    public void test()
    {
    }
}
