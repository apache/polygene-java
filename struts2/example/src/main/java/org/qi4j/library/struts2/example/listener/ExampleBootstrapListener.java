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

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.index.rdf.RdfQueryService;
import org.qi4j.entity.index.rdf.RdfFactoryService;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.library.rdf.entity.EntitySerializer;
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
import org.qi4j.spi.entity.helpers.UuidIdentityGeneratorService;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;

/**
 * @author edward.yakop@gmail.com
 */
public class ExampleBootstrapListener extends Qi4jApplicationBootstrapListener
{
    @Override
    protected final Assembler createAssembler()
    {
        return new Assembler()
        {
            public void assemble( ModuleAssembly aModule )
                throws AssemblyException
            {
                ActionConfiguration actionConfiguration = new ActionConfiguration();
                actionConfiguration.addObjects( HelloWorldAction.class, IndexAction.class );
                actionConfiguration.addComposites( AddItem.class, EditItem.class, ListItems.class );

                aModule.addAssembler( new Struts2PluginAssembler( actionConfiguration ) );
                aModule.addAssembler( new CodebehindAssembler() );

                aModule.addEntities( Item.class );
                aModule.addServices(
                    MemoryEntityStoreService.class,
                    UuidIdentityGeneratorService.class,
                    MemoryRepositoryService.class,
                    RdfQueryService.class, RdfFactoryService.class
                );
                aModule.addObjects( EntitySerializer.class );
            }
        };
    }

    @Override
    protected final Module getQi4jStrutsModule( Application anApplication )
    {
        return anApplication.findModule( "Layer 1", "Module 1" );
    }
}
