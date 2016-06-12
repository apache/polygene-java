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

import java.time.Instant;
import java.time.LocalDate;
import java.util.function.Function;
import org.junit.Test;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.conversion.values.TestModel.PersonEntity;
import org.apache.zest.library.conversion.values.TestModel.PersonValue;
import org.apache.zest.library.conversion.values.TestModel.PersonValue2;
import org.apache.zest.library.conversion.values.TestModel.PersonValue3;
import org.apache.zest.library.conversion.values.TestModel.PersonValue4;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;
import static org.apache.zest.library.conversion.values.TestModel.createBirthDate;
import static org.apache.zest.library.conversion.values.TestModel.createPerson;

public class EntityToValueTest
    extends AbstractZestTest
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
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity entity = setupPersonEntities( uow );

            // START SNIPPET: conversion
            EntityToValueService conversion = serviceFinder.findService( EntityToValueService.class ).get();
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
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity niclas = setupPersonEntities( uow );

            ServiceReference<EntityToValueService> reference = serviceFinder.findService( EntityToValueService.class );
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
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity niclas = setupPersonEntities( uow );

            ServiceReference<EntityToValueService> reference = serviceFinder.findService( EntityToValueService.class );
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
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity niclas = setupPersonEntities( uow );

            ServiceReference<EntityToValueService> reference = serviceFinder.findService( EntityToValueService.class );
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
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity entity = setupPersonEntities( uow );

            // START SNIPPET: prototypeOpportunity
            EntityToValueService conversion = serviceFinder.findService( EntityToValueService.class ).get();
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

    private static PersonEntity setupPersonEntities( UnitOfWork uow )
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

    private static PersonEntity createNiclas( UnitOfWork uow )
    {
        String firstName = "Niclas";
        String lastName = "Hedhman";
        LocalDate birthTime = createBirthDate( 1964, 9, 25 );
        return createPerson( uow, firstName, lastName, birthTime );
    }

    private static PersonEntity createLis( UnitOfWork uow )
    {
        String firstName = "Lis";
        String lastName = "Gazi";
        LocalDate birthTime = createBirthDate( 1976, 2, 19 );
        return createPerson( uow, firstName, lastName, birthTime );
    }

    private static PersonEntity createEric( UnitOfWork uow )
    {
        String firstName = "Eric";
        String lastName = "Hedman";
        LocalDate birthTime = createBirthDate( 2004, 4, 8 );
        return createPerson( uow, firstName, lastName, birthTime );
    }
}
