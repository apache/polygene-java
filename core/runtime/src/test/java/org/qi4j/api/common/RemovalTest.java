/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.api.common;

import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

public class RemovalTest
    extends AbstractQi4jTest
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
        UnitOfWork uow = module.newUnitOfWork();
        EntityBuilder<TestEntity> builder = uow.newEntityBuilder( TestEntity.class, "123" );
        builder.instance().test().set( "habba" );
        TestEntity test = builder.newInstance();
        uow.remove( test );
        uow.complete();
    }

    @Test
    public void givenStandardPidRegulatorWhenNoChangeInInputExpectOutputToGoTowardsMinimum()
        throws Exception
    {
        UnitOfWork uow = module.newUnitOfWork();
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
