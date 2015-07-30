/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.apache.zest.test.performance.entitystore.memory;

import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entitystore.StateChangeListener;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.test.entity.AbstractEntityStoreTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;

import static org.apache.zest.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;

/**
 * Test of MemoryEntityStoreService
 */
public class MemoryEntityStoreTest
    extends AbstractEntityStoreTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );

        module.services( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class );
        module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
        module.importedServices( StatePrinter.class ).importedBy( NEW_OBJECT );
        module.objects( StatePrinter.class );
    }

    static public class StatePrinter
        implements StateChangeListener
    {
        public void notifyChanges( Iterable<EntityState> changedStates )
        {
            for( EntityState changedState : changedStates )
            {
                System.out.println( changedState.status().name() + ":" + changedState.identity() );
            }
        }
    }
}
