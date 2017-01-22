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
package org.apache.polygene.library.http;

import org.eclipse.jetty.server.Server;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.service.importer.InstanceImporter;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ImportedServiceDeclaration;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.ServiceDeclaration;

public class JettyServiceAssembler
    extends Assemblers.VisibilityIdentityConfig<JettyServiceAssembler>
{
    protected ModuleAssembly serverModule;
    protected Visibility serverVisibility = Visibility.module;
    protected String serverIdentity;

    /**
     * @param serverModule Defaults to assembled module
     * @param serverVisibility Defaults to {@link Visibility#module}
     */
    public JettyServiceAssembler withServer( ModuleAssembly serverModule, Visibility serverVisibility )
    {
        this.serverModule = serverModule;
        this.serverVisibility = serverVisibility;
        return this;
    }

    public JettyServiceAssembler serverIdentifiedBy( String serverIdentity )
    {
        this.serverIdentity = serverIdentity;
        return this;
    }

    @Override
    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ServiceDeclaration service = module.services( httpService() ).
            visibleIn( visibility() ).
            instantiateOnStartup();
        if( hasIdentity() )
        {
            service.identifiedBy( identity() );
        }
        if( hasConfig() )
        {
            configModule().entities( configurationEntity() ).visibleIn( configVisibility() );
        }
        assembleServer( module );
    }

    protected final void assembleServer( ModuleAssembly module )
    {
        serverModule = serverModule != null ? serverModule : module;
        ImportedServiceDeclaration server = serverModule.importedServices( Server.class ).
            importedBy( InstanceImporter.class ).
            setMetaInfo( new Server() ).
            visibleIn( serverVisibility );
        if( serverIdentity != null )
        {
            server.identifiedBy( serverIdentity );
        }
    }

    protected Class<? extends JettyConfiguration> configurationEntity()
    {
        return JettyConfiguration.class;
    }

    protected Class<? extends JettyService> httpService()
    {
        return JettyService.class;
    }
}
