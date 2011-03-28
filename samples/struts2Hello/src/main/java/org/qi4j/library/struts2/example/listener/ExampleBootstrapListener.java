/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.struts2.example.listener;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.RdfIndexingEngineService;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.library.struts2.ActionConfiguration;
import org.qi4j.library.struts2.Qi4jApplicationBootstrapListener;
import org.qi4j.library.struts2.bootstrap.Struts2PluginAssembler;
import org.qi4j.library.struts2.codebehind.bootstrap.CodebehindAssembler;
import org.qi4j.library.struts2.example.Item;
import org.qi4j.library.struts2.example.actions.AddItem;
import org.qi4j.library.struts2.example.actions.EditItem;
import org.qi4j.library.struts2.example.actions.HelloWorldAction;
import org.qi4j.library.struts2.example.actions.IndexAction;
import org.qi4j.library.struts2.example.actions.ListItems;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

/**
 * @author edward.yakop@gmail.com
 */
public class ExampleBootstrapListener
    extends Qi4jApplicationBootstrapListener
{
    @Override
    protected final ApplicationAssembler createAssembler()
    {
        return new ApplicationAssembler()
        {
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                return applicationFactory.newApplicationAssembly( new Assembler()
                {
                    public void assemble( ModuleAssembly aModule )
                        throws AssemblyException
                    {
                        ActionConfiguration actionConfiguration = new ActionConfiguration();
                        actionConfiguration.addObjects( HelloWorldAction.class, IndexAction.class );
                        actionConfiguration.addComposites( AddItem.class, EditItem.class, ListItems.class );

                        new Struts2PluginAssembler( actionConfiguration ).assemble( aModule );
                        new CodebehindAssembler().assemble( aModule );

                        aModule.entities( Item.class );
                        aModule.services(
                            MemoryEntityStoreService.class,
                            UuidIdentityGeneratorService.class,
                            MemoryRepositoryService.class,
                            RdfIndexingEngineService.class
                        );
                        aModule.objects( EntityStateSerializer.class );
                    }
                } );
            }
        };
    }

    @Override
    protected final Module qi4jStrutsModule( Application application )
    {
        return application.findModule( "Layer 1", "Module 1" );
    }
}
