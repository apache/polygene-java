/*
 * Copyright 2010-2012 Niclas Hedhman.
 * Copyright 2011 Rickard Öberg.
 * Copyright 2013-2015 Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.conversion.values;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueComposite;

/**
 * Test Model.
 */
final class TestModel
{
    static PersonEntity createPerson( UnitOfWork uow, String firstName, String lastName, Date birthTime )
    {
        EntityBuilder<PersonEntity> builder = uow.newEntityBuilder( PersonEntity.class, "id:" + firstName );
        PersonState state = builder.instanceFor( PersonState.class );
        state.firstName().set( firstName );
        state.lastName().set( lastName );
        state.dateOfBirth().set( birthTime );
        return builder.newInstance();
    }

    static Date createBirthDate( int year, int month, int day )
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        calendar.set( year, month - 1, day, 12, 0, 0 );
        return calendar.getTime();
    }

    // START SNIPPET: state
    public interface PersonState
    {

        Property<String> firstName();

        Property<String> lastName();

        Property<Date> dateOfBirth();

    }
    // END SNIPPET: state

    // START SNIPPET: value
    public interface PersonValue
        extends PersonState, ValueComposite
    {

        @Optional
        Property<String> spouse();

        @Optional
        Property<List<String>> children();

    }
    // END SNIPPET: value

    // START SNIPPET: entity
    @Mixins( PersonMixin.class )
    public interface PersonEntity
        extends EntityComposite
    {

        String firstName();

        String lastName();

        Integer age();

        @Optional
        Association<PersonEntity> spouse();

        ManyAssociation<PersonEntity> children();

    }
    // END SNIPPET: entity

    // START SNIPPET: entity
    public static abstract class PersonMixin
        implements PersonEntity
    {

        @This
        private PersonState state;
        // END SNIPPET: entity

        @Override
        public String firstName()
        {
            return state.firstName().get();
        }

        @Override
        public String lastName()
        {
            return state.lastName().get();
        }

        @Override
        public Integer age()
        {
            long now = System.currentTimeMillis();
            long birthdate = state.dateOfBirth().get().getTime();
            return (int) ( ( now - birthdate ) / 1000 / 3600 / 24 / 365.25 );
        }

        // START SNIPPET: entity
    }
    // END SNIPPET: entity

    // START SNIPPET: unqualified
    @Unqualified
    public interface PersonValue2
        extends ValueComposite
    {

        Property<String> firstName();

        Property<String> lastName();

        Property<Date> dateOfBirth();

        @Optional
        Property<String> spouse();

        @Optional
        Property<List<String>> children();

    }
    // END SNIPPET: unqualified

    @Unqualified( true )
    public interface PersonValue3
        extends ValueComposite
    {

        Property<String> firstName();

        Property<String> lastName();

        Property<Date> dateOfBirth();

        @Optional
        Property<String> spouse();

        @Optional
        Property<List<String>> children();

    }

    @Unqualified( false )
    public interface PersonValue4
        extends ValueComposite
    {

        Property<String> firstName();

        Property<String> lastName();

        Property<Date> dateOfBirth();

        @Optional
        Property<String> spouse();

        @Optional
        Property<List<String>> children();

    }

    private TestModel()
    {
    }
}
