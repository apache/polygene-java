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

package org.apache.zest.envisage.school.infrastructure.persistence;

import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.index.rdf.RdfIndexingEngineService;
import org.apache.zest.index.rdf.query.RdfQueryParserFactory;
import org.apache.zest.library.rdf.entity.EntityStateSerializer;
import org.apache.zest.library.rdf.entity.EntityTypeSerializer;
import org.apache.zest.library.rdf.repository.MemoryRepositoryService;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;

import static org.apache.zest.api.common.Visibility.application;

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
