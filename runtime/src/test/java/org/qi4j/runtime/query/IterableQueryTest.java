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
package org.qi4j.runtime.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.ge;
import static org.qi4j.api.query.QueryExpressions.gt;
import static org.qi4j.api.query.QueryExpressions.isNotNull;
import static org.qi4j.api.query.QueryExpressions.isNull;
import static org.qi4j.api.query.QueryExpressions.lt;
import static org.qi4j.api.query.QueryExpressions.matches;
import static org.qi4j.api.query.QueryExpressions.not;
import static org.qi4j.api.query.QueryExpressions.oneOf;
import static org.qi4j.api.query.QueryExpressions.or;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.runtime.query.model.City;
import org.qi4j.runtime.query.model.Domain;
import org.qi4j.runtime.query.model.Female;
import org.qi4j.runtime.query.model.Male;
import org.qi4j.runtime.query.model.Nameable;
import org.qi4j.runtime.query.model.Person;
import org.qi4j.runtime.query.model.entities.CityEntity;
import org.qi4j.runtime.query.model.entities.DomainEntity;
import org.qi4j.runtime.query.model.entities.FemaleEntity;
import org.qi4j.runtime.query.model.entities.MaleEntity;
import org.qi4j.spi.entity.helpers.UuidIdentityGeneratorService;
import org.qi4j.spi.entity.helpers.EntityTypeRegistryService;
import org.qi4j.spi.query.EntityFinderException;

public class IterableQueryTest
{

    private UnitOfWork uow;
    private QueryBuilderFactory qbf;

    @Before
    public void setUp() throws UnitOfWorkCompletionException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addEntities(
                    MaleEntity.class,
                    FemaleEntity.class,
                    CityEntity.class,
                    DomainEntity.class
                );
                module.addServices(
                    MemoryEntityStoreService.class,
                    UuidIdentityGeneratorService.class,
                    EntityTypeRegistryService.class

                );
            }
        };
        uow = assembler.unitOfWorkFactory().newUnitOfWork();
        Network.populate( uow );
        uow.apply();
        qbf = uow.queryBuilderFactory();
    }

    @After
    public void tearDown()
    {
        uow.discard();
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

        for( Nameable entity : results )
        {
            String firstExpected = null;
            if( expected.size() > 0 )
            {
                firstExpected = expected.get( 0 );
            }
            if( firstExpected == null )
            {
                fail( entity.name().get() + " returned but not expected" );
            }
            else if( !firstExpected.equals( entity.name().get() ) )
            {
                fail( entity.name().get() + " is not in the expected order" );
            }
            expected.remove( 0 );
        }
        for( String notReturned : expected )
        {
            fail( notReturned + " was expected but not returned" );
        }

    }

    @Test
    public void givenQueryWhenExecutedReturnAll() throws EntityFinderException
    {
        final QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        final Query<Person> query = qb.newQuery( Network.persons() );
        System.out.println( query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe" );
    }

    @Test
    public void givenEqQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        final QueryBuilder<Domain> qb = qbf.newQueryBuilder( Domain.class );
        final Nameable nameable = templateFor( Nameable.class );
        qb.where(
            eq( nameable.name(), "Gaming" )
        );
        final Query<Domain> query = qb.newQuery( Network.domains() );
        verifyUnorderedResults( query, "Gaming" );
    }

    @Test
    public void givenMixinTypeQueryWhenExecutedReturnAll() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        verifyUnorderedResults(
            query,
            "Joe Doe", "Ann Doe", "Jack Doe",
            "Penang", "Kuala Lumpur",
            "Cooking", "Gaming", "Programming", "Cars"
        );
    }

    @Test
    public void givenEqQueryOnValueWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person personTemplate = templateFor( Person.class );
        City placeOfBirth = personTemplate.placeOfBirth().get();
        qb.where(
            eq( placeOfBirth.name(), "Kuala Lumpur" )
        );

        Query<Person> query = qb.newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void givenEqQueryOnAssociationWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            eq( person.mother().get().placeOfBirth().get().name(), "Kuala Lumpur" )
        );
        Query<Person> query = qb.newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void givenGeQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            ge( person.yearOfBirth(), 1973 )
        );
        Query<Person> query = qb.newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void givenAndQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Person person = templateFor( Person.class );
        qb.where(
            and(
                ge( person.yearOfBirth(), 1900 ),
                eq( person.placeOfBirth().get().name(), "Penang" )
            )
        );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void givenMultipleAndQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Person person = templateFor( Person.class );
        qb.where(
            and(
                ge( person.yearOfBirth(), 1900 ),
                lt( person.yearOfBirth(), 2000 ),
                eq( person.placeOfBirth().get().name(), "Penang" )
            )
        );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void givenOrQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            )
        );
        Query<Person> query = qb.newQuery( Network.persons() );
        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    public void givenMultipleOrQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 ),
                eq( person.yearOfBirth(), 1990 )
            )
        );
        Query<Person> query = qb.newQuery( Network.persons() );
        verifyUnorderedResults( query, "Jack Doe", "Ann Doe", "Joe Doe" );
    }

    @Test
    public void givenOrQueryOnFemalesWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Female> qb = qbf.newQueryBuilder( Female.class );
        Person person = templateFor( Person.class );
        qb.where(
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            )
        );
        Query<Female> query = qb.newQuery( Network.females() );
        verifyUnorderedResults( query, "Ann Doe" );
    }

    @Test
    public void givenNotQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            not(
                eq( person.yearOfBirth(), 1975 )
            )
        );
        Query<Person> query = qb.newQuery( Network.persons() );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void givenIsNotNullQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            isNotNull( person.email() )
        );
        Query<Person> query = qb.newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void givenIsNullQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        qb.where(
            isNull( person.email() )
        );
        Query<Person> query = qb.newQuery( Network.persons() );
        verifyUnorderedResults( query, "Ann Doe", "Jack Doe" );
    }

    @Test
    public void givenIsNotNullOnMixinTypeWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        qb.where(
            isNotNull( person.wife() )
        );
        Query<Person> query = qb.newQuery( Network.persons() );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void givenIsNullOnMixinTypeWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Male> qb = qbf.newQueryBuilder( Male.class );
        Male person = templateFor( Male.class );
        qb.where(
            isNull( person.wife() )
        );
        Query<Male> query = qb.newQuery( Network.males() );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void givenIsNullOnAssociationWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        qb.where(
            isNull( person.wife() )
        );
        Query<Person> query = qb.newQuery( Network.persons() );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void givenOrderAndMaxResultsQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return only 2 entities
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        query.orderBy( orderBy( nameable.name() ) );
        query.maxResults( 2 );
        verifyOrderedResults(
            query,
            "Ann Doe", "Cars"
        );
    }

    @Test
    public void givenOrderAndFirstAndMaxResultsQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return only 3 entities starting with forth one
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        query.orderBy( orderBy( nameable.name() ) );
        query.firstResult( 3 );
        query.maxResults( 3 );
        verifyOrderedResults(
            query,
            "Gaming", "Jack Doe", "Joe Doe"
        );
    }

    @Test
    public void givenOrderByOnMixinTypeQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        query.orderBy( orderBy( nameable.name() ) );
        verifyOrderedResults(
            query,
            "Ann Doe", "Cars", "Cooking", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming"
        );
    }

    @Test
    public void givenGtAndOrderByQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return all Nameable entities with a name > "D" sorted by name
        Nameable nameable = templateFor( Nameable.class );
        qb.where(
            gt( nameable.name(), "D" )
        );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        query.orderBy( orderBy( nameable.name() ) );
        verifyOrderedResults(
            query,
            "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming"
        );
    }

    @Test
    public void givenGtAndOrderByDescendingQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        qb.where(
            gt( person.yearOfBirth(), 1973 )
        );
        Query<Person> query = qb.newQuery( Network.persons() );
        query.orderBy( orderBy( person.name(), OrderBy.Order.DESCENDING ) );
        verifyOrderedResults(
            query,
            "Joe Doe", "Ann Doe"
        );
    }

    @Test
    public void givenOrderByMultipleQueryWhenExecutedThenReturnCorrect() throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        // should return all Persons sorted by name of the city they were born, and then by year they were born
        Person person = templateFor( Person.class );
        Query<Person> query = qb.newQuery( Network.persons() );
        query.orderBy( orderBy( person.placeOfBirth().get().name() ),
                       orderBy( person.yearOfBirth() ) );
        verifyOrderedResults(
            query,
            "Ann Doe", "Joe Doe", "Jack Doe"
        );
    }

    @Test
    public void givenMatchesQueryWhenExecutedThenReturnCorrect()
        throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        qb.where(
            matches( nameable.name(), "J.*Doe" )
        );
        Query<Nameable> query = qb.newQuery( Network.nameables() );
        verifyUnorderedResults(
            query,
            "Jack Doe", "Joe Doe"
        );
    }

    // TODO solve ManyAssociation filtering for iterables
    //@Test
    public void givenOneOfQueryWhenExecutedThenReturnCorrect()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        Domain interests = oneOf( person.interests() );
        qb.where( eq( interests.name(), "Cars" ) );

        Query<Person> query = qb.newQuery( Network.persons() );
        verifyOrderedResults( query, "Jack Doe" );
    }

}