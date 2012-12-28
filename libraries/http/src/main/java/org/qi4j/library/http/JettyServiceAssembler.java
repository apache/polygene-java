/*
 * Copyright 2008 Edward Yakop.
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.http;

import org.eclipse.jetty.server.Server;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.importer.InstanceImporter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class JettyServiceAssembler
        implements Assembler
{

    protected Visibility serverVisibility = Visibility.module;
    protected ModuleAssembly configModule;
    protected Visibility configVisibility = Visibility.layer;
    protected ModuleAssembly serverModule;
    protected Visibility httpServiceVisibility = Visibility.module;

    /**
     * @param serverVisibility Defaults to {@link Visibility#module}
     */
    public JettyServiceAssembler withServerVisibility( Visibility serverVisibility )
    {
        this.serverVisibility = serverVisibility;
        return this;
    }

    /**
     * @param serverModule Defaults to assembled module
     */
    public JettyServiceAssembler withServerModule( ModuleAssembly serverModule )
    {
        this.serverModule = serverModule;
        return this;
    }

    /**
     * @param configVisibility Defaults to {@link Visibility#layer}
     */
    public JettyServiceAssembler withConfigVisibility( Visibility configVisibility )
    {
        this.configVisibility = configVisibility;
        return this;
    }

    /**
     * @param configModule Defaults to assembled module
     */
    public JettyServiceAssembler withConfigModule( ModuleAssembly configModule )
    {
        this.configModule = configModule;
        return this;
    }

    /**
     * @param httpServiceVisibility Defaults to {@link Visibility#module}
     */
    public JettyServiceAssembler withHttpServiceVisibility( Visibility httpServiceVisibility )
    {
        this.httpServiceVisibility = httpServiceVisibility;
        return this;
    }

    @Override
    public final void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        assembleServer( module );
        configModule = configModule != null ? configModule : module;
        configModule.entities( configurationEntity() ).visibleIn( configVisibility );
        module.services( httpService() ).
                visibleIn( httpServiceVisibility ).
                instantiateOnStartup();
    }

    protected final void assembleServer( ModuleAssembly module )
    {
        serverModule = serverModule != null ? serverModule : module;
        serverModule.importedServices( Server.class ).
                importedBy( InstanceImporter.class ).
                setMetaInfo( new Server() ).
                visibleIn( serverVisibility );
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
