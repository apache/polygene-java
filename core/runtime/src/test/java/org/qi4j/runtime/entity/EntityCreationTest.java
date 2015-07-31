/*
 * Copyright (c) 2010, Stanislav Muhametsin.
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
package org.qi4j.runtime.entity;

import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

/**
 * Test case for http://team.ops4j.org/browse/QI-274
 */
public class EntityCreationTest
        extends AbstractQi4jTest
{

    @Mixins( SomeEntityMixin.class )
    public interface SomeEntity
            extends EntityComposite, Lifecycle
    {

        @Immutable
        public Property<String> someProperty();

    }

    public static class SomeEntityMixin
            implements Lifecycle
    {

        @This
        private SomeEntity _me;

        @Override
        public void create()
                throws LifecycleException
        {
            this._me.someProperty().set( "SomeValue" );
        }

        @Override
        public void remove()
                throws LifecycleException
        {
        }

    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( SomeEntity.class );
    }

    @Test
    public void doTestUseUowNewEntity()
    {
        UnitOfWork uow = module.newUnitOfWork();
        SomeEntity entity = uow.newEntity( SomeEntity.class );
        uow.discard();
    }

    @Test
    public void doTestUseEntityBuilder()
    {
        UnitOfWork uow = module.newUnitOfWork();
        EntityBuilder<SomeEntity> builder = uow.newEntityBuilder( SomeEntity.class );
        SomeEntity entity = builder.newInstance();
        uow.discard();
    }

}
