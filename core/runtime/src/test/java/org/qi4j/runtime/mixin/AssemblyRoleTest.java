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

package org.qi4j.runtime.mixin;

import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of declaring types in assembly
 */
public class AssemblyRoleTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );

        module.transients( FooComposite.class ).withTypes( Foo.class );
        module.transients( FooComposite2.class ).withTypes( Foo.class ).withMixins( CustomFooMixin.class );

        module.entities( FooEntity.class ).withTypes( Foo.class );
    }

    @Test
    public void testAssemblyTypesCustomMixin()
    {
        FooComposite2 composite2 = module.newTransient( FooComposite2.class );
        assertThat( "Custom mixin has executed", ( (Foo) composite2 ).test( "Foo", 42 ), equalTo( "Foo/42" ) );
    }

    @Test
    public void testAssemblyTypesDefaultMixin()
    {
        FooComposite composite = module.newTransient( FooComposite.class );
        assertThat( "Default mixin has executed", ( (Foo) composite ).test( "Foo", 42 ), equalTo( "Foo 42" ) );
    }

    @Test
    public void testAssemblyMixinsEntity()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        FooEntity entity = uow.newEntity( FooEntity.class, "123" );
        uow.complete();

        uow = module.newUnitOfWork();
        Foo foo = uow.get( Foo.class, "123" );

        try
        {
            assertThat( "Default mixin has executed", foo.test( "Foo", 42 ), equalTo( "Foo 42" ) );
        }
        finally
        {
            uow.discard();
        }
    }

    public interface FooComposite
        extends TransientComposite
    {
    }

    public interface FooComposite2
        extends TransientComposite
    {
    }

    @Mixins( FooMixin.class )
    public interface Foo
    {
        String test( String foo, int bar );
    }

    public static class FooMixin
        implements Foo
    {
        public String test( String foo, int bar )
        {
            return foo + " " + bar;
        }
    }

    public static class CustomFooMixin
        implements Foo
    {
        public String test( String foo, int bar )
        {
            return foo + "/" + bar;
        }
    }

    public interface FooEntity
        extends EntityComposite
    {

    }
}