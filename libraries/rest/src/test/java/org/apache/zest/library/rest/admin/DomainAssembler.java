/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.apache.zest.library.rest.admin;

import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.index.rdf.RdfIndexingEngineService;
import org.apache.zest.library.rdf.repository.MemoryRepositoryService;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;

import static org.apache.zest.api.common.Visibility.application;
import static org.apache.zest.api.common.Visibility.layer;

/**
 * JAVADOC
 */
public class DomainAssembler
    implements Assembler
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services(
            MemoryEntityStoreService.class,
            UuidIdentityGeneratorService.class,
            RdfIndexingEngineService.class
        ).visibleIn( application );
        module.services( MemoryRepositoryService.class ).identifiedBy( "rdf-indexing" ).visibleIn( layer );

        module.entities( TestEntity.class, TestRole.class, TestEntity2.class ).visibleIn( application );
        module.values( TestValue.class ).visibleIn( application );
        module.services( DummyDataService.class ).instantiateOnStartup();
    }
}
