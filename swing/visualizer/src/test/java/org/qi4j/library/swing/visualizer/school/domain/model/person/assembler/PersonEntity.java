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

import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.Identity;
import org.qi4j.injection.scope.This;
import org.qi4j.library.swing.visualizer.school.domain.model.person.Person;
import org.qi4j.library.swing.visualizer.school.domain.model.person.PersonId;
import org.qi4j.library.swing.visualizer.school.domain.model.person.Role;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
@Mixins( PersonEntity.PersonMixin.class )
interface PersonEntity extends Person, EntityComposite
{
    class PersonMixin
        implements Person
    {
        @This private PersonState state;
        private PersonId personId;

        public PersonMixin( @This Identity identity )
        {
            String personIdString = identity.identity().get();
            personId = new PersonId( personIdString );
        }

        public final PersonId personId()
        {
            return personId;
        }

        public final String firstName()
        {
            return state.firstName().get();
        }

        public final String lastName()
        {
            return state.lastName().get();
        }

        public final Iterable<Role> roles()
        {
            return state.roles();
        }

        public final void addRole( Role role )
        {
            state.roles().add( role );
        }
    }
}
