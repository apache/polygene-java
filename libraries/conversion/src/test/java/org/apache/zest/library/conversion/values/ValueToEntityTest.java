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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.functional.Iterables;
import org.apache.zest.library.conversion.values.TestModel.PersonEntity;
import org.apache.zest.library.conversion.values.TestModel.PersonValue;
import org.apache.zest.library.conversion.values.TestModel.PersonValue2;
import org.apache.zest.library.conversion.values.TestModel.PersonValue3;
import org.apache.zest.library.conversion.values.TestModel.PersonValue4;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.apache.zest.api.usecase.UsecaseBuilder.newUsecase;
import static org.apache.zest.library.conversion.values.TestModel.createBirthDate;
import static org.apache.zest.library.conversion.values.TestModel.createPerson;

/**
 * ValueToEntity Service Test.
 */
public class ValueToEntityTest
    extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // START SNIPPET: assembly
        new ValueToEntityAssembler().assemble( module );
        // END SNIPPET: assembly
        new EntityTestAssembler().assemble( module );
        module.entities( PersonEntity.class );
        module.values( PersonValue.class );
        module.values( PersonValue2.class );
        module.values( PersonValue3.class );
        module.values( PersonValue4.class );
    }

    private LocalDate someBirthDate;
    private String ednaIdentity;
    private String zekeIdentity;
    private String fredIdentity;

    @Before
    public void setupInitialData()
        throws UnitOfWorkCompletionException
    {
        // See http://en.wikipedia.org/wiki/Template:Flintstones_family_tree
        someBirthDate = createBirthDate( 1, 1, 1 );
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "InitialData" ) ) )
        {
            ednaIdentity = createPerson( uow, "Edna", "Flintstone", someBirthDate ).identity().get();
            zekeIdentity = createPerson( uow, "Zeke", "Flintstone", someBirthDate ).identity().get();
            fredIdentity = createPerson( uow, "Fred", "Flintstone", someBirthDate ).identity().get();
            uow.complete();
        }
    }

    @Test
    public void givenQualifiedValueWhenCreatingEntityExpectCorrectEntity()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<PersonValue> builder = valueBuilderFactory.newValueBuilder( PersonValue.class );
        builder.prototype().firstName().set( "Ed" );
        builder.prototype().lastName().set( "Flintstone" );
        builder.prototype().dateOfBirth().set( someBirthDate );
        builder.prototype().spouse().set( ednaIdentity );
        builder.prototype().children().set( Arrays.asList( zekeIdentity, fredIdentity ) );
        PersonValue edValue = builder.newInstance();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "CreatingEntityFromQualifiedValue" ) ) )
        {
            // START SNIPPET: creation
            ValueToEntity conversion = serviceFinder.findService( ValueToEntity.class ).get();
            PersonEntity edEntity = conversion.create( PersonEntity.class, edValue );
            // END SNIPPET: creation
            assertThat( edEntity.firstName(), equalTo( "Ed" ) );
            assertThat( edEntity.lastName(), equalTo( "Flintstone" ) );
            assertThat( edEntity.spouse().get().firstName(), equalTo( "Edna" ) );
            assertThat( Iterables.count( Iterables.filter( new Predicate<PersonEntity>()
            {
                @Override
                public boolean test( PersonEntity child )
                {
                    return "Zeke".equals( child.firstName() ) || "Fred".equals( child.firstName() );
                }
            }, edEntity.children() ) ), is( 2L ) );

            uow.complete();
        }
    }

    @Test
    public void givenUnqualifiedValueWhenCreatingEntityExpectCorrectEntity()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<PersonValue2> builder = valueBuilderFactory.newValueBuilder( PersonValue2.class );
        builder.prototype().firstName().set( "Ed" );
        builder.prototype().lastName().set( "Flintstone" );
        builder.prototype().dateOfBirth().set( someBirthDate );
        builder.prototype().spouse().set( ednaIdentity );
        builder.prototype().children().set( Arrays.asList( zekeIdentity, fredIdentity ) );
        PersonValue2 edValue = builder.newInstance();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "CreatingEntityFromUnqualifiedValue" ) ) )
        {
            ValueToEntity conversion = serviceFinder.findService( ValueToEntity.class ).get();

            PersonEntity edEntity = conversion.create( PersonEntity.class, "id:Ed", edValue );

            assertThat( edEntity.identity().get(), equalTo( "id:Ed" ) );
            assertThat( edEntity.firstName(), equalTo( "Ed" ) );
            assertThat( edEntity.lastName(), equalTo( "Flintstone" ) );
            assertThat( edEntity.spouse().get().firstName(), equalTo( "Edna" ) );
            assertThat( Iterables.count( Iterables.filter( new Predicate<PersonEntity>()
            {
                @Override
                public boolean test( PersonEntity child )
                {
                    return "Zeke".equals( child.firstName() ) || "Fred".equals( child.firstName() );
                }
            }, edEntity.children() ) ), is( 2L ) );

            uow.complete();
        }
    }

    @Test
    public void givenUnqualifiedValue2WhenCreatingEntityExpectCorrectEntity()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<PersonValue3> builder = valueBuilderFactory.newValueBuilder( PersonValue3.class );
        builder.prototype().firstName().set( "Ed" );
        builder.prototype().lastName().set( "Flintstone" );
        builder.prototype().dateOfBirth().set( someBirthDate );
        builder.prototype().spouse().set( ednaIdentity );
        builder.prototype().children().set( Arrays.asList( zekeIdentity, fredIdentity ) );
        PersonValue3 edValue = builder.newInstance();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "CreatingEntityFromUnqualifiedValue" ) ) )
        {
            ValueToEntity conversion = serviceFinder.findService( ValueToEntity.class ).get();

            PersonEntity edEntity = conversion.create( PersonEntity.class, "id:Ed", edValue );

            assertThat( edEntity.identity().get(), equalTo( "id:Ed" ) );
            assertThat( edEntity.firstName(), equalTo( "Ed" ) );
            assertThat( edEntity.lastName(), equalTo( "Flintstone" ) );
            assertThat( edEntity.spouse().get().firstName(), equalTo( "Edna" ) );
            assertThat( Iterables.count( Iterables.filter( new Predicate<PersonEntity>()
            {
                @Override
                public boolean test( PersonEntity child )
                {
                    return "Zeke".equals( child.firstName() ) || "Fred".equals( child.firstName() );
                }
            }, edEntity.children() ) ), is( 2L ) );

            uow.complete();
        }
    }

    @Test( expected = ConstraintViolationException.class )
    public void givenQualifiedValueNotFromSameInterfaceWhenCreatingEntityExpectNonOptionalException()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<PersonValue4> builder = valueBuilderFactory.newValueBuilder( PersonValue4.class );
        builder.prototype().firstName().set( "Ed" );
        builder.prototype().lastName().set( "Flintstone" );
        builder.prototype().dateOfBirth().set( someBirthDate );
        builder.prototype().spouse().set( ednaIdentity );
        builder.prototype().children().set( Arrays.asList( zekeIdentity, fredIdentity ) );
        PersonValue4 edValue = builder.newInstance();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "CreatingEntityFromUnqualifiedValue" ) ) )
        {
            ValueToEntity conversion = serviceFinder.findService( ValueToEntity.class ).get();

            PersonEntity edEntity = conversion.create( PersonEntity.class, "id:Ed", edValue );

            uow.complete();
        }

    }

    @Test
    public void givenQualifiedValueWhenUpdatingEntityExpectCorrectEntity()
        throws UnitOfWorkCompletionException
    {
        String rickyIdentity;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "CreateRickySlaghoopleWithTypo" ) ) )
        {
            PersonEntity ricky = createPerson( uow, "Ricky", "Slaghople", someBirthDate );
            ricky.spouse().set( uow.get( PersonEntity.class, ednaIdentity ) );
            ricky.children().add( uow.get( PersonEntity.class, zekeIdentity ) );
            rickyIdentity = ricky.identity().get();
            assertThat( ricky.spouse().get(), notNullValue() );
            assertThat( ricky.children().count(), is( 1 ) );
            uow.complete();
        }
        ValueBuilder<PersonValue> builder = valueBuilderFactory.newValueBuilder( PersonValue.class );
        builder.prototype().firstName().set( "Ricky" );
        builder.prototype().lastName().set( "Slaghoople" );
        builder.prototype().dateOfBirth().set( someBirthDate );
        PersonValue rickyNewStateValue = builder.newInstance();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "UpdateRickySlaghoople" ) ) )
        {
            PersonEntity rickyEntity = uow.get( PersonEntity.class, rickyIdentity );
            // START SNIPPET: update
            ValueToEntity conversion = serviceFinder.findService( ValueToEntity.class ).get();
            conversion.update( rickyEntity, rickyNewStateValue );
            // END SNIPPET: update

            assertThat( rickyEntity.lastName(), equalTo( "Slaghoople" ) );
            assertThat( rickyEntity.spouse().get(), nullValue() );
            assertThat( rickyEntity.children().count(), is( 0 ) );

            uow.complete();
        }
    }

    @Test
    public void givenUnqualifiedValueWhenUpdatingEntityExpectCorrectEntity()
        throws UnitOfWorkCompletionException
    {
        String rickyIdentity;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "CreateRickySlaghoopleWithTypo" ) ) )
        {
            PersonEntity ricky = createPerson( uow, "Ricky", "Slaghople", someBirthDate );
            ricky.spouse().set( uow.get( PersonEntity.class, ednaIdentity ) );
            ricky.children().add( uow.get( PersonEntity.class, zekeIdentity ) );
            rickyIdentity = ricky.identity().get();
            assertThat( ricky.spouse().get(), notNullValue() );
            assertThat( ricky.children().count(), is( 1 ) );
            uow.complete();
        }
        ValueBuilder<PersonValue2> builder = valueBuilderFactory.newValueBuilder( PersonValue2.class );
        builder.prototype().firstName().set( "Ricky" );
        builder.prototype().lastName().set( "Slaghoople" );
        builder.prototype().dateOfBirth().set( someBirthDate );
        PersonValue2 newStateValue = builder.newInstance();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "UpdateRickySlaghoople" ) ) )
        {
            PersonEntity ricky = uow.get( PersonEntity.class, rickyIdentity );

            ValueToEntity conversion = serviceFinder.findService( ValueToEntity.class ).get();
            conversion.update( ricky, newStateValue );

            assertThat( ricky.lastName(), equalTo( "Slaghoople" ) );
            assertThat( ricky.spouse().get(), nullValue() );
            assertThat( ricky.children().count(), is( 0 ) );

            uow.complete();
        }
    }

    @Test
    public void givenUnqualifiedValue2WhenUpdatingEntityExpectCorrectEntity()
        throws UnitOfWorkCompletionException
    {
        String rickyIdentity;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "CreateRickySlaghoopleWithTypo" ) ) )
        {
            PersonEntity ricky = createPerson( uow, "Ricky", "Slaghople", someBirthDate );
            ricky.spouse().set( uow.get( PersonEntity.class, ednaIdentity ) );
            ricky.children().add( uow.get( PersonEntity.class, zekeIdentity ) );
            rickyIdentity = ricky.identity().get();
            assertThat( ricky.spouse().get(), notNullValue() );
            assertThat( ricky.children().count(), is( 1 ) );
            uow.complete();
        }
        ValueBuilder<PersonValue3> builder = valueBuilderFactory.newValueBuilder( PersonValue3.class );
        builder.prototype().firstName().set( "Ricky" );
        builder.prototype().lastName().set( "Slaghoople" );
        builder.prototype().dateOfBirth().set( someBirthDate );
        PersonValue3 newStateValue = builder.newInstance();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "UpdateRickySlaghoople" ) ) )
        {
            PersonEntity ricky = uow.get( PersonEntity.class, rickyIdentity );

            ValueToEntity conversion = serviceFinder.findService( ValueToEntity.class ).get();
            conversion.update( ricky, newStateValue );

            assertThat( ricky.lastName(), equalTo( "Slaghoople" ) );
            assertThat( ricky.spouse().get(), nullValue() );
            assertThat( ricky.children().count(), is( 0 ) );

            uow.complete();
        }
    }

    @Test
    public void givenQualifiedValueNotFromSameInterfaceWhenUpdatingEntityExpectPropsNotUpdated()
        throws UnitOfWorkCompletionException
    {
        String rickyIdentity;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "CreateRickySlaghoopleWithTypo" ) ) )
        {
            PersonEntity ricky = createPerson( uow, "Ricky", "Slaghople", someBirthDate );
            ricky.spouse().set( uow.get( PersonEntity.class, ednaIdentity ) );
            ricky.children().add( uow.get( PersonEntity.class, zekeIdentity ) );
            rickyIdentity = ricky.identity().get();
            assertThat( ricky.spouse().get(), notNullValue() );
            assertThat( ricky.children().count(), is( 1 ) );
            uow.complete();
        }
        ValueBuilder<PersonValue4> builder = valueBuilderFactory.newValueBuilder( PersonValue4.class );
        builder.prototype().firstName().set( "Ricky" );
        builder.prototype().lastName().set( "Slaghoople" );
        builder.prototype().dateOfBirth().set( someBirthDate );
        PersonValue4 newStateValue = builder.newInstance();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( newUsecase( "UpdateRickySlaghoopleWontWork" ) ) )
        {
            PersonEntity ricky = uow.get( PersonEntity.class, rickyIdentity );

            ValueToEntity conversion = serviceFinder.findService( ValueToEntity.class ).get();
            conversion.update( ricky, newStateValue );

            assertThat( ricky.lastName(), equalTo( "Slaghople" ) );
            assertThat( ricky.spouse().get(), nullValue() );
            assertThat( ricky.children().count(), is( 0 ) );

            uow.complete();
        }
    }
}
