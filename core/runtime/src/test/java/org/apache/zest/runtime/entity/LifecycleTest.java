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

package org.apache.zest.runtime.entity;

import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.Lifecycle;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;
import org.apache.zest.test.EntityTestAssembler;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 * Test for the Lifecycle interface
 */
public class LifecycleTest
    extends AbstractPolygeneTest
{
    public static boolean create;
    public static boolean remove;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( TestEntity.class );
    }

    @Test
    public void whenEntityHasLifecycleWhenInstantiatedThenInvokeCreate()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
            builder.newInstance();
            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }

        assertThat( "Lifecycle.create() was invoked", create, CoreMatchers.equalTo( true ) );
    }

    @Test
    public void whenEntityHasLifecycleWhenRemovedThenInvokeRemove()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
            TestEntity testEntity = builder.newInstance();
            unitOfWork.remove( testEntity );
            unitOfWork.complete();
        }
        finally
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
        {
            create = true;
        }

        public void remove()
        {
            remove = true;
        }

        public void doSomething()
        {
            System.out.println( "Test!" );
        }
    }
}
