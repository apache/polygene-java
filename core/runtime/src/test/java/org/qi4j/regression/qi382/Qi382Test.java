/*
 * Copyright 2013 Niclas Hedhman.
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
package org.qi4j.regression.qi382;

import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class Qi382Test extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
        module.addServices( MemoryEntityStoreService.class );
        module.entities( Car.class, Person.class );
    }

    @Test
    public void givenCreationOfTwoEntitiesWhenAssigningOneToOtherExpectCompletionToSucceed()
        throws UnitOfWorkCompletionException
    {
        try( UnitOfWork unitOfWork = module.newUnitOfWork() )
        {
            Car car = unitOfWork.newEntity( Car.class, "Ferrari" );
            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = module.newUnitOfWork() )
        {
            Car car = unitOfWork.get( Car.class, "Ferrari" );
            assertThat( car, notNullValue() );
            Person p = unitOfWork.get( Person.class, "Niclas" );
            assertThat( p, notNullValue() );
            assertThat( p.car().get(), equalTo( car ) );
        }
    }

    @Mixins( Car.CarMixin.class )
    public interface Car extends EntityComposite, Lifecycle
    {

        static class CarMixin implements Lifecycle
        {
            @This
            private Car me;

            @Structure
            private Module module;

            @Override
            public void create()
                throws LifecycleException
            {
                UnitOfWork unitOfWork = module.currentUnitOfWork();
                EntityBuilder<Person> builder = unitOfWork.newEntityBuilder( Person.class, "Niclas" );
                builder.instance().car().set( me );
                builder.newInstance();
            }

            @Override
            public void remove()
                throws LifecycleException
            {

            }
        }
    }

    public interface Person extends EntityComposite
    {
        Association<Car> car();
    }
}
