/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.demo.intro;

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueComposite;

public class StateModelingDocs
{

// START SNIPPET: intro1
    interface PersonEntity
            extends EntityComposite
    {
        Property<String> givenName();
        Property<String> surName();
    }

// END SNIPPET: intro1




    static class Roles {

// START SNIPPET: roles
        interface Nameable
        {
            @UseDefaults Property<String> givenName();
            @UseDefaults @Optional Property<String> surName();
        }

        interface PersonEntity
                extends Nameable, EntityComposite
        {}
// END SNIPPET: roles
    }

    static class Values {

// START SNIPPET: values
        interface NameValue
                extends ValueComposite
        {
            @UseDefaults Property<String> givenName();
            @UseDefaults @Optional Property<String> surName();
        }

        interface Nameable
        {
            Property<NameValue> name();
        }
// END SNIPPET: values


// START SNIPPET: private
        @Mixins(ListablePersonMixin.class)
        interface PersonEntity
                extends Listable, EntityComposite {}

        interface PersonState
                extends Nameable {}

        public class ListablePersonMixin
                implements Listable
        {
            @This PersonState person;

            @Override
            public String listName()
            {
                String fullName = person.name().get().givenName().get();
                String sn = person.name().get().surName().get();
                if (sn != null) fullName += " "+sn;
                return fullName;
            }
        }

        interface Listable
        {
            public String listName();
        }
// END SNIPPET: private

    }


    static class More {
// START SNIPPET: more
        interface PersonEntity
                extends EntityComposite
        {
            Association<PersonEntity> father();
            @Optional Association<PersonEntity> spouse();
            ManyAssociation<PersonEntity> children();
            @Aggregated ManyAssociation<BookNoteEntity> favouriteBooks();
        }

        interface BookNoteEntity
                extends EntityComposite
        {
            Property<String> note();
            Association<BookEntity> book();
        }

// END SNIPPET: more
        interface BookEntity
                extends EntityComposite
        {}
    }

}
