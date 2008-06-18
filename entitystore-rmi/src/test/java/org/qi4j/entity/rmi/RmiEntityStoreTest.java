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
package org.qi4j.entity.rmi;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * Test the RMI store
 */
public class RmiEntityStoreTest
    extends AbstractEntityStoreTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        super.assemble( module );
        module.setName( "Module 1" );
        module.addServices( ClientRmiEntityStoreService.class );

        ModuleAssembly remote = module.getLayerAssembly().newModuleAssembly();
        remote.setName( "Server" );
        remote.addEntities( TestEntity.class, TestValue.class, RegistryConfiguration.class );
        remote.addServices( ServerRmiEntityStoreService.class,
                            RegistryService.class,
                            MemoryEntityStoreService.class ).instantiateOnStartup();
    }
}