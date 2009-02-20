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
import static org.qi4j.api.entity.association.Qualifier.qualifier;
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
        @Structure UnitOfWorkFactory uowf;

        public void activate() throws Exception
        {
            UnitOfWork unitOfWork = uowf.newUnitOfWork();
            try
            {
                EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( "test1", TestEntity.class );
                builder.stateOfComposite().name().set( "Foo bar" );
                builder.stateOfComposite().age().set( 42 );
                TestEntity testEntity = builder.newInstance();

                EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder( "test2", TestEntity.class );
                builder2.stateOfComposite().name().set( "Xyzzy" );
                builder2.stateOfComposite().age().set( 12 );
                builder2.stateOfComposite().association().set( testEntity );
                builder2.stateOfComposite().manyAssociation().add( testEntity );
                builder2.stateOfComposite().manyAssociation().add( testEntity );

                EntityBuilder<TestRole> builder3 = unitOfWork.newEntityBuilder(TestRole.class);
                builder3.stateOfComposite().name().set( "A role" );
                TestRole testRole = builder3.newInstance();

                builder2.stateOfComposite().manyAssociationQualifier().add( qualifier( testEntity, testRole ));

                TestEntity testEntity2 = builder2.newInstance();

                unitOfWork.complete();
            }
            catch( Exception e )
            {
                unitOfWork.discard();
                throw e;
            }

        }

        public void passivate() throws Exception
        {
        }
    }
}
