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

package org.qi4j.rest;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * JAVADOC
 */
@Mixins( DummyDataService.DummyDataMixin.class )
public interface DummyDataService
    extends ServiceComposite, Activatable
{

    class DummyDataMixin
        implements Activatable
    {
        @Structure
        UnitOfWorkFactory uowf;

        public void activate()
            throws Exception
        {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                {
                    EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class, "test1" );
                    builder.instance().name().set( "Foo bar" );
                    builder.instance().age().set( 42 );
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
            catch( Exception e )
            {
                unitOfWork.discard();
                throw e;
            }
        }

        public void passivate()
            throws Exception
        {
        }
    }
}
