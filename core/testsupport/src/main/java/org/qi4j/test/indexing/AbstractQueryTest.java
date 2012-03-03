/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2009 Niclas Hedhman.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.query.NotQueryableException;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.IndexExporter;
import org.qi4j.test.indexing.model.City;
import org.qi4j.test.indexing.model.Domain;
import org.qi4j.test.indexing.model.Female;
import org.qi4j.test.indexing.model.File;
import org.qi4j.test.indexing.model.Male;
import org.qi4j.test.indexing.model.Nameable;
import org.qi4j.test.indexing.model.Person;
import org.qi4j.test.indexing.model.QueryParam;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.contains;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.ge;
import static org.qi4j.api.query.QueryExpressions.gt;
import static org.qi4j.api.query.QueryExpressions.isNotNull;
import static org.qi4j.api.query.QueryExpressions.isNull;
import static org.qi4j.api.query.QueryExpressions.matches;
import static org.qi4j.api.query.QueryExpressions.not;
import static org.qi4j.api.query.QueryExpressions.oneOf;
import static org.qi4j.api.query.QueryExpressions.or;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

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

    private static void verifyUnorderedResults( final Iterable<? extends Nameable> results, final String... names )
    {
        final List<String> expected = new ArrayList<String>( Arrays.asList( names ) );

        for( Nameable entity : results )
        {
            String name = entity.name().get();
            assertTrue( name + " returned but not expected", expected.remove( name ) );
        }

        for( String notReturned : expected )
        {
            fail( notReturned + " was expected but not returned" );
        }
    }

    private static void verifyOrderedResults( final Iterable<? extends Nameable> results, final String... names )
    {
        final List<String> expected = new ArrayList<String>( Arrays.asList( names ) );
        final List<String> actual = new ArrayList<String>();
        for( Nameable result : results )
        {
            actual.add( result.name().get() );
        }

        assertThat( "Result is incorrect", actual, equalTo( expected ) );
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
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe", "Penang", "Kuala Lumpur", "Cooking", "Gaming",
                                "Programming", "Cars" );
        System.out.println( "*** script03: " + query );
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

    @Ignore( "Skip this one for now. It sporadically fails sometimes." )
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
    @Ignore( "Wait until indexing of complex values is implemented" )
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
    public void script30()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        QueryParam queryParam = null; // oneOf( person.personalWebsite().get().queryParams() );
        Query<Person> query = unitOfWork.newQuery( qb.where( and( eq( queryParam.name(), "foo" ), eq( queryParam.value(), "bar" ) ) )
        );
        System.out.println( "*** script30: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    @Ignore( "Wait till 1.1?" )
    public void script31()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Map<String, String> info = new HashMap<String, String>();
        Query<Person> query = unitOfWork.newQuery( qb.where( eq( person.additionalInfo(), info ) ) );
        System.out.println( "*** script31: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script32()
    {
        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Map<String, String> info = new HashMap<String, String>();
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

        verifyUnorderedResults( query, "Joe Doe" );
    }
}
