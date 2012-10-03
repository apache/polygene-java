/*
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.runtime.structure;

import java.util.Iterator;
import org.junit.Test;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.functional.Iterables;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.*;

/**
 * This test assert that Type to Composite lookup succeed for Objects, Transients, Values, Entities and Services.
 */
public class TypeToCompositeLookupTest
{

    private static final String CATHEDRAL = "cathedral";

    private static final String BAZAR = "bazar";

    public interface Foo
    {

        String bar();

    }

    public static class FooObject
            implements Foo
    {

        public String bar()
        {
            return BAZAR;
        }

    }

    public static class SomeFooObject
            extends FooObject
    {

        @Override
        public String bar()
        {
            return CATHEDRAL;
        }

    }

    @Mixins( FooObject.class )
    public interface FooTransient
            extends Foo, TransientComposite
    {
    }

    @Mixins( SomeFooObject.class )
    public interface SomeFooTransient
            extends FooTransient
    {
    }

    @Mixins( FooObject.class )
    public interface FooValue
            extends Foo, ValueComposite
    {
    }

    @Mixins( SomeFooObject.class )
    public interface SomeFooValue
            extends FooValue
    {
    }

    @Mixins( FooObject.class )
    public interface FooEntity
            extends Foo, EntityComposite
    {
    }

    @Mixins( SomeFooObject.class )
    public interface SomeFooEntity
            extends FooEntity
    {
    }

    @Mixins( FooObject.class )
    public interface FooService
            extends Foo, ServiceComposite
    {
    }

    @Mixins( SomeFooObject.class )
    public interface SomeFooService
            extends FooService
    {
    }

    @Test
    public void objects()
    {
        Module module = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.objects( SomeFooObject.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newObject( SomeFooObject.class ).bar() );
        assertEquals( CATHEDRAL, module.newObject( FooObject.class ).bar() );
        assertEquals( CATHEDRAL, module.newObject( Foo.class ).bar() );
    }

    @Test
    public void objectsAmbiguousDeclaration()
    {
        Module module = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.objects( SomeFooObject.class, FooObject.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newObject( SomeFooObject.class ).bar() );
        assertEquals( BAZAR, module.newObject( FooObject.class ).bar() );

        try {

            module.newObject( Foo.class );
            fail( "Ambiguous type exception not detected for Objects" );

        } catch ( AmbiguousTypeException expected ) {
        }
    }

    @Test
    public void transients()
    {
        Module module = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.transients( SomeFooTransient.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newTransientBuilder( SomeFooTransient.class ).newInstance().bar() );
        assertEquals( CATHEDRAL, module.newTransientBuilder( FooTransient.class ).newInstance().bar() );
        assertEquals( CATHEDRAL, module.newTransientBuilder( Foo.class ).newInstance().bar() );
    }

    @Test
    public void transientsAmbiguousDeclaration()
    {
        Module module = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.transients( SomeFooTransient.class, FooTransient.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newTransientBuilder( SomeFooTransient.class ).newInstance().bar() );
        assertEquals( BAZAR, module.newTransientBuilder( FooTransient.class ).newInstance().bar() );

        try {

            module.newTransientBuilder( Foo.class );
            fail( "Ambiguous type exception not detected for Transients" );

        } catch ( AmbiguousTypeException expected ) {
        }
    }

    @Test
    public void values()
    {
        Module module = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.values( SomeFooValue.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newValueBuilder( SomeFooValue.class ).newInstance().bar() );
        assertEquals( CATHEDRAL, module.newValueBuilder( FooValue.class ).newInstance().bar() );
        assertEquals( CATHEDRAL, module.newValueBuilder( Foo.class ).newInstance().bar() );
    }

    @Test
    public void valuesAmbiguousDeclaration()
    {
        Module module = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.values( SomeFooValue.class, FooValue.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newValueBuilder( SomeFooValue.class ).newInstance().bar() );
        assertEquals( BAZAR, module.newValueBuilder( FooValue.class ).newInstance().bar() );

        try {

            module.newValueBuilder( Foo.class );
            fail( "Ambiguous type exception not detected for Values" );

        } catch ( AmbiguousTypeException expected ) {
        }
    }

    @Test
    public void entities()
    {
        Module module = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                new EntityTestAssembler().assemble( module );
                module.entities( SomeFooEntity.class );
            }

        }.module();

        UnitOfWork uow = module.newUnitOfWork();
        try {

            assertEquals( CATHEDRAL, uow.newEntityBuilder( SomeFooEntity.class ).newInstance().bar() );
            assertEquals( CATHEDRAL, uow.newEntityBuilder( FooEntity.class ).newInstance().bar() );
            assertEquals( CATHEDRAL, uow.newEntityBuilder( Foo.class ).newInstance().bar() );

        } finally {
            uow.discard();
        }
    }

    @Test
    public void entitiesAmbiguousDeclaration()
    {
        Module module = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                new EntityTestAssembler().assemble( module );
                module.entities( SomeFooEntity.class, FooEntity.class );
            }

        }.module();

        UnitOfWork uow = module.newUnitOfWork();
        try {

            // Specific Type used
            assertEquals( CATHEDRAL, uow.newEntityBuilder( SomeFooEntity.class ).newInstance().bar() );

            // Specific Type used
            assertEquals( BAZAR, uow.newEntityBuilder( FooEntity.class ).newInstance().bar() );

            // First matching Type used
            assertEquals( CATHEDRAL, uow.newEntityBuilder( Foo.class ).newInstance().bar() );

        } finally {
            uow.discard();
        }
    }

    @Test
    public void services()
    {
        Module module = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.services( SomeFooService.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.findService( SomeFooService.class ).get().bar() );
        assertEquals( CATHEDRAL, module.findService( FooService.class ).get().bar() );
        assertEquals( CATHEDRAL, module.findService( Foo.class ).get().bar() );
    }

    @Test
    public void servicesPluralDeclaration()
    {
        Module module = new SingletonAssembler()
        {

            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.services( SomeFooService.class, FooService.class );
            }

        }.module();

        assertEquals( 1, Iterables.count( module.findServices( SomeFooService.class ) ) );
        assertEquals( 2, Iterables.count( module.findServices( FooService.class ) ) );
        assertEquals( 2, Iterables.count( module.findServices( Foo.class ) ) );

        assertEquals( CATHEDRAL, module.findService( SomeFooService.class ).get().bar() );

        // Follows assembly Type order
        Iterator<ServiceReference<FooService>> fooServices = module.findServices( FooService.class ).iterator();
        assertEquals( CATHEDRAL, fooServices.next().get().bar() );
        assertEquals( BAZAR, fooServices.next().get().bar() );

        // Follows assembly Type order
        Iterator<ServiceReference<Foo>> foos = module.findServices( Foo.class ).iterator();
        assertEquals( CATHEDRAL, foos.next().get().bar() );
        assertEquals( BAZAR, foos.next().get().bar() );
    }

}
