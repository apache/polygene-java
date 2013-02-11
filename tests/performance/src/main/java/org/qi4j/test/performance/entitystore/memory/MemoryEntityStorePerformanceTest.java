/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.test.performance.entitystore.memory;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.entitystore.helpers.MapEntityStoreMixin;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.performance.entitystore.model.AbstractEntityStorePerformanceTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

public class MemoryEntityStorePerformanceTest
    extends AbstractEntityStorePerformanceTest
{

    public MemoryEntityStorePerformanceTest()
    {
        super( "MemoryEntityStore", createAssembler() );
    }

    private static Assembler createAssembler()
    {
        return new Assembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.services( MemoryEntityStoreService.class );
//                module.services( MemoryEntityStoreService2.class );
                module.services( UuidIdentityGeneratorService.class );
                new OrgJsonValueSerializationAssembler().assemble( module );
            }
        };
    }

    // Alternate variant that uses the standard MapEntityStore
    @Mixins( MapEntityStoreMixin.class )
    interface MemoryEntityStoreService2
        extends MemoryEntityStoreService
    {
    }

}
