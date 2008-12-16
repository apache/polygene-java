/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.library.swing.visualizer.school.domain.model.person.assembler;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.library.swing.visualizer.school.domain.model.person.Person;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( SamplePersonBootstrapService.SamplePersonBootstrapMixin.class )
public interface SamplePersonBootstrapService extends Activatable, ServiceComposite
{
    String EDWARD = "edward";
    String NICLAS = "niclas";
    String RICKARD = "rickard";

    public class SamplePersonBootstrapMixin
        implements Activatable
    {
        private static final String[][] DATAS =
            {
                { EDWARD, "Edward", "Yakop" },
                { NICLAS, "Niclas", "Hedhman" },
                { RICKARD, "Rickard", "Öberg" }
            };

        @Structure private UnitOfWorkFactory uowf;

        public final void activate()
            throws Exception
        {
            UnitOfWork uow = uowf.nestedUnitOfWork();

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
            EntityBuilder<Person> person = uow.newEntityBuilder( personId, Person.class );

            PersonState state = person.stateFor( PersonState.class );
            state.firstName().set( firstName );
            state.lastName().set( lastName );

            person.newInstance();
        }

        public final void passivate()
            throws Exception
        {
        }
    }
}
