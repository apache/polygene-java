/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.entitystore.helpers.MapEntityStoreMixin;
import org.qi4j.test.performance.entitystore.AbstractEntityStorePerformanceTest;
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
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                new MemoryEntityStoreAssembler().assemble( module );
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
