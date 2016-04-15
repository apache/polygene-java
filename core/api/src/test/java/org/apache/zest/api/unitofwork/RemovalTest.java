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

package org.apache.zest.api.unitofwork;

import org.junit.Test;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

public class RemovalTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( TestEntity.class );
        module.entities( PidRegulator.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenEntityIsCreatedAndUnitOfWorkIsNotCompletedWhenEntityIsRemoveThenSuccessfulRemoval()
        throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<TestEntity> builder = uow.newEntityBuilder( TestEntity.class, "123" );
            builder.instance().test().set( "habba" );
            TestEntity test = builder.newInstance();
            uow.remove( test );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenStandardPidRegulatorWhenNoChangeInInputExpectOutputToGoTowardsMinimum()
        throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        PidRegulator regulator = null;
        try
        {
            regulator = createPidRegulator( uow );
        }
        finally
        {
            if( regulator != null )
            {
                uow.remove( regulator );
            }
            // TODO: This problem is related to that uow.remove() has a bug.
            // If the Entity is both created and removed in the same session, then the remove() should simply remove
            // the entity from the internal UoW holding area, and not set the REMOVED status.

            // Probably that UnitOfWorkInstance.remove() should also call instanceCache.remove(), but the question is
            // then what is an InstanceKey vs EntityReference
            uow.complete();
        }
    }

    public interface TestEntity
        extends EntityComposite
    {
        @Optional
        Property<String> test();
    }

    private PidRegulator createPidRegulator( UnitOfWork uow )
        throws UnitOfWorkCompletionException
    {
        EntityBuilder<PidRegulator> builder = uow.newEntityBuilder( PidRegulator.class );
        PidRegulator prototype = builder.instance();
        prototype.p().set( 1.0f );
        prototype.i().set( 10f );
        prototype.d().set( 0.1f );
        prototype.maxD().set( 10f );
        prototype.maximum().set( 100f );
        prototype.minimum().set( 0f );
        PidRegulator regulator = builder.newInstance();

        return regulator;
    }

    //    @Mixins( { PidRegulatorAlgorithmMixin.class } )
    public interface PidRegulator
        extends PidParameters, EntityComposite
    {
    }

    public interface PidParameters
    {
        Property<Float> p();

        Property<Float> i();

        Property<Float> d();

        Property<Float> maxD();

        Property<Float> minimum();

        Property<Float> maximum();
    }
}
