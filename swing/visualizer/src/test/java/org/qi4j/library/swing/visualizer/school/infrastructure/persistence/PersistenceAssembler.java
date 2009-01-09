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
package org.qi4j.library.swing.visualizer.school.infrastructure.persistence;

import static org.qi4j.api.common.Visibility.application;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.index.rdf.RdfFactoryService;
import org.qi4j.entity.index.rdf.RdfQueryService;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.spi.entity.helpers.UuidIdentityGeneratorService;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public class PersistenceAssembler
    implements Assembler
{
    public final void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.addObjects( EntityStateSerializer.class );

        aModule.addServices(
            UuidIdentityGeneratorService.class,
            MemoryEntityStoreService.class,

            // Query
            RdfQueryService.class, RdfFactoryService.class,
            MemoryRepositoryService.class )
            .visibleIn( application )
            .instantiateOnStartup();
    }
}
