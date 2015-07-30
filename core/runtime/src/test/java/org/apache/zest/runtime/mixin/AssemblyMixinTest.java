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

package org.apache.zest.runtime.mixin;

import org.junit.Test;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of declaring mixin in assembly
 */
public class AssemblyMixinTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );

        module.transients( FooComposite.class ).withMixins( CustomFooMixin.class );

        module.entities( FooEntity.class ).withMixins( FooMixin.class );
    }

    @Test
    public void testAssemblyMixins()
    {
        assertThat( "Custom mixin has executed", module.newTransient( Foo.class )
            .test( "Foo", 42 ), equalTo( "Foo/42" ) );
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
            assertThat( "Custom mixin has executed", foo.test( "Foo", 42 ), equalTo( "Foo 42" ) );
        }
        finally
        {
            uow.discard();
        }
    }

    public interface FooComposite
        extends TransientComposite, Foo
    {
    }

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
        extends EntityComposite, Foo
    {

    }
}