/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2009-2012 Niclas Hedhman.
 * Copyright 2014 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.indexing;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.query.NotQueryableException;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.IndexExporter;
import org.qi4j.test.indexing.model.Account;
import org.qi4j.test.indexing.model.City;
import org.qi4j.test.indexing.model.Domain;
import org.qi4j.test.indexing.model.Female;
import org.qi4j.test.indexing.model.File;
import org.qi4j.test.indexing.model.Male;
import org.qi4j.test.indexing.model.Nameable;
import org.qi4j.test.indexing.model.Person;
import org.qi4j.test.indexing.model.QueryParam;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.DateTimeZone.forID;
import static org.junit.Assert.assertThat;
import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.contains;
import static org.qi4j.api.query.QueryExpressions.containsName;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.ge;
import static org.qi4j.api.query.QueryExpressions.gt;
import static org.qi4j.api.query.QueryExpressions.isNotNull;
import static org.qi4j.api.query.QueryExpressions.isNull;
import static org.qi4j.api.query.QueryExpressions.lt;
import static org.qi4j.api.query.QueryExpressions.matches;
import static org.qi4j.api.query.QueryExpressions.ne;
import static org.qi4j.api.query.QueryExpressions.not;
import static org.qi4j.api.query.QueryExpressions.oneOf;
import static org.qi4j.api.query.QueryExpressions.or;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_GeometryFromText;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Within;
import static org.qi4j.test.indexing.NameableAssert.verifyOrderedResults;
import static org.qi4j.test.indexing.NameableAssert.verifyUnorderedResults;

/**
 * Abstract satisfiedBy with tests for simple queries against Index/Query engines.
 */
public abstract class AbstractQueryTest
    extends AbstractAnyQueryTest
{

    @Test
    public void showNetwork()
        throws IOException
    {
        IndexExporter indexerExporter = module.findService( IndexExporter.class ).get();
        indexerExporter.exportReadableToStream( System.out );
    }

    @Test
    public void script01()
        throws EntityFinderException
    {
        final QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        final Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script01: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script02()
        throws EntityFinderException
    {
        final QueryBuilder<Domain> qb = this.module.newQueryBuilder( Domain.class );
        final Nameable nameable = templateFor( Nameable.class );
        final Query<Domain> query = unitOfWork.newQuery( qb.where( eq( nameable.name(), "Gaming" ) ) );
        System.out.println( "*** script02: " + query );
        verifyUnorderedResults( query, "Gaming" );
    }

    @Test
    public void script03()
        throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = this.module.newQueryBuilder( Nameable.class );
        Query<Nameable> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script03: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe", "Penang", "Kuala Lumpur", "Cooking", "Gaming",
                                "Programming", "Cars" );
    }

    @Test
    public void script04()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person personTemplate = templateFor( Person.class );
        City placeOfBirth = personTemplate.placeOfBirth().get();
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( placeOfBirth.name(), "Kuala Lumpur" ) ) );
        System.out.println( "*** script04: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script04_ne()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person personTemplate = templateFor( Person.class );
        City placeOfBirth = personTemplate.placeOfBirth().get();
        Query<Person> query = unitOfWork.newQuery( qb.where( ne( placeOfBirth.name(), "Kuala Lumpur" ) ) );
        System.out.println( "*** script04_ne: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script05()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
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
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( ge( person.yearOfBirth(), 1973 ) ) );
        System.out.println( "*** script06: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void script07()
        throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = this.module.newQueryBuilder( Nameable.class );
        Person person = templateFor( Person.class );
        Query<Nameable> query = unitOfWork.newQuery( qb.where(
            and( ge( person.yearOfBirth(), 1900 ), eq( person.placeOfBirth().get().name(), "Penang" ) ) ) );
        System.out.println( "*** script07: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void script08()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( or( eq( person.yearOfBirth(), 1970 ), eq( person.yearOfBirth(), 1975 ) ) )
        );
        System.out.println( "*** script08: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void script09()
        throws EntityFinderException
    {
        QueryBuilder<Female> qb = this.module.newQueryBuilder( Female.class );
        Person person = templateFor( Person.class );
        Query<Female> query = unitOfWork.newQuery( qb.where( or( eq( person.yearOfBirth(), 1970 ), eq( person.yearOfBirth(), 1975 ) ) )
        );
        System.out.println( "*** script09: " + query );
        verifyUnorderedResults( query, "Ann Doe" );
    }

    @Test
    public void script10()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( not( eq( person.yearOfBirth(), 1975 ) ) ) );
        System.out.println( "*** script10: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script11()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( isNotNull( person.email() ) ) );
        System.out.println( "*** script11: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script12()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( isNull( person.email() ) ) );
        System.out.println( "*** script12: " + query );
        verifyUnorderedResults( query, "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script12_ne()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( ne( person.email(), "joe@thedoes.net" ) ) );
        System.out.println( "*** script12_ne: " + query );
        verifyUnorderedResults( query );
    }

    @Test
    public void script13()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( isNotNull( person.wife() ) ) );
        System.out.println( "*** script13: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script14()
        throws EntityFinderException
    {
        QueryBuilder<Male> qb = this.module.newQueryBuilder( Male.class );
        Male person = templateFor( Male.class );
        Query<Male> query = unitOfWork.newQuery( qb.where( isNull( person.wife() ) ) );
        System.out.println( "*** script14: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script15()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( isNull( person.wife() ) ) );
        System.out.println( "*** script15: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script16()
        throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = this.module.newQueryBuilder( Nameable.class );
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
        throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = this.module.newQueryBuilder( Nameable.class );
        // should return only 3 entities starting with forth one
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = unitOfWork.newQuery( qb );
        query.orderBy( orderBy( nameable.name() ) );
        query.firstResult( 3 );
        query.maxResults( 2 );
        System.out.println( "*** script17: " + query );
        verifyOrderedResults( query, "Gaming", "Jack Doe" );
    }

    @Test
    public void script18()
        throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = this.module.newQueryBuilder( Nameable.class );
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = unitOfWork.newQuery( qb );
        query.orderBy( orderBy( nameable.name() ) );
        System.out.println( "*** script18: " + query );
        verifyOrderedResults( query, "Ann Doe", "Cars", "Cooking", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur",
                              "Penang", "Programming" );
    }

    @Test
    public void script19()
        throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = this.module.newQueryBuilder( Nameable.class );
        // should return all Nameable entities with a name > "D" sorted by name
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = unitOfWork.newQuery( qb.where( gt( nameable.name(), "D" ) ) );
        query.orderBy( orderBy( nameable.name() ) );
        System.out.println( "*** script19: " + query );
        verifyOrderedResults( query, "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming" );
    }

    @Test
    public void script20()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( gt( person.yearOfBirth(), 1973 ) ) );
        query.orderBy( orderBy( person.name(), OrderBy.Order.DESCENDING ) );
        System.out.println( "*** script20: " + query );
        verifyOrderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script21()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        // should return all Persons sorted by name of the city they were born, and then by year they were born
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb );
        query.orderBy( orderBy( person.placeOfBirth().get().name() ), orderBy( person.yearOfBirth() ) );
        System.out.println( "*** script21: " + query );
        verifyOrderedResults( query, "Ann Doe", "Joe Doe", "Jack Doe" );
    }

    @Test
    public void script22()
        throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = this.module.newQueryBuilder( Nameable.class );
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        Query<Nameable> query = unitOfWork.newQuery( qb.where( matches( nameable.name(), "J.*Doe" ) ) );
        System.out.println( "*** script22: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script23()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Domain interests = oneOf( person.interests() );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( interests.name(), "Cars" ) ) );
        System.out.println( "*** script23: " + query );
        verifyOrderedResults( query, "Jack Doe" );
    }

    @Test
    public void script24()
        throws EntityFinderException
    {
        final QueryBuilder<Domain> qb = this.module.newQueryBuilder( Domain.class );
        final Nameable nameable = templateFor( Nameable.class );
        final Query<Domain> query = unitOfWork.newQuery( qb.where( eq( nameable.name(), "Gaming" ) ) );
        System.out.println( "*** script24: " + query );
        assertThat( query.find().name().get(), is( equalTo( "Gaming" ) ) );
    }

    @Test( expected = NotQueryableException.class )
    public void script25()
    {
        this.module.newQueryBuilder( File.class );
    }

    @Test( expected = NotQueryableException.class )
    public void script26()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where( eq( person.personalWebsite().get().file().get().value(), "some/path" ) );
    }

    @Test( expected = NotQueryableException.class )
    public void script27()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where( eq( person.personalWebsite().get().host().get().value(), "www.qi4j.org" ) );
    }

    @Test( expected = NotQueryableException.class )
    public void script28()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where( eq( person.personalWebsite().get().port().get().value(), 8080 ) );
    }

    @Test
    public void script29()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
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
    @Ignore( "Wait till 1.1?" )
    // Paul: I don't understand this test
    @SuppressWarnings( "unchecked" )
    public void script30()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        QueryParam queryParam = null; // oneOf( person.personalWebsite().get().queryParams() );
        Query<Person> query = unitOfWork.newQuery( qb.where( and( eq( queryParam.name(), "foo" ), eq( queryParam.value(), "bar" ) ) ) );
        System.out.println( "*** script30: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    @Ignore( "Equality on Property<Map<?,?>> not implemented" )
    public void script31()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Map<String, String> info = new HashMap<>( 0 );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.additionalInfo(), info ) ) );
        System.out.println( "*** script31: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script32()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.address().get().line1(), "Qi Alley 4j" ) ) );
        System.out.println( "*** script32: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script33()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Domain gaming = unitOfWork.get( Domain.class, "Gaming" );
        Query<Person> query = unitOfWork.newQuery( qb.where( contains( person.interests(), gaming ) ) );
        System.out.println( "*** script33: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script34()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Female annDoe = unitOfWork.get( Female.class, "anndoe" );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.mother(), annDoe ) ) );
        System.out.println( "*** script34: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script35()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( containsName( person.accounts(), "anns" ) ) );
        System.out.println( "*** script35: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    public void script36()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Account anns = unitOfWork.get( Account.class, "accountOfAnnDoe" );
        Query<Person> query = unitOfWork.newQuery( qb.where( contains( person.accounts(), anns ) ) );
        System.out.println( "*** script36: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    @Ignore( "Traversing of NamedAssociations is not implemented" )
    public void script37()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.accounts().get( "anns" ).number(),
                                                                 "accountOfAnnDoe" ) ) );
        System.out.println( "*** script37: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    public void script38()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.title(), Person.Title.DR ) ) );
        System.out.println( "*** script38: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script39()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where( ne( person.title(), Person.Title.DR ) ) );
        System.out.println( "*** script39: " + query );

        verifyUnorderedResults( query, "Ann Doe", "Joe Doe" );
    }

    @Test
    public void script40_Date()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.dateValue(), new DateTime( "2010-03-04T13:24:35", UTC ).toDate() ) ) );
        System.out.println( "*** script40_Date: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script41_Date()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.dateValue(), new DateTime( "2010-03-04T13:24:35", UTC ).toDate() ) ) );
        System.out.println( "*** script41_Date: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script42_Date()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.dateValue(), new DateTime( "2010-03-04T14:24:35", forID( "CET" ) ).toDate() ) ) );
        System.out.println( "*** script42_Date: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script43_Date()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            and( gt( person.dateValue(), new DateTime( "2005-03-04T13:24:35", UTC ).toDate() ),
                 lt( person.dateValue(), new DateTime( "2015-03-04T13:24:35", UTC ).toDate() ) ) ) );
        System.out.println( "*** script43_Date: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script40_DateTime()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.dateTimeValue(), new DateTime( "2010-03-04T13:24:35", UTC ) ) ) );
        System.out.println( "*** script40_DateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script41_DateTime()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.dateTimeValue(), new DateTime( "2010-03-04T13:24:35", UTC ) ) ) );
        System.out.println( "*** script41_DateTime: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script42_DateTime()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.dateTimeValue(), new DateTime( "2010-03-04T14:24:35", forID( "CET" ) ) ) ) );
        System.out.println( "*** script42_DateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script43_DateTime()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            and( gt( person.dateTimeValue(), new DateTime( "2005-03-04T13:24:35", UTC ) ),
                 lt( person.dateTimeValue(), new DateTime( "2015-03-04T13:24:35", UTC ) ) ) ) );
        System.out.println( "*** script43_DateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script40_LocalDateTime()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.localDateTimeValue(), new LocalDateTime( "2010-03-04T13:23:00", UTC ) ) ) );
        System.out.println( "*** script40_LocalDateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script41_LocalDateTime()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.localDateTimeValue(), new LocalDateTime( "2010-03-04T13:23:00", UTC ) ) ) );
        System.out.println( "*** script41_LocalDateTime: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script42_LocalDateTime()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.localDateTimeValue(), new LocalDateTime( "2010-03-04T13:23:00", forID( "CET" ) ) ) ) );
        System.out.println( "*** script42_LocalDateTime: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script43_LocalDateTime()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            and( gt( person.localDateTimeValue(), new LocalDateTime( "2005-03-04T13:24:35", UTC ) ),
                 lt( person.localDateTimeValue(), new LocalDateTime( "2015-03-04T13:24:35", UTC ) ) ) ) );
        System.out.println( "*** script43_LocalDateTime: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script40_LocalDate()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.localDateValue(), new LocalDate( "2010-03-04", UTC ) ) ) );
        System.out.println( "*** script40_LocalDate: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script41_LocalDate()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.localDateValue(), new LocalDate( "2010-03-04", UTC ) ) ) );
        System.out.println( "*** script41_LocalDate: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script42_LocalDate()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.localDateValue(), new LocalDate( "2010-03-04", forID( "CET" ) ) ) ) );
        System.out.println( "*** script42_LocalDate: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script43_LocalDate()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            and( gt( person.localDateValue(), new LocalDate( "2005-03-04", UTC ) ),
                 lt( person.localDateValue(), new LocalDate( "2015-03-04", UTC ) ) ) ) );
        System.out.println( "*** script43_LocalDate: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script50_BigInteger()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.bigInteger(), new BigInteger( "23232323232323232323232323" ) ) ) );
        System.out.println( "*** script50_BigInteger: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script51_BigInteger()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.bigInteger(), new BigInteger( "23232323232323232323232323" ) ) ) );
        System.out.println( "*** script51_BigInteger: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script52_BigInteger()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ge( person.bigInteger(), new BigInteger( "23232323232323232323232323" ) ) ) );
        System.out.println( "*** script52_BigInteger: " + query );

        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script50_BigDecimal()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            eq( person.bigDecimal(), new BigDecimal( "2342.76931348623157e+307" ) ) ) );
        System.out.println( "*** script50_BigDecimal: " + query );

        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script51_BigDecimal()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Query<Person> query = unitOfWork.newQuery( qb.where(
            ne( person.bigDecimal(), new BigDecimal( "2342.76931348623157e+307" ) ) ) );
        System.out.println( "*** script51_BigDecimal: " + query );

        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script52_BigDecimal() {
        QueryBuilder<Person> qb = this.module.newQueryBuilder(Person.class);
        Person person = templateFor(Person.class);
        Query<Person> query = unitOfWork.newQuery(qb.where(
                ge(person.bigDecimal(), new BigDecimal("2342.76931348623157e+307"))));
        System.out.println("*** script52_BigDecimal: " + query);

        verifyUnorderedResults(query, "Jack Doe", "Joe Doe");
    }
}
