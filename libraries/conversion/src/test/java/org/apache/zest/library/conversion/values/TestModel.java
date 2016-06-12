/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.library.conversion.values;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.value.ValueComposite;

import static java.time.ZoneOffset.UTC;

/**
 * Test Model.
 */
final class TestModel
{
    static PersonEntity createPerson( UnitOfWork uow, String firstName, String lastName, LocalDate birthDate )
    {
        EntityBuilder<PersonEntity> builder = uow.newEntityBuilder( PersonEntity.class, "id:" + firstName );
        PersonState state = builder.instanceFor( PersonState.class );
        state.firstName().set( firstName );
        state.lastName().set( lastName );
        state.dateOfBirth().set( birthDate );
        return builder.newInstance();
    }

    static LocalDate createBirthDate( int year, int month, int day )
    {
        return LocalDate.of( year, month, day);
    }

    // START SNIPPET: state
    public interface PersonState
    {

        Property<String> firstName();

        Property<String> lastName();

        Property<LocalDate> dateOfBirth();

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
            Duration age = Duration.between( state.dateOfBirth().get(), Instant.now() );
            return (int) age.toDays()/365;
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

        Property<LocalDate> dateOfBirth();

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

        Property<LocalDate> dateOfBirth();

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

        Property<LocalDate> dateOfBirth();

        @Optional
        Property<String> spouse();

        @Optional
        Property<List<String>> children();

    }

    private TestModel()
    {
    }
}
