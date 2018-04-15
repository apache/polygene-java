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
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

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

        assertThat( module.newObject( SomeOtherFooImpl.class ).bar(), equalTo( CATHEDRAL ) );
        assertThat( module.newObject( BasicFooImpl.class ).bar(), equalTo( CATHEDRAL ) );
        assertThat( module.newObject( Foo.class ).bar(), equalTo( CATHEDRAL ) );
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

        assertThat( module.newObject( SomeOtherFooImpl.class ).bar(), equalTo( CATHEDRAL ) );
        assertThat( module.newObject( BasicFooImpl.class ).bar(), equalTo( BAZAR ) );

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

        assertThat( module.newTransientBuilder( SomeOtherFoo.class ).newInstance().bar(), equalTo( CATHEDRAL ) );
        assertThat( module.newTransientBuilder( BasicFoo.class ).newInstance().bar(), equalTo( CATHEDRAL ) );
        assertThat( module.newTransientBuilder( Foo.class ).newInstance().bar(), equalTo( CATHEDRAL ) );
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

        assertThat( module.newTransientBuilder( SomeOtherFoo.class ).newInstance().bar(), equalTo( CATHEDRAL ) );
        assertThat( module.newTransientBuilder( BasicFoo.class ).newInstance().bar(), equalTo( BAZAR ) );

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

        assertThat( module.newValueBuilder( SomeOtherFoo.class ).newInstance().bar(), equalTo( CATHEDRAL ) );
        assertThat( module.newValueBuilder( BasicFoo.class ).newInstance().bar(), equalTo( CATHEDRAL ) );
        assertThat( module.newValueBuilder( Foo.class ).newInstance().bar(), equalTo( CATHEDRAL ) );
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

        assertThat( module.newValueBuilder( SomeOtherFoo.class ).newInstance().bar(), equalTo( CATHEDRAL ) );
        assertThat( module.newValueBuilder( BasicFoo.class ).newInstance().bar(), equalTo( BAZAR ) );

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

        assertThat( someOtherFoo.bar(), equalTo( CATHEDRAL ) );
        assertThat( basicFoo.bar(), equalTo( CATHEDRAL ) );
        assertThat( foo.bar(), equalTo( CATHEDRAL ) );

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
        assertThat( uow.newEntityBuilder( SomeOtherFoo.class ).newInstance().bar(), equalTo( CATHEDRAL ) );

        // Specific Type used
        assertThat( uow.newEntityBuilder( BasicFoo.class ).newInstance().bar(), equalTo( BAZAR ) );

        Identity someOtherFooIdentity = ((HasIdentity) someOtherFoo).identity().get();
        Identity basicFooIdentity = ((HasIdentity) basicFoo).identity().get();

        uow.complete();

        uow = uowf.newUnitOfWork();

        assertThat( uow.get( SomeOtherFoo.class, someOtherFooIdentity ).bar(), equalTo( CATHEDRAL ) );
        assertThat( uow.get( BasicFoo.class, basicFooIdentity ).bar(), equalTo( BAZAR ) );
        assertThat( uow.get( Foo.class, someOtherFooIdentity ).bar(), equalTo( CATHEDRAL ) );
        assertThat( uow.get( Foo.class, basicFooIdentity ).bar(), equalTo( BAZAR ) );

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

        assertThat( module.findService( SomeOtherFoo.class ).get().bar(), equalTo( CATHEDRAL ) );
        assertThat( module.findService( BasicFoo.class ).get().bar(), equalTo( CATHEDRAL ) );
        assertThat( module.findService( Foo.class ).get().bar(), equalTo( CATHEDRAL ) );
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

        assertThat( module.findServices( SomeOtherFoo.class ).count(), equalTo( 1 ) );
        assertThat( module.findServices( BasicFoo.class ).count(), equalTo( 2 ) );
        assertThat( module.findServices( Foo.class ).count(), equalTo( 2 ) );

        assertThat( module.findService( SomeOtherFoo.class ).get().bar(), equalTo( CATHEDRAL ) );

        // Exact type match first even if it is assembled _after_ an assignable, the assignable comes after
        Iterator<ServiceReference<BasicFoo>> basicFoos = module.findServices( BasicFoo.class ).iterator();
        assertThat( basicFoos.next().get().bar(), equalTo( BAZAR ) );
        assertThat( basicFoos.next().get().bar(), equalTo( CATHEDRAL ) );
        assertThat( basicFoos.hasNext(), is( false ) );

        // No exact type match, all assembled are assignable, follows assembly Type order
        Iterator<ServiceReference<Foo>> foos = module.findServices( Foo.class ).iterator();
        assertThat( foos.next().get().bar(), equalTo( CATHEDRAL ) );
        assertThat( foos.next().get().bar(), equalTo( BAZAR ) );
        assertThat( foos.hasNext(), is( false ) );
    }

}
