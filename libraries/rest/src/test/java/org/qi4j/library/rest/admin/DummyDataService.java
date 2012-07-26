/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.rest.admin;

import java.util.HashMap;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

@Mixins( DummyDataService.DummyDataMixin.class )
@Activators( DummyDataService.Activator.class )
public interface DummyDataService
    extends ServiceComposite
{
    
    void insertInitialData()
            throws Exception;

    static class Activator
            extends ActivatorAdapter<ServiceReference<DummyDataService>>
    {

        @Override
        public void afterActivation( ServiceReference<DummyDataService> activated )
                throws Exception
        {
            activated.get().insertInitialData();
        }

    }
    
    abstract class DummyDataMixin
        implements DummyDataService
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        public void insertInitialData()
            throws Exception
        {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                {
                    ValueBuilder<TestValue> valueBuilder = vbf.newValueBuilder( TestValue.class );
                    valueBuilder.prototype().longList().get().add( 42L );
                    valueBuilder.prototype().string().set( "Foo bar value" );
                    valueBuilder.prototype().map().set( new HashMap() );

                    EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class, "test1" );
                    builder.instance().name().set( "Foo bar" );
                    builder.instance().age().set( 42 );
                    builder.instance().value().set( valueBuilder.newInstance() );
                    TestEntity testEntity = builder.newInstance();

                    EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder( TestEntity.class, "test2" );
                    builder2.instance().name().set( "Xyzzy" );
                    builder2.instance().age().set( 12 );
                    builder2.instance().association().set( testEntity );
                    builder2.instance().manyAssociation().add( 0, testEntity );
                    builder2.instance().manyAssociation().add( 0, testEntity );

                    EntityBuilder<TestRole> builder3 = unitOfWork.newEntityBuilder( TestRole.class );
                    builder3.instance().name().set( "A role" );
                    TestRole testRole = builder3.newInstance();

                    builder2.newInstance();
                }

                {
                    EntityBuilder<TestEntity2> builder = unitOfWork.newEntityBuilder( TestEntity2.class, "test3" );
                    builder.instance().name().set( "Test3" );
                    builder.newInstance();
                }

                unitOfWork.complete();
            }
            finally
            {
                unitOfWork.discard();
            }
        }

    }
}
