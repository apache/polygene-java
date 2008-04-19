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


package org.qi4j.entity.jgroups;

import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.test.entity.AbstractEntityStoreTest;

/**
 * Test of JGroups EntityStore backend.
 */
public class JGroupsEntityStoreTest
    extends AbstractEntityStoreTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        super.assemble( module );
        module.addServices( JGroupsEntityStoreService.class );
    }

    @Test
    public void whenNewEntityThenFindInReplica()
        throws Exception
    {
        // Create first app
        SingletonAssembler app1 = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addServices( JGroupsEntityStoreService.class, UuidIdentityGeneratorService.class ).instantiateOnStartup();
                module.addComposites( TestEntity.class );
            }
        };

        // Create second app
        SingletonAssembler app2 = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addServices( JGroupsEntityStoreService.class, UuidIdentityGeneratorService.class ).instantiateOnStartup();
                module.addComposites( TestEntity.class );
            }
        };

        // Create entity in app 1
        System.out.println( "Create entity" );
        UnitOfWork app1Unit = app1.getUnitOfWorkFactory().newUnitOfWork();
        TestEntity instance = app1Unit.newEntityBuilder( TestEntity.class ).newInstance();
        instance.name().set( "Foo" );
        app1Unit.complete();

//        Thread.sleep( 5000 );

        // Find entity in app 2
        System.out.println( "Find entity" );
        UnitOfWork app2Unit = app2.getUnitOfWorkFactory().newUnitOfWork();
        instance = app2Unit.dereference( instance );

        System.out.println( instance.name() );
        app2Unit.discard();

    }
}