/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.index.rdf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.index.rdf.model.*;
import org.qi4j.index.rdf.model.entities.*;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.runtime.query.NotQueryableException;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.test.EntityTestAssembler;

import java.util.*;

public class RdfQueryTest
{

    private SingletonAssembler assembler;
    private QueryBuilderFactory qbf;
    private UnitOfWork unitOfWork;

    @Before
    public void setUp() throws UnitOfWorkCompletionException
    {
        assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addEntities(
                    MaleEntity.class,
                    FemaleEntity.class,
                    CityEntity.class,
                    DomainEntity.class,
                    AccountEntity.class,
                    CatEntity.class
                );
                module.addValues(
                    URL.class,
                    Protocol.class,
                    Host.class,
                    Port.class,
                    File.class,
                    QueryParam.class
                );
                new EntityTestAssembler().assemble( module );
                module.addServices(
                    MemoryRepositoryService.class,
                    RdfFactoryService.class,
                    RdfIndexerExporterComposite.class
                );
                module.addObjects( EntityStateSerializer.class, EntityTypeSerializer.class );
            }
        };
        Network.populate( assembler );
        unitOfWork = assembler.unitOfWorkFactory().newUnitOfWork();
        qbf = assembler.queryBuilderFactory();
    }

    @After
    public void tearDown()
    {
        if( unitOfWork != null )
        {
            unitOfWork.discard();
        }
    }

    @Test
    public void showNetwork()
    {
        ServiceFinder serviceFinder = assembler.serviceFinder();
        RdfIndexerExporterComposite rdfIndexerExporter =
            serviceFinder.<RdfIndexerExporterComposite>findService( RdfIndexerExporterComposite.class ).get();
        rdfIndexerExporter.toRDF( System.out );
    }

    private static void verifyUnorderedResults( final Iterable<? extends Nameable> results,
                                                final String... names )
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

    private static void verifyOrderedResults( final Iterable<? extends Nameable> results,
                                              final String... names )
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
    public void script01() throws EntityFinderException
    {
        final QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        final Query<Person> query = qb.newQuery(unitOfWork);
        System.out.println( query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script02() throws EntityFinderException
    {
        final QueryBuilder<Domain> qb = qbf.newQueryBuilder( Domain.class );
        final Nameable nameable = templateFor( Nameable.class );
        qb.where(
            eq( nameable.name(), "Gaming" )
        );
        final Query<Domain> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Gaming" );
    }

    @Test
    public void script03() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Query<Nameable> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults(
            query,
            "Joe Doe", "Ann Doe", "Jack Doe",
            "Penang", "Kuala Lumpur",
            "Cooking", "Gaming", "Programming", "Cars"
        );
    }

    @Test
    public void script04() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person personTemplate = templateFor( Person.class );
        City placeOfBirth = personTemplate.placeOfBirth().get();
        qb.where(
            eq( placeOfBirth.name(), "Kuala Lumpur" )
        );

        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script05() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            eq( person.mother().get().placeOfBirth().get().name(), "Kuala Lumpur" )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script06() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            ge( person.yearOfBirth(), 1973 )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script07() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Person person = templateFor( Person.class );
        qb.where(
            and(
                ge( person.yearOfBirth(), 1900 ),
                eq( person.placeOfBirth().get().name(), "Penang" )
            )
        );
        Query<Nameable> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script08() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    public void script09() throws EntityFinderException
    {
        QueryBuilder<Female> qb = qbf.newQueryBuilder( Female.class );
        Person person = templateFor( Person.class );
        qb.where(
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            )
        );
        Query<Female> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Ann Doe" );
    }

    @Test
    public void script10() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            not(
                eq( person.yearOfBirth(), 1975 )
            )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script11() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            isNotNull( person.email() )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script12() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            isNull( person.email() )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script13() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        qb.where(
            isNotNull( person.wife() )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script14() throws EntityFinderException
    {
        QueryBuilder<Male> qb = qbf.newQueryBuilder( Male.class );
        Male person = templateFor( Male.class );
        qb.where(
            isNull( person.wife() )
        );
        Query<Male> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script15() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        qb.where(
            isNull( person.wife() )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script16() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return only 2 entities
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery(unitOfWork);
        query.orderBy( orderBy( nameable.name() ) );
        query.maxResults( 2 );
        verifyOrderedResults(
            query,
            "Ann Doe", "Cars"
        );
    }

    @Test
    public void script17() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return only 3 entities starting with forth one
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery(unitOfWork);
        query.orderBy( orderBy( nameable.name() ) );
        query.firstResult( 3 );
        query.maxResults( 3 );
        verifyOrderedResults(
            query,
            "Gaming", "Jack Doe", "Joe Doe"
        );
    }

    @Test
    public void script18() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery(unitOfWork);
        query.orderBy( orderBy( nameable.name() ) );
        verifyOrderedResults(
            query,
            "Ann Doe", "Cars", "Cooking", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming"
        );
    }

    @Test
    public void script19() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return all Nameable entities with a name > "D" sorted by name
        Nameable nameable = templateFor( Nameable.class );
        qb.where(
            gt( nameable.name(), "D" )
        );
        Query<Nameable> query = qb.newQuery(unitOfWork);
        query.orderBy( orderBy( nameable.name() ) );
        verifyOrderedResults(
            query,
            "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming"
        );
    }

    @Test
    public void script20() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        qb.where(
            gt( person.yearOfBirth(), 1973 )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        query.orderBy( orderBy( person.name(), OrderBy.Order.DESCENDING ) );
        verifyOrderedResults(
            query,
            "Joe Doe", "Ann Doe"
        );
    }

    @Test
    public void script21() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        // should return all Persons sorted by name of the city they were born, and then by year they were born
        Person person = templateFor( Person.class );
        Query<Person> query = qb.newQuery(unitOfWork);
        query.orderBy( orderBy( person.placeOfBirth().get().name() ),
                       orderBy( person.yearOfBirth() ) );
        verifyOrderedResults(
            query,
            "Ann Doe", "Joe Doe", "Jack Doe"
        );
    }

    @Test
    public void script22()
        throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        qb.where(
            matches( nameable.name(), "J.*Doe" )
        );
        Query<Nameable> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults(
            query,
            "Jack Doe", "Joe Doe"
        );
    }

    @Ignore( "Skip this one for now. It sporadically fails sometimes." )
    @Test
    public void script23()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Domain interests = oneOf( person.interests() );
        qb.where( eq( interests.name(), "Cars" ) );

        Query<Person> query = qb.newQuery(unitOfWork);
        verifyOrderedResults( query, "Jack Doe" );
    }

    @Test
    public void script24() throws EntityFinderException
    {
        final QueryBuilder<Domain> qb = qbf.newQueryBuilder( Domain.class );
        final Nameable nameable = templateFor( Nameable.class );
        qb.where(
            eq( nameable.name(), "Gaming" )
        );
        final Query<Domain> query = qb.newQuery(unitOfWork);
        assertThat( query.find().name().get(), is( equalTo( "Gaming" ) ) );
    }

    @Test( expected = NotQueryableException.class )
    public void script25()
    {
        qbf.newQueryBuilder( File.class );
    }

    @Test( expected = NotQueryableException.class )
    public void script26()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            eq( person.personalWebsite().get().file().get().value(), "some/path" )
        );
    }

    @Test( expected = NotQueryableException.class )
    public void script27()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            eq( person.personalWebsite().get().host().get().value(), "www.qi4j.org" )
        );
    }

    @Test( expected = NotQueryableException.class )
    public void script28()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            eq( person.personalWebsite().get().port().get().value(), 8080 )
        );
    }

    @Test
    @Ignore("Wait until indexing of complex values is implemented")
    public void script29()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            eq( person.personalWebsite().get().protocol().get().value(), "http" )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    @Ignore( "Wait till 0.7.0?" )
    public void script30()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        QueryParam queryParam = null; //oneOf( person.personalWebsite().get().queryParams() );
        qb.where(
            and(
                eq( queryParam.name(), "foo" ),
                eq( queryParam.value(), "bar" )
            )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    @Ignore( "Wait till 0.7.0?" )
    public void script31()
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Map<String, String> info = new HashMap<String, String>();
        qb.where(
            eq( person.additionalInfo(), info )
        );
        Query<Person> query = qb.newQuery(unitOfWork);
        verifyUnorderedResults( query, "Jack Doe" );
    }

}