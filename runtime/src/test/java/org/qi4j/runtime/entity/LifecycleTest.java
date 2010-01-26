/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2009, Niclas Hedhman.
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

package org.qi4j.runtime.entity;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.*;

/**
 * Test for the Lifecycle interface
 */
public class LifecycleTest
    extends AbstractQi4jTest
{
    public static boolean create;
    public static boolean remove;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.addEntities( TestEntity.class );
    }

    @Test
    public void whenEntityHasLifecycleWhenInstantiatedThenInvokeCreate()
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
            builder.newInstance();
            unitOfWork.complete();
        }
        catch( Exception e )
        {
            unitOfWork.discard();
        }

        assertThat( "Lifecycle.create() was invoked", create, CoreMatchers.equalTo( true ) );
    }

    @Test
    public void whenEntityHasLifecycleWhenRemovedThenInvokeRemove()
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
            TestEntity testEntity = builder.newInstance();
            unitOfWork.remove( testEntity );
            unitOfWork.complete();
        }
        catch( Exception e )
        {
            unitOfWork.discard();
        }

        assertThat( "Lifecycle.remove() was invoked", remove, CoreMatchers.equalTo( true ) );
    }

    public interface Testing
    {
        void doSomething();
    }

    @Mixins( TestMixin.class )
    public interface TestEntity
        extends Testing, EntityComposite
    {
    }

    public static class TestMixin
        implements Lifecycle, Testing
    {
        public void create()
            throws LifecycleException
        {
            create = true;
        }

        public void remove()
            throws LifecycleException
        {
            remove = true;
        }

        public void doSomething()
        {
            System.out.println( "Test!" );
        }
    }
}
