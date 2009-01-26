/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.model;

import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.library.general.model.properties.Name;

/**
 * Generic interface of PersonName that stores first and last name.
 */
@Mixins( PersonName.PersonNameMixin.class )
public interface PersonName
{
    Name firstName();

    Name lastName();

    @Computed Name fullName();

    public abstract class PersonNameMixin
        implements PersonName
    {
        @This PersonName personName;

        @State Name fullName;

        /**
         * Returns a person full name in the format LastName, FirstName
         */
        public Name fullName()
        {
            return new ComputedFullName();
        }

        private class ComputedFullName extends ComputedPropertyInstance<String>
            implements Name
        {
            public ComputedFullName()
                throws IllegalArgumentException
            {
                super( PersonNameMixin.this.fullName );
            }

            public String get()
            {
                return personName.firstName().get() + " " + personName.lastName().get();
            }
        }
    }
}
