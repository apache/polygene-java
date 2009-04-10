/*  Copyright 2008 Rickard Öberg.
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
package org.qi4j.entitystore.coherence;

import org.junit.After;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.entity.helpers.UuidIdentityGeneratorService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * JAVADOC
 */
public class CoherenceEntityStoreTest extends AbstractEntityStoreTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        super.assemble( module );
        module.addServices( CoherenceEntityStoreService.class, UuidIdentityGeneratorService.class );

        ModuleAssembly config = module.layerAssembly().newModuleAssembly( "config" );
        config.addEntities( CoherenceConfiguration.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreService.class );
    }

    @Test
    @Override public void whenRemovedEntityThenCannotFindEntity() throws Exception
    {
        super.whenRemovedEntityThenCannotFindEntity();
    }

    @Test
    @Override public void givenPropertyIsModifiedWhenUnitOfWorkCompletesThenStoreState() throws UnitOfWorkCompletionException
    {
        super.givenPropertyIsModifiedWhenUnitOfWorkCompletesThenStoreState();
    }

    @Test
    @Override public void givenEntityIsNotModifiedWhenUnitOfWorkCompletesThenDontStoreState() throws UnitOfWorkCompletionException
    {
        super.givenEntityIsNotModifiedWhenUnitOfWorkCompletesThenDontStoreState();
    }

    @Override @After public void tearDown() throws Exception
    {
        super.tearDown();
    }
}