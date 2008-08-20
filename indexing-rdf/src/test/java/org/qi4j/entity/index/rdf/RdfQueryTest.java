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
package org.qi4j.entity.index.rdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.index.rdf.model.City;
import org.qi4j.entity.index.rdf.model.Domain;
import org.qi4j.entity.index.rdf.model.Female;
import org.qi4j.entity.index.rdf.model.Male;
import org.qi4j.entity.index.rdf.model.Nameable;
import org.qi4j.entity.index.rdf.model.Person;
import org.qi4j.entity.index.rdf.model.entities.CatEntity;
import org.qi4j.entity.index.rdf.model.entities.CityEntity;
import org.qi4j.entity.index.rdf.model.entities.DomainEntity;
import org.qi4j.entity.index.rdf.model.entities.FemaleEntity;
import org.qi4j.entity.index.rdf.model.entities.MaleEntity;
import org.qi4j.entity.memory.IndexedMemoryEntityStoreService;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;
import static org.qi4j.query.QueryExpressions.and;
import static org.qi4j.query.QueryExpressions.oneOf;
import static org.qi4j.query.QueryExpressions.eq;
import static org.qi4j.query.QueryExpressions.ge;
import static org.qi4j.query.QueryExpressions.gt;
import static org.qi4j.query.QueryExpressions.isNotNull;
import static org.qi4j.query.QueryExpressions.isNull;
import static org.qi4j.query.QueryExpressions.matches;
import static org.qi4j.query.QueryExpressions.not;
import static org.qi4j.query.QueryExpressions.or;
import static org.qi4j.query.QueryExpressions.orderBy;
import static org.qi4j.query.QueryExpressions.templateFor;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.service.ServiceFinder;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.spi.query.EntityFinderException;

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
                    CatEntity.class
                );
                module.addServices(
                    MemoryRepositoryService.class,
                    IndexedMemoryEntityStoreService.class,
                    UuidIdentityGeneratorService.class,
                    RdfIndexerExporterComposite.class
                );
            }
        };
        Network.populate( assembler.unitOfWorkFactory().newUnitOfWork() );
        unitOfWork = assembler.unitOfWorkFactory().newUnitOfWork();
        qbf = unitOfWork.queryBuilderFactory();
    }

    @After
    public void tearDown()
    {
        unitOfWork.discard();
    }

    @Test
    public void showNetwork()
    {
        ServiceFinder serviceFinder = assembler.serviceFinder();
        RdfIndexerExporterComposite rdfIndexerExporter =
            serviceFinder.findService( RdfIndexerExporterComposite.class ).get();
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
    public void script01() throws EntityFinderException
    {
        final QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        final Query<Person> query = qb.newQuery();
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
        final Query<Domain> query = qb.newQuery();
        verifyUnorderedResults( query, "Gaming" );
    }

    @Test
    public void script03() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Query<Nameable> query = qb.newQuery();
        verifyUnorderedResults(
            query,
            "Joe Doe", "Ann Doe", "Jack Doe",
            "Penang", "Kuala Lumpur",
            "Cooking", "Gaming", "Programming", "Cars",
            "Felix"
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

        Query<Person> query = qb.newQuery();
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
        Query<Person> query = qb.newQuery();
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
        Query<Person> query = qb.newQuery();
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
        Query<Nameable> query = qb.newQuery();
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
        Query<Person> query = qb.newQuery();
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
        Query<Female> query = qb.newQuery();
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
        Query<Person> query = qb.newQuery();
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
        Query<Person> query = qb.newQuery();
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
        Query<Person> query = qb.newQuery();
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
        Query<Person> query = qb.newQuery();
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
        Query<Male> query = qb.newQuery();
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
        Query<Person> query = qb.newQuery();
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script16() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return only 2 entities
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery();
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
        Query<Nameable> query = qb.newQuery();
        query.orderBy( orderBy( nameable.name() ) );
        query.firstResult( 3 );
        query.maxResults( 3 );
        verifyOrderedResults(
            query,
            "Felix", "Gaming", "Jack Doe"
        );
    }

    @Test
    public void script18() throws EntityFinderException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery();
        query.orderBy( orderBy( nameable.name() ) );
        verifyOrderedResults(
            query,
            "Ann Doe", "Cars", "Cooking", "Felix", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang",
            "Programming"
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
        Query<Nameable> query = qb.newQuery();
        query.orderBy( orderBy( nameable.name() ) );
        verifyOrderedResults(
            query,
            "Felix", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming"
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
        Query<Person> query = qb.newQuery();
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
        Query<Person> query = qb.newQuery();
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
        Query<Nameable> query = qb.newQuery();
        verifyUnorderedResults(
            query,
            "Jack Doe", "Joe Doe"
        );
    }

    @Test
    public void script23()
        throws EntityFinderException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person personTmpl = templateFor( Person.class );
        Domain personInterestsTmpl = oneOf( personTmpl.interests() );
        qb.where( eq( personInterestsTmpl.name(), "Cars" ) );

        Query<Person> query = qb.newQuery();
        verifyOrderedResults( query, "Jack Doe" );
    }

}