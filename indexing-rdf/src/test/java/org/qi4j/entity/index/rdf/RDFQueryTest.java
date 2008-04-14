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

import org.junit.Before;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.memory.IndexedMemoryEntityStoreService;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;
import static org.qi4j.query.QueryExpressions.*;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.spi.query.SearchException;

public class RDFQueryTest
{

    private SingletonAssembler assembler;
    private QueryBuilderFactory qbf;

    @Before
    public void setUp() throws UnitOfWorkCompletionException
    {
        assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposites(
                    MaleComposite.class,
                    FemaleComposite.class,
                    CityComposite.class,
                    DomainComposite.class,
                    CatComposite.class
                );
                module.addServices(
                    IndexedMemoryEntityStoreService.class,
                    UuidIdentityGeneratorService.class,
                    RDFIndexerExporterComposite.class
                );
            }
        };
        Network.populate( assembler.getUnitOfWorkFactory().newUnitOfWork() );
        qbf = assembler.getUnitOfWorkFactory().newUnitOfWork().queryBuilderFactory();
    }

    @Test
    public void showNetwork()
    {
        assembler.getServiceLocator().lookupService( RDFIndexerExporterComposite.class ).get().toRDF( System.out );
    }

    @Test
    public void script01() throws SearchException
    {
        QueryBuilder<PersonComposite> qb = qbf.newQueryBuilder( PersonComposite.class );
        // should return all persons (Joe, Ann, Jack Doe)
        Query<PersonComposite> query = qb.newQuery();
        for( PersonComposite entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script02() throws SearchException
    {
        QueryBuilder<Domain> qb = qbf.newQueryBuilder( Domain.class );
        Nameable nameable = templateFor( Nameable.class );
        qb.where(
            eq( nameable.name(), "Gaming" )
        );
        // should return Gaming domain
        Query<Domain> query = qb.newQuery();
        for( Domain entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script03() throws SearchException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return all entities
        Query<Nameable> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script04() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        qb.where(
            eq( person.placeOfBirth().get().name(), "Kuala Lumpur" )
        );
        Query<Person> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script05() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        // should return Joe Doe
        qb.where(
            eq( person.mother().get().placeOfBirth().get().name(), "Kuala Lumpur" )
        );
        Query<Person> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script06() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        qb.where(
            ge( person.yearOfBirth(), 1973 )
        );
        Query<Person> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script07() throws SearchException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Person person = templateFor( Person.class );
        // should return Jack Doe
        qb.where(
            and(
                ge( person.yearOfBirth(), 1900 ),
                eq( person.placeOfBirth().get().name(), "Penang" )
            )
        );
        Query<Nameable> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script08() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        // should return Jack and Ann Doe
        qb.where(
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            )
        );
        Query<Person> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script09() throws SearchException
    {
        QueryBuilder<Female> qb = qbf.newQueryBuilder( Female.class );
        Person person = templateFor( Person.class );
        // should return Ann Doe
        qb.where(
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            )
        );
        Query<Female> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script10() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        // should return Joe and Jack Doe
        qb.where(
            not(
                eq( person.yearOfBirth(), 1975 )
            )
        );
        Query<Person> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script11() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        // should return Joe Doe
        qb.where(
            isNotNull( person.email() )
        );
        Query<Person> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script12() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Person person = templateFor( Person.class );
        // should return Ann and Jack Doe
        qb.where(
            isNull( person.email() )
        );
        Query<Person> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script13() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        // should return Jack Doe
        qb.where(
            isNotNull( person.wife() )
        );
        Query<Person> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script14() throws SearchException
    {
        QueryBuilder<Male> qb = qbf.newQueryBuilder( Male.class );
        Male person = templateFor( Male.class );
        // should return Joe Doe
        qb.where(
            isNull( person.wife() )
        );
        Query<Male> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script15() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        Male person = templateFor( Male.class );
        // should return Ann and Joe Doe
        qb.where(
            isNull( person.wife() )
        );
        Query<Person> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script16() throws SearchException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return only 2 entities
        Query<Nameable> query = qb.newQuery();
        query.maxResults( 2 );
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script17() throws SearchException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return only 3 entities starting with third one
        Query<Nameable> query = qb.newQuery();
        query.firstResult( 3 );
        query.maxResults( 3 );
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script18() throws SearchException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        Query<Nameable> query = qb.newQuery();
        query.orderBy( orderBy( nameable.name() ) );
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script19() throws SearchException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        // should return all Nameable entities with a name > "B" sorted by name
        Nameable nameable = templateFor( Nameable.class );
        qb.where(
            gt( nameable.name(), "B" )
        );
        Query<Nameable> query = qb.newQuery();
        query.orderBy( orderBy( nameable.name() ) );
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script20() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        qb.where(
            gt( person.yearOfBirth(), 1973 )
        );
        Query<Person> query = qb.newQuery();
        query.orderBy( orderBy( person.name(), OrderBy.Order.DESCENDING ) );
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

    @Test
    public void script21() throws SearchException
    {
        QueryBuilder<Person> qb = qbf.newQueryBuilder( Person.class );
        // should return all Persons sorted by name of the city they were born
        Person person = templateFor( Person.class );
        Query<Person> query = qb.newQuery();
        query.orderBy( orderBy( person.placeOfBirth().get().name() ) );
        for( Person entity : query )
        {
            System.out.println( "Result: " + entity.placeOfBirth().get().name() );
        }
    }

    @Test
    public void script22() throws SearchException
    {
        QueryBuilder<Nameable> qb = qbf.newQueryBuilder( Nameable.class );
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        qb.where(
            matches( nameable.name(), "J.*Doe" )
        );
        Query<Nameable> query = qb.newQuery();
        for( Nameable entity : query )
        {
            System.out.println( "Result: " + entity.name() );
        }
    }

}