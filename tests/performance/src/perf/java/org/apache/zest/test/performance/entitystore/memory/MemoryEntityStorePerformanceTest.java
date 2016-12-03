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
 */
package org.apache.zest.test.performance.entitystore.memory;

import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.assembly.MemoryEntityStoreAssembler;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.spi.entitystore.helpers.MapEntityStoreMixin;
import org.apache.zest.test.performance.entitystore.AbstractEntityStorePerformanceTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

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
