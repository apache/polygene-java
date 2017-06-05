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
package org.apache.polygene.runtime.structure;

import java.util.Iterator;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.composite.AmbiguousTypeException;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Theses tests ensure that Type to Composite lookup work as expected for
 * Objects, Transients, Values, Entities and Services.
 */
public class TypeToCompositeLookupTest
{

    private static final String CATHEDRAL = "cathedral";
    private static final String BAZAR = "bazar";

    public interface Foo
    {
        String bar();
    }

    public static class BasicFooImpl
        implements Foo
    {

        @Override
        public String bar()
        {
            return BAZAR;
        }

    }

    public static class SomeOtherFooImpl
        extends BasicFooImpl
    {

        @Override
        public String bar()
        {
            return CATHEDRAL;
        }

    }

    @Mixins( BasicFooImpl.class )
    public interface BasicFoo
        extends Foo
    {
    }

    @Mixins( SomeOtherFooImpl.class )
    public interface SomeOtherFoo
        extends BasicFoo
    {
    }

    @Test
    public void objects()
        throws ActivationException, AssemblyException
    {
        Module module = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( SomeOtherFooImpl.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newObject( SomeOtherFooImpl.class ).bar() );
        assertEquals( CATHEDRAL, module.newObject( BasicFooImpl.class ).bar() );
        assertEquals( CATHEDRAL, module.newObject( Foo.class ).bar() );
    }

    @Test
    public void objectsAmbiguousDeclaration()
        throws ActivationException, AssemblyException
    {
        Module module = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( SomeOtherFooImpl.class, BasicFooImpl.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newObject( SomeOtherFooImpl.class ).bar() );
        assertEquals( BAZAR, module.newObject( BasicFooImpl.class ).bar() );

        try
        {
            module.newObject( Foo.class );
            fail( "Ambiguous type exception not detected for Objects" );
        }
        catch( AmbiguousTypeException expected )
        {
        }
    }

    @Test
    public void transients()
        throws ActivationException, AssemblyException
    {
        Module module = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( SomeOtherFoo.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newTransientBuilder( SomeOtherFoo.class ).newInstance().bar() );
        assertEquals( CATHEDRAL, module.newTransientBuilder( BasicFoo.class ).newInstance().bar() );
        assertEquals( CATHEDRAL, module.newTransientBuilder( Foo.class ).newInstance().bar() );
    }

    @Test
    public void transientsAmbiguousDeclaration()
        throws ActivationException, AssemblyException
    {
        Module module = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( SomeOtherFoo.class, BasicFoo.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newTransientBuilder( SomeOtherFoo.class ).newInstance().bar() );
        assertEquals( BAZAR, module.newTransientBuilder( BasicFoo.class ).newInstance().bar() );

        try
        {
            module.newTransientBuilder( Foo.class );
            fail( "Ambiguous type exception not detected for Transients" );
        }
        catch( AmbiguousTypeException expected )
        {
        }
    }

    @Test
    public void values()
        throws ActivationException, AssemblyException
    {
        Module module = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.values( SomeOtherFoo.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newValueBuilder( SomeOtherFoo.class ).newInstance().bar() );
        assertEquals( CATHEDRAL, module.newValueBuilder( BasicFoo.class ).newInstance().bar() );
        assertEquals( CATHEDRAL, module.newValueBuilder( Foo.class ).newInstance().bar() );
    }

    @Test
    public void valuesAmbiguousDeclaration()
        throws ActivationException, AssemblyException
    {
        Module module = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.values( SomeOtherFoo.class, BasicFoo.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.newValueBuilder( SomeOtherFoo.class ).newInstance().bar() );
        assertEquals( BAZAR, module.newValueBuilder( BasicFoo.class ).newInstance().bar() );

        try
        {
            module.newValueBuilder( Foo.class );
            fail( "Ambiguous type exception not detected for Values" );
        }
        catch( AmbiguousTypeException expected )
        {
        }
    }

    @Test
    public void entities()
        throws UnitOfWorkCompletionException, ActivationException, AssemblyException
    {
        UnitOfWorkFactory uowf = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                new EntityTestAssembler().assemble( module );
                module.entities( SomeOtherFoo.class );
            }

        }.module().unitOfWorkFactory();

        UnitOfWork uow = uowf.newUnitOfWork();

        SomeOtherFoo someOtherFoo = uow.newEntityBuilder( SomeOtherFoo.class ).newInstance();
        BasicFoo basicFoo = uow.newEntityBuilder( BasicFoo.class ).newInstance();
        Foo foo = uow.newEntityBuilder( Foo.class ).newInstance();

        assertEquals( CATHEDRAL, someOtherFoo.bar() );
        assertEquals( CATHEDRAL, basicFoo.bar() );
        assertEquals( CATHEDRAL, foo.bar() );

        Identity someOtherFooIdentity = ((HasIdentity) someOtherFoo).identity().get();
        Identity basicFooIdentity = ((HasIdentity) basicFoo).identity().get();
        Identity fooIdentity = ((HasIdentity) foo).identity().get();

        uow.complete();

        uow = uowf.newUnitOfWork();

        uow.get( SomeOtherFoo.class,  someOtherFooIdentity );
        uow.get( BasicFoo.class, basicFooIdentity );
        uow.get( Foo.class,  fooIdentity );

        uow.discard();
    }

    @Test
    public void entitiesAmbiguousDeclaration()
        throws UnitOfWorkCompletionException, ActivationException, AssemblyException
    {
        UnitOfWorkFactory uowf = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                new EntityTestAssembler().assemble( module );
                module.entities( SomeOtherFoo.class, BasicFoo.class );
            }

        }.module().unitOfWorkFactory();

        UnitOfWork uow = uowf.newUnitOfWork();

        SomeOtherFoo someOtherFoo = uow.newEntityBuilder( SomeOtherFoo.class ).newInstance();
        BasicFoo basicFoo = uow.newEntityBuilder( BasicFoo.class ).newInstance();
        try
        {
            uow.newEntityBuilder( Foo.class ).newInstance();
            fail( "Ambiguous type exception not detected for Entities" );
        }
        catch( AmbiguousTypeException expected )
        {
        }

        // Specific Type used
        assertEquals( CATHEDRAL, uow.newEntityBuilder( SomeOtherFoo.class ).newInstance().bar() );

        // Specific Type used
        assertEquals( BAZAR, uow.newEntityBuilder( BasicFoo.class ).newInstance().bar() );

        Identity someOtherFooIdentity = ((HasIdentity) someOtherFoo).identity().get();
        Identity basicFooIdentity = ((HasIdentity) basicFoo).identity().get();

        uow.complete();

        uow = uowf.newUnitOfWork();

        assertEquals( CATHEDRAL, uow.get( SomeOtherFoo.class, someOtherFooIdentity ).bar() );
        assertEquals( BAZAR, uow.get( BasicFoo.class, basicFooIdentity ).bar() );
        assertEquals( CATHEDRAL, uow.get( Foo.class, someOtherFooIdentity ).bar() );
        assertEquals( BAZAR, uow.get( Foo.class, basicFooIdentity ).bar() );

        uow.discard();
    }

    @Test
    public void services()
        throws ActivationException, AssemblyException
    {
        Module module = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.services( SomeOtherFoo.class );
            }

        }.module();

        assertEquals( CATHEDRAL, module.findService( SomeOtherFoo.class ).get().bar() );
        assertEquals( CATHEDRAL, module.findService( BasicFoo.class ).get().bar() );
        assertEquals( CATHEDRAL, module.findService( Foo.class ).get().bar() );
    }

    @Test
    public void servicesPluralDeclaration()
        throws ActivationException, AssemblyException
    {
        Module module = new SingletonAssembler()
        {

            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.services( SomeOtherFoo.class, BasicFoo.class );
            }

        }.module();

        assertEquals( 1, module.findServices( SomeOtherFoo.class ).count() );
        assertEquals( 2, module.findServices( BasicFoo.class ).count() );
        assertEquals( 2, module.findServices( Foo.class ).count() );

        assertEquals( CATHEDRAL, module.findService( SomeOtherFoo.class ).get().bar() );

        // Exact type match first even if it is assembled _after_ an assignable, the assignable comes after
        Iterator<ServiceReference<BasicFoo>> basicFoos = module.findServices( BasicFoo.class ).iterator();
        assertEquals( BAZAR, basicFoos.next().get().bar() );
        assertEquals( CATHEDRAL, basicFoos.next().get().bar() );
        assertFalse( basicFoos.hasNext() );

        // No exact type match, all assembled are assignable, follows assembly Type order
        Iterator<ServiceReference<Foo>> foos = module.findServices( Foo.class ).iterator();
        assertEquals( CATHEDRAL, foos.next().get().bar() );
        assertEquals( BAZAR, foos.next().get().bar() );
        assertFalse( foos.hasNext() );
    }

}
