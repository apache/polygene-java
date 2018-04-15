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
package org.apache.polygene.test.indexing;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.query.NotQueryableException;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.grammar.OrderBy;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.spi.query.IndexExporter;
import org.apache.polygene.test.model.Account;
import org.apache.polygene.test.model.City;
import org.apache.polygene.test.model.Domain;
import org.apache.polygene.test.model.Female;
import org.apache.polygene.test.model.File;
import org.apache.polygene.test.model.Male;
import org.apache.polygene.test.model.Nameable;
import org.apache.polygene.test.model.Person;
import org.apache.polygene.test.model.QueryParam;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static java.time.ZoneOffset.UTC;
import static org.apache.polygene.api.query.QueryExpressions.and;
import static org.apache.polygene.api.query.QueryExpressions.contains;
import static org.apache.polygene.api.query.QueryExpressions.containsName;
import static org.apache.polygene.api.query.QueryExpressions.eq;
import static org.apache.polygene.api.query.QueryExpressions.ge;
import static org.apache.polygene.api.query.QueryExpressions.gt;
import static org.apache.polygene.api.query.QueryExpressions.isNotNull;
import static org.apache.polygene.api.query.QueryExpressions.isNull;
import static org.apache.polygene.api.query.QueryExpressions.lt;
import static org.apache.polygene.api.query.QueryExpressions.matches;
import static org.apache.polygene.api.query.QueryExpressions.ne;
import static org.apache.polygene.api.query.QueryExpressions.not;
import static org.apache.polygene.api.query.QueryExpressions.oneOf;
import static org.apache.polygene.api.query.QueryExpressions.or;
import static org.apache.polygene.api.query.QueryExpressions.orderBy;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;
import static org.apache.polygene.test.indexing.NameableAssert.verifyOrderedResults;
import static org.apache.polygene.test.indexing.NameableAssert.verifyUnorderedResults;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Abstract satisfiedBy with tests for simple queries against Index/Query engines.
 */
public abstract class AbstractQueryTest
    extends AbstractAnyQueryTest
{
    @Structure
    Module moduleInstance;

    @Test
    public void showNetwork()
        throws IOException
    {
        IndexExporter indexerExporter = moduleInstance.findService( IndexExporter.class ).get();
        indexerExporter.exportReadableToStream( System.out );
    }

    @Test
    public void script01()
    {
        final QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        final Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script01: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script02()
    {
        final QueryBuilder<Domain> qb = this.moduleInstance.newQueryBuilder( Domain.class );
        final Nameable nameable = templateFor( Nameable.class );
        final Query<Domain> query = unitOfWork.newQuery( qb.where( eq( nameable.name(), "Gaming" ) ) );
        System.out.println( "*** script02: " + query );
        verifyUnorderedResults( query, "Gaming" );
    }

    @Test
    public void script03()
    {
        QueryBuilder<Nameable> qb = this.moduleInstance.newQueryBuilder( Nameable.class );
        Query<Nameable> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script03: " + query );
        verifyUnorderedResults( query, "Felix", "Joe Doe", "Ann Doe", "Jack Doe", "Penang", "Kuala Lumpur", "Cooking", "Gaming",
                                "Programming", "Cars" );
    }

    @Test
    public void script04()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person personTemplate = templateFor( Person.class );
        City placeOfBirth = personTemplate.placeOfBirth().get();
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( placeOfBirth.name(), "Kuala Lumpur" ) ) );
        System.out.println( "*** script04: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script04_ne()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person personTemplate = templateFor( Person.class );
        City placeOfBirth = personTemplate.placeOfBirth().get();
        Query<Person> query = unitOfWork.newQuery( qb.where( ne( placeOfBirth.name(), "Kuala Lumpur" ) ) );
        System.out.println( "*** script04_ne: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script05()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.mother()
                                                                     .get()
                                                                     .placeOfBirth()
                                                                     .get()
                                                                     .name(), "Kuala Lumpur" ) )
        );
        System.out.println( "*** script05: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script06()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( ge( person.yearOfBirth(), 1973 ) ) );
        System.out.println( "*** script06: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void script07()
    {
        QueryBuilder<Nameable> qb = this.moduleInstance.newQueryBuilder( Nameable.class );
        Person person = templateFor( Person.class );
        Query<Nameable> query = unitOfWork.newQuery( qb.where(
            and( ge( person.yearOfBirth(), 1900 ), eq( person.placeOfBirth().get().name(), "Penang" ) ) ) );
        System.out.println( "*** script07: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void script08()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( or( eq( person.yearOfBirth(), 1970 ), eq( person.yearOfBirth(), 1975 ) ) )
        );
        System.out.println( "*** script08: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void script09()
    {
        QueryBuilder<Female> qb = this.moduleInstance.newQueryBuilder( Female.class );
        Person person = templateFor( Person.class );
        Query<Female> query = unitOfWork.newQuery( qb.where( or( eq( person.yearOfBirth(), 1970 ), eq( person.yearOfBirth(), 1975 ) ) )
        );
        System.out.println( "*** script09: " + query );
        verifyUnorderedResults( query, "Ann Doe" );
    }

    @Test
    public void script10()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( not( eq( person.yearOfBirth(), 1975 ) ) ) );
        System.out.println( "*** script10: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script11()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( isNotNull( person.email() ) ) );
        System.out.println( "*** script11: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script12()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( isNull( person.email() ) ) );
        System.out.println( "*** script12: " + query );
        verifyUnorderedResults( query, "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script12_ne()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( ne( person.email(), "joe@thedoes.net" ) ) );
        System.out.println( "*** script12_ne: " + query );
        verifyUnorderedResults( query );
    }

    @Test
    public void script13()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( isNotNull( person.wife() ) ) );
        System.out.println( "*** script13: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script14()
    {
        QueryBuilder<Male> qb = this.moduleInstance.newQueryBuilder( Male.class );
        Male person = templateFor( Male.class );
        Query<Male> query = unitOfWork.newQuery( qb.where( isNull( person.wife() ) ) );
        System.out.println( "*** script14: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script15()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( isNull( person.wife() ) ) );
        System.out.println( "*** script15: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script16()
    {
        QueryBuilder<Nameable> qb = this.moduleInstance.newQueryBuilder( Nameable.class );
        // should return only 2 entities
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = unitOfWork.newQuery( qb );
        query.orderBy( orderBy( nameable.name() ) );
        query.maxResults( 2 );
        System.out.println( "*** script16: " + query );
        verifyOrderedResults( query, "Ann Doe", "Cars" );
    }

    @Test
    public void script17()
    {
        QueryBuilder<Nameable> qb = this.moduleInstance.newQueryBuilder( Nameable.class );
        // should return only 3 entities starting with forth one
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = unitOfWork.newQuery( qb );
        query.orderBy( orderBy( nameable.name() ) );
        query.firstResult( 3 );
        query.maxResults( 2 );
        System.out.println( "*** script17: " + query );
        verifyOrderedResults( query, "Felix", "Gaming" );
    }

    @Test
    public void script18()
    {
        QueryBuilder<Nameable> qb = this.moduleInstance.newQueryBuilder( Nameable.class );
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = unitOfWork.newQuery( qb );
        query.orderBy( orderBy( nameable.name() ) );
        System.out.println( "*** script18: " + query );
        verifyOrderedResults( query, "Ann Doe", "Cars", "Cooking", "Felix", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur",
                              "Penang", "Programming" );
    }

    @Test
    public void script19()
    {
        QueryBuilder<Nameable> qb = this.moduleInstance.newQueryBuilder( Nameable.class );
        // should return all Nameable entities with a name > "D" sorted by name
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = unitOfWork.newQuery( qb.where( gt( nameable.name(), "D" ) ) );
        query.orderBy( orderBy( nameable.name() ) );
        System.out.println( "*** script19: " + query );
        verifyOrderedResults( query, "Felix", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming" );
    }

    @Test
    public void script20()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( gt( person.yearOfBirth(), 1973 ) ) );
        query.orderBy( orderBy( person.name(), OrderBy.Order.DESCENDING ) );
        System.out.println( "*** script20: " + query );
        verifyOrderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script21()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        // should return all Persons sorted by name of the city they were born, and then by year they were born
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb );
        query.orderBy( orderBy( person.placeOfBirth().get().name() ), orderBy( person.yearOfBirth() ) );
        System.out.println( "*** script21: " + query );
        verifyOrderedResults( query, "Ann Doe", "Joe Doe", "Jack Doe" );
    }

    @Test
    public void script22()
    {
        QueryBuilder<Nameable> qb = this.moduleInstance.newQueryBuilder( Nameable.class );
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        Query<Nameable> query = unitOfWork.newQuery( qb.where( matches( nameable.name(), "J.*Doe" ) ) );
        System.out.println( "*** script22: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script23()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Domain interests = oneOf( person.interests() );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( interests.name(), "Cars" ) ) );
        System.out.println( "*** script23: " + query );
        verifyOrderedResults( query, "Jack Doe" );
    }

    @Test
    public void script24()
    {
        final QueryBuilder<Domain> qb = this.moduleInstance.newQueryBuilder( Domain.class );
        final Nameable nameable = templateFor( Nameable.class );
        final Query<Domain> query = unitOfWork.newQuery( qb.where( eq( nameable.name(), "Gaming" ) ) );
        System.out.println( "*** script24: " + query );
        assertThat( query.find().name().get(), is( equalTo( "Gaming" ) ) );
    }

    @Test
    public void script25()
    {
        assertThrows( NotQueryableException.class, () ->
            this.moduleInstance.newQueryBuilder( File.class )
        );
    }

    @Test
    public void script26()
    {
        assertThrows( NotQueryableException.class, () -> {
            QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
            Person person = templateFor( Person.class );
            qb.where( eq( person.personalWebsite().get().file().get().value(), "some/path" ) );
        } );
    }

    @Test
    public void script27()
    {
        assertThrows( NotQueryableException.class, () -> {
            QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
            Person person = templateFor( Person.class );
            qb.where( eq( person.personalWebsite().get().host().get().value(), "polygene.apache.org" ) );
        } );
    }

    @Test
    public void script28()
    {
        assertThrows( NotQueryableException.class, () -> {
            QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
            Person person = templateFor( Person.class );
            qb.where( eq( person.personalWebsite().get().port().get().value(), 8080 ) );
        } );
    }

    @Test
    public void script29()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.personalWebsite()
                                                                     .get()
                                                                     .protocol()
                                                                     .get()
                                                                     .value(), "http" ) )
        );
        System.out.println( "*** script29: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    @Disabled( "Wait till 1.1?" )
    // Paul: I don't understand this test
    @SuppressWarnings( "unchecked" )
    public void script30()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        QueryParam queryParam = null; //oneOf( person.personalWebsite().get().queryParams() );
        Query<Person> query = unitOfWork.newQuery( qb.where( and( eq( queryParam.name(), "foo" ), eq( queryParam.value(), "bar" ) ) ) );
        System.out.println( "*** script30: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    @Disabled( "Equality on Property<Map<?,?>> not implemented" )
    public void script31()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Map<String, String> info = new HashMap<>( 0 );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.additionalInfo(), info ) ) );
        System.out.println( "*** script31: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script32()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.address().get().line1(), "Qi Alley 4j" ) ) );
        System.out.println( "*** script32: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script33()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Domain gaming = unitOfWork.get( Domain.class, StringIdentity.identityOf( "Gaming" ) );
        Query<Person> query = unitOfWork.newQuery( qb.where( contains( person.interests(), gaming ) ) );
        System.out.println( "*** script33: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script34()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Female annDoe = unitOfWork.get( Female.class, StringIdentity.identityOf( "anndoe" ) );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.mother(), annDoe ) ) );
        System.out.println( "*** script34: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script35()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( containsName( person.accounts(), "anns" ) ) );
        System.out.println( "*** script35: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    public void script36()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Account anns = unitOfWork.get( Account.class, StringIdentity.identityOf( "accountOfAnnDoe" ) );
        Query<Person> query = unitOfWork.newQuery( qb.where( contains( person.accounts(), anns ) ) );
        System.out.println( "*** script36: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    @Disabled( "Traversing of NamedAssociations is not implemented" )
    public void script37()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.accounts().get( "anns" ).number(),
                                                                 "accountOfAnnDoe" ) ) );
        System.out.println( "*** script37: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    public void script38()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.title(), Person.Title.DR ) ) );
        System.out.println( "*** script38: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script39()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( ne( person.title(), Person.Title.DR ) ) );
        System.out.println( "*** script39: " + query );

        verifyUnorderedResults( query, "Ann Doe", "Joe Doe" );
    }

    @Test
    public void script40_Date()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Instant refInstant = ZonedDateTime.of( 2010, 3, 4, 13, 24, 35, 0, UTC ).toInstant();
        System.out.println( refInstant );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.instantValue(), refInstant ) ) );
        System.out.println( "*** script40_Date: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script41_Instant()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Instant refInstant = ZonedDateTime.of( 2010, 3, 4, 13, 24, 35, 0, UTC ).toInstant();
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.instantValue(), refInstant ) ) );
        System.out.println( "*** script41_Instant: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script42_Instant()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        ZonedDateTime cetTime = ZonedDateTime.of( 2010, 3, 4, 14, 24, 35, 0, ZoneId.of( "CET" ) );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.instantValue(), cetTime.toInstant() ) ) );
        System.out.println( "*** script42_Instant: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script43_Date()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        ZonedDateTime start = ZonedDateTime.of( 2005, 3, 4, 13, 24, 35, 0, UTC );
        ZonedDateTime end = ZonedDateTime.of( 2015, 3, 4, 13, 24, 35, 0, UTC );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            and( gt( person.instantValue(), start.toInstant() ),
                 lt( person.instantValue(), end.toInstant() ) ) ) );
        System.out.println( "*** script43_Date: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script40_DateTime()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        ZonedDateTime time = ZonedDateTime.of( 2010, 3, 4, 13, 24, 35, 0, UTC );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.dateTimeValue(), time ) ) );
        System.out.println( "*** script40_DateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script41_DateTime()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        ZonedDateTime time = ZonedDateTime.of( 2010, 3, 4, 13, 24, 35, 0, UTC );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.dateTimeValue(), time ) ) );
        System.out.println( "*** script41_DateTime: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script42_DateTime()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        ZonedDateTime time = ZonedDateTime.of( 2010, 3, 4, 13, 24, 35, 0, ZoneId.of( "CET" ) );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.dateTimeValue(), time ) ) );
        System.out.println( "*** script42_DateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script43_DateTime()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        ZonedDateTime start = ZonedDateTime.of( 2005, 3, 4, 13, 24, 35, 0, UTC );
        ZonedDateTime end = ZonedDateTime.of( 2015, 3, 4, 13, 24, 35, 0, UTC );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            and( gt( person.dateTimeValue(), start ),
                 lt( person.dateTimeValue(), end ) ) ) );
        System.out.println( "*** script43_DateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script40_LocalDateTime()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.localDateTimeValue(), LocalDateTime.of( 2010, 3, 4, 13, 23, 0 ) ) ) );
        System.out.println( "*** script40_LocalDateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script41_LocalDateTime()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.localDateTimeValue(), LocalDateTime.of( 2010, 3, 4, 13, 23, 0 ) ) ) );
        System.out.println( "*** script41_LocalDateTime: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script42_LocalDateTime()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        LocalDateTime time = LocalDateTime.of( 2010, 3, 4, 13, 23, 0 );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.localDateTimeValue(), time ) ) );
        System.out.println( "*** script42_LocalDateTime: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script43_LocalDateTime()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        LocalDateTime start = ZonedDateTime.of( 2005, 3, 4, 13, 24, 35, 0, UTC ).toLocalDateTime();
        LocalDateTime end = ZonedDateTime.of( 2015, 3, 4, 13, 24, 35, 0, UTC ).toLocalDateTime();
        Query<Person> query = unitOfWork.newQuery( qb.where(
            and( gt( person.localDateTimeValue(), start ),
                 lt( person.localDateTimeValue(), end ) ) ) );
        System.out.println( "*** script43_LocalDateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script40_LocalDate()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.localDateValue(), LocalDate.of( 2010, 3, 4 ) ) ) );
        System.out.println( "*** script40_LocalDate: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script41_LocalDate()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.localDateValue(), LocalDate.of( 2010, 3, 4 ) ) ) );
        System.out.println( "*** script41_LocalDate: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script42_LocalDate()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        LocalDate time = ZonedDateTime.of( 2010, 3, 4, 13, 24, 35, 0, ZoneId.of( "CET" ) ).toLocalDate();
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.localDateValue(), time ) ) );
        System.out.println( "*** script42_LocalDate: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script43_LocalDate()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        LocalDate start = ZonedDateTime.of( 2005, 3, 4, 13, 24, 35, 0, UTC ).toLocalDate();
        LocalDate end = ZonedDateTime.of( 2015, 3, 4, 13, 24, 35, 0, UTC ).toLocalDate();
        Query<Person> query = unitOfWork.newQuery( qb.where(
            and( gt( person.localDateValue(), start ),
                 lt( person.localDateValue(), end ) ) ) );
        System.out.println( "*** script43_LocalDate: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script50_BigInteger()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.bigInteger(), new BigInteger( "23232323232323232323232323" ) ) ) );
        System.out.println( "*** script50_BigInteger: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script51_BigInteger()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.bigInteger(), new BigInteger( "23232323232323232323232323" ) ) ) );
        System.out.println( "*** script51_BigInteger: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script52_BigInteger()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ge( person.bigInteger(), new BigInteger( "23232323232323232323232323" ) ) ) );
        System.out.println( "*** script52_BigInteger: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script50_BigDecimal()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.bigDecimal(), new BigDecimal( "2342.76931348623157e+307" ) ) ) );
        System.out.println( "*** script50_BigDecimal: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script51_BigDecimal()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.bigDecimal(), new BigDecimal( "2342.76931348623157e+307" ) ) ) );
        System.out.println( "*** script51_BigDecimal: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script52_BigDecimal()
    {
        QueryBuilder<Person> qb = this.moduleInstance.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ge( person.bigDecimal(), new BigDecimal( "2342.76931348623157e+307" ) ) ) );
        System.out.println( "*** script52_BigDecimal: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }
}
