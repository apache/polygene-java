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

import junit.framework.Assert;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilder;
import static org.qi4j.api.annotation.ParameterValue.parameter;

public class PersonTest extends AbstractTest
{
    public void testNewPerson() throws Exception
    {
        CompositeBuilder<PersonComposite> builder = builderFactory.newCompositeBuilder( PersonComposite.class );
        builder.newMixin( Person.class, parameter( "firstName", "Sianny"), parameter("lastName", "Halim"));
        PersonComposite person = builder.newInstance();

        String firstName = "Sianny";
        String lastName = "Halim";

        person.setFirstName( firstName );
        person.setLastName( lastName );
        person.setGender( GenderType.female );

        Assert.assertEquals( firstName, person.getFirstName() );
        Assert.assertEquals( lastName, person.getLastName() );
        Assert.assertEquals( GenderType.female, person.getGender() );
    }

    private interface PersonComposite extends Person, Composite
    {
    }
}
