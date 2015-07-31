/*
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.envisage.school.infrastructure.persistence;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.RdfIndexingEngineService;
import org.qi4j.index.rdf.query.RdfQueryParserFactory;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

import static org.qi4j.api.common.Visibility.application;

public class PersistenceAssembler
    implements Assembler
{
    @Override
    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );

        module.services(
            UuidIdentityGeneratorService.class,
            MemoryEntityStoreService.class,
            // Query
            RdfQueryParserFactory.class, RdfIndexingEngineService.class,
            MemoryRepositoryService.class
        ).visibleIn( application ).instantiateOnStartup();
    }
}
