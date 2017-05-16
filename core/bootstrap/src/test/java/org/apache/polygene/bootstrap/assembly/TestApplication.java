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
package org.apache.polygene.bootstrap.assembly;

import org.apache.polygene.api.structure.Application;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.layered.LayeredApplicationAssembler;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.assembly.config.ConfigurationLayer;
import org.apache.polygene.bootstrap.assembly.connectivity.ConnectivityLayer;
import org.apache.polygene.bootstrap.assembly.domain.DomainLayer;
import org.apache.polygene.bootstrap.assembly.infrastructure.InfrastructureLayer;
import org.apache.polygene.bootstrap.assembly.service.ServiceLayer;

// START SNIPPET: application
public class TestApplication extends LayeredApplicationAssembler
{

    public TestApplication( String name, String version, Application.Mode mode )
        throws AssemblyException
    {
        super( name, version, mode );
    }

    @Override
    protected void assembleLayers( ApplicationAssembly assembly )
        throws AssemblyException
    {
        LayerAssembly configLayer = createLayer( ConfigurationLayer.class );
        ModuleAssembly configModule = configLayer.module( "Configuration Module" );
        LayerAssembly infraLayer = new InfrastructureLayer( configModule ).assemble( assembly.layer( InfrastructureLayer.NAME  ));
        LayerAssembly domainLayer = createLayer( DomainLayer.class );
        LayerAssembly serviceLayer = createLayer( ServiceLayer.class );
        LayerAssembly connectivityLayer = createLayer( ConnectivityLayer.class );

        connectivityLayer.uses( serviceLayer );
        connectivityLayer.uses( domainLayer );
        serviceLayer.uses( domainLayer );
        domainLayer.uses( infraLayer );
        infraLayer.uses( configLayer );
    }
}
// END SNIPPET: application
