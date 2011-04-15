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
package org.qi4j.envisage.school.domain.person.assembly;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.envisage.school.domain.person.Person;
import org.qi4j.envisage.school.domain.person.Role;

@Mixins( PersonEntity.PersonMixin.class )
public interface PersonEntity
    extends Person, EntityComposite
{
    class PersonMixin
        implements Person
    {
        @This
        private PersonState state;

        public String firstName()
        {
            return state.firstName().get();
        }

        public String lastName()
        {
            return state.lastName().get();
        }

        public Iterable<Role> roles()
        {
            return state.roles();
        }

        public void addRole( Role role )
        {
            state.roles().add( 0, role );
        }
    }

    static interface PersonState
    {
        Property<String> firstName();

        Property<String> lastName();

        ManyAssociation<Role> roles();
    }
}