/*
 * Copyright 2010 Niclas Hedhman.
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
package org.qi4j.library.conversion.values;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;
import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;

public class EntityToValueTest
    extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // START SNIPPET: assembly
        new EntityToValueAssembler().assemble( module );
        // END SNIPPET: assembly
        new EntityTestAssembler().assemble( module );
        module.entities( PersonEntity.class );
        module.values( PersonValue.class );
        module.values( PersonValue2.class );
        module.values( PersonValue3.class );
        module.values( PersonValue4.class );
    }

    @Test
    public void whenConvertingEntityToValueExpectCorrectValues()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            PersonEntity entity = setupPersonEntities( uow );

            // START SNIPPET: conversion
            EntityToValueService conversion = module.findService( EntityToValueService.class ).get();
            PersonValue value = conversion.convert( PersonValue.class, entity );
            // END SNIPPET: conversion
            assertEquals( "Niclas", value.firstName().get() );
            assertEquals( "Hedhman", value.lastName().get() );
            assertEquals( "id:Lis", value.spouse().get() );
            assertEquals( "id:Eric", value.children().get().get( 0 ) );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenUnqualifiedValueWhenConvertingEntityExpectCorrectMapping()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            PersonEntity niclas = setupPersonEntities( uow );

            ServiceReference<EntityToValueService> reference = module.findService( EntityToValueService.class );
            EntityToValueService service = reference.get();

            PersonValue2 niclasValue = service.convert( PersonValue2.class, niclas );
            assertEquals( "Niclas", niclasValue.firstName().get() );
            assertEquals( "Hedhman", niclasValue.lastName().get() );
            assertEquals( "id:Lis", niclasValue.spouse().get() );
            assertEquals( "id:Eric", niclasValue.children().get().get( 0 ) );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenUnqualifiedValue2WhenConvertingEntityExpectCorrectMapping()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            PersonEntity niclas = setupPersonEntities( uow );

            ServiceReference<EntityToValueService> reference = module.findService( EntityToValueService.class );
            EntityToValueService service = reference.get();

            PersonValue3 niclasValue = service.convert( PersonValue3.class, niclas );
            assertEquals( "Niclas", niclasValue.firstName().get() );
            assertEquals( "Hedhman", niclasValue.lastName().get() );
            assertEquals( "id:Lis", niclasValue.spouse().get() );
            assertEquals( "id:Eric", niclasValue.children().get().get( 0 ) );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test( expected = ConstraintViolationException.class )
    public void givenQualifiedValueNotFromSameInterfaceWhenConvertingEntityExpectNonOptionalException()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            PersonEntity niclas = setupPersonEntities( uow );

            ServiceReference<EntityToValueService> reference = module.findService( EntityToValueService.class );
            EntityToValueService service = reference.get();

            PersonValue4 niclasValue = service.convert( PersonValue4.class, niclas );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void whenConvertingEntityToValueUsingPrototypeOpportunityExpectCorrectValues()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            PersonEntity entity = setupPersonEntities( uow );

            // START SNIPPET: prototypeOpportunity
            EntityToValueService conversion = module.findService( EntityToValueService.class ).get();
            PersonValue value = conversion.convert( PersonValue.class, entity, new Function<PersonValue, PersonValue>()
            {
                @Override
                public PersonValue apply( PersonValue prototype )
                {
                    prototype.firstName().set( "Prototype Opportunity" );
                    return prototype;
                }
            } );
            // END SNIPPET: prototypeOpportunity
            assertEquals( "Prototype Opportunity", value.firstName().get() );
            assertEquals( "Hedhman", value.lastName().get() );
            assertEquals( "id:Lis", value.spouse().get() );
            assertEquals( "id:Eric", value.children().get().get( 0 ) );
            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    private PersonEntity setupPersonEntities( UnitOfWork uow )
    {
        PersonEntity niclas = createNiclas( uow );
        PersonEntity lis = createLis( uow );
        PersonEntity eric = createEric( uow );
        niclas.spouse().set( lis );
        niclas.children().add( eric );
        lis.spouse().set( niclas );
        lis.children().add( eric );
        assertEquals( "Niclas", niclas.firstName() );
        assertEquals( "Hedhman", niclas.lastName() );
        assertEquals( "Lis", lis.firstName() );
        assertEquals( "Gazi", lis.lastName() );
        assertEquals( "Eric", eric.firstName() );
        assertEquals( "Hedman", eric.lastName() );
        return niclas;
    }

    private PersonEntity createNiclas( UnitOfWork uow )
    {
        String firstName = "Niclas";
        String lastName = "Hedhman";
        ZonedDateTime birthTime = createBirthDate( 1964, 9, 25 );
        return createPerson( uow, firstName, lastName, birthTime );
    }

    private PersonEntity createLis( UnitOfWork uow )
    {
        String firstName = "Lis";
        String lastName = "Gazi";
        ZonedDateTime birthTime = createBirthDate( 1976, 2, 19 );
        return createPerson( uow, firstName, lastName, birthTime );
    }

    private PersonEntity createEric( UnitOfWork uow )
    {
        String firstName = "Eric";
        String lastName = "Hedman";
        ZonedDateTime birthTime = createBirthDate( 2004, 4, 8 );
        return createPerson( uow, firstName, lastName, birthTime );
    }

    private PersonEntity createPerson( UnitOfWork uow, String firstName, String lastName, ZonedDateTime birthTime )
    {
        EntityBuilder<PersonEntity> builder = uow.newEntityBuilder( PersonEntity.class, "id:" + firstName );
        PersonState state = builder.instanceFor( PersonState.class );
        state.firstName().set( firstName );
        state.lastName().set( lastName );
        state.dateOfBirth().set( birthTime );
        return builder.newInstance();
    }

    private ZonedDateTime createBirthDate( int year, int month, int day )
    {
        return ZonedDateTime.of( year, month, day, 0, 0, 0, 0, ZoneId.systemDefault() );
    }

    // START SNIPPET: state
    public interface PersonState
    {

        Property<String> firstName();

        Property<String> lastName();

        Property<ZonedDateTime> dateOfBirth();

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
            return (int) state.dateOfBirth().get().until(ZonedDateTime.now(), ChronoUnit.YEARS );
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

        Property<ZonedDateTime> dateOfBirth();

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

        Property<ZonedDateTime> dateOfBirth();

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

        Property<ZonedDateTime> dateOfBirth();

        @Optional
        Property<String> spouse();

        @Optional
        Property<List<String>> children();

    }

}
