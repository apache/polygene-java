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

import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceComposite;

/**
 * TODO
 */
@Mixins( DummyDataService.DummyDataMixin.class )
public interface DummyDataService
    extends ServiceComposite, Activatable
{

    class DummyDataMixin
        implements Activatable
    {
        @Structure UnitOfWorkFactory uowf;

        public void activate() throws Exception
        {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( "test1", TestEntity.class );
                builder.stateOfComposite().test1().set( "Foo bar" );
                TestEntity testEntity = builder.newInstance();

                EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder( "test2", TestEntity.class );
                builder2.stateOfComposite().test1().set( "Xyzzy" );
                builder2.stateOfComposite().association().set( testEntity );
                TestEntity testEntity2 = builder2.newInstance();

                unitOfWork.complete();
            }
            catch( Exception e )
            {
                unitOfWork.discard();
            }

        }

        public void passivate() throws Exception
        {
        }
    }
}
