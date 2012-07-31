/*  Copyright 2008 Edward Yakop.
*   Copyright 2009 Niclas Hedhman.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.envisage.school.domain.person.initialdata;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.envisage.school.domain.person.Person;
import org.qi4j.envisage.school.domain.person.assembly.PersonEntity;

@Mixins( SamplePersonInitialData.SamplePersonBootstrapMixin.class )
@Activators( SamplePersonInitialData.Activator.class )
public interface SamplePersonInitialData
    extends ServiceComposite
{
    String EDWARD = "edward";
    String NICLAS = "niclas";
    String RICKARD = "rickard";
    
    void insertInitialData()
            throws Exception;

    class Activator
            extends ActivatorAdapter<ServiceReference<SamplePersonInitialData>>
    {

        @Override
        public void afterActivation( ServiceReference<SamplePersonInitialData> activated )
                throws Exception
        {
            activated.get().insertInitialData();
        }

    }

    public abstract class SamplePersonBootstrapMixin
        implements SamplePersonInitialData
    {
        private static final String[][] DATAS =
            {
                { EDWARD, "Edward", "Yakop" },
                { NICLAS, "Niclas", "Hedhman" },
                { RICKARD, "Rickard", "Öberg" }
            };

        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public void insertInitialData()
            throws Exception
        {
            UnitOfWork uow = uowf.currentUnitOfWork();

            for( String[] data : DATAS )
            {
                String personId = data[ 0 ];
                String firstName = data[ 1 ];
                String lastName = data[ 2 ];
                createPerson( uow, personId, firstName, lastName );
            }

            uow.complete();
        }

        private void createPerson( UnitOfWork uow, String personId, String firstName, String lastName )
        {
            EntityBuilder<Person> person = uow.newEntityBuilder( Person.class, personId );

            PersonEntity.PersonState state = person.instanceFor( PersonEntity.PersonState.class );
            state.firstName().set( firstName );
            state.lastName().set( lastName );

            person.newInstance();
        }

    }
}