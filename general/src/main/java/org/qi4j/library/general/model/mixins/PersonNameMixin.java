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
package org.qi4j.library.general.model.mixins;

import org.qi4j.api.annotation.Property;
import org.qi4j.library.general.model.PersonName;

/**
 * Default mixin implementation for {@link org.qi4j.library.general.model.PersonName}
 */
public final class PersonNameMixin implements PersonName
{
    private String lastName;
    private String firstName;


    public PersonNameMixin( @Property("lastname") String lastName, @Property("firstname") String firstName )
    {
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public PersonNameMixin()
    {
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName( String aFirstName )
    {
        firstName = aFirstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName( String aLastName )
    {
        lastName = aLastName;
    }
}
