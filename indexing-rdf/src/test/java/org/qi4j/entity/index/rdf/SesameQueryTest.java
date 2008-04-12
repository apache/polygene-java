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
import static org.qi4j.query.QueryExpressions.*;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.spi.query.EntitySearcher;
import org.qi4j.spi.query.SearchException;

public class SesameQueryTest
{

    private static final BooleanExpression ALL = null;
    private static final OrderBy[] NO_SORTING = null;
    private static final Integer NO_FIRST_RESULT = null;
    private static final Integer NO_MAX_RESULTS = null;

    private SingletonAssembler assembler;
    private EntitySearcher searchEngine;

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
        searchEngine = assembler.getServiceLocator().lookupService( RDFQueryService.class ).get();
    }

    @Test
    public void showNetwork()
    {
        assembler.getServiceLocator().lookupService( RDFIndexerExporterComposite.class ).get().toRDF( System.out );
    }

    @Test
    public void script01() throws SearchException
    {
        // should return all persons (Joe, Ann, Jack Doe)
        searchEngine.find(
            PersonComposite.class,
            ALL,
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script02() throws SearchException
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Gaming domain
        searchEngine.find(
            Domain.class,
            eq( nameable.name(), "Gaming" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script03() throws SearchException
    {
        // should return all entities
        searchEngine.find(
            Nameable.class,
            ALL,
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script04() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        searchEngine.find(
            Person.class,
            eq( person.placeOfBirth().get().name(), "Kuala Lumpur" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script05() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        searchEngine.find(
            Person.class,
            eq( person.mother().get().placeOfBirth().get().name(), "Kuala Lumpur" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script06() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        searchEngine.find(
            Person.class,
            ge( person.yearOfBirth(), 1973 ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script07() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Jack Doe
        searchEngine.find(
            Nameable.class,
            and(
                ge( person.yearOfBirth(), 1900 ),
                eq( person.placeOfBirth().get().name(), "Penang" )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script08() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Jack and Ann Doe
        searchEngine.find(
            Person.class,
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script09() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Ann Doe
        searchEngine.find(
            Female.class,
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script10() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Jack Doe
        searchEngine.find(
            Person.class,
            not(
                eq( person.yearOfBirth(), 1975 )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script11() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        searchEngine.find(
            Person.class,
            isNotNull( person.email() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script12() throws SearchException
    {
        Person person = templateFor( Person.class );
        // should return Ann and Jack Doe
        searchEngine.find(
            Person.class,
            isNull( person.email() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script13() throws SearchException
    {
        Male person = templateFor( Male.class );
        // should return Jack Doe
        searchEngine.find(
            Person.class,
            isNotNull( person.wife() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script14() throws SearchException
    {
        Male person = templateFor( Male.class );
        // should return Jack Doe
        searchEngine.find(
            Male.class,
            isNull( person.wife() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script15() throws SearchException
    {
        Male person = templateFor( Male.class );
        // should return Ann and Jack Doe
        searchEngine.find(
            Person.class,
            isNull( person.wife() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script16() throws SearchException
    {
        // should return only 2 entities
        searchEngine.find(
            Nameable.class,
            ALL,
            NO_SORTING, NO_FIRST_RESULT, 2
        );
    }

    @Test
    public void script17() throws SearchException
    {
        // should return only 2 entities starting with third one
        searchEngine.find(
            Nameable.class,
            ALL,
            NO_SORTING, 3, 2
        );
    }

    @Test
    public void script18() throws SearchException
    {
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        searchEngine.find(
            Nameable.class,
            ALL,
            new OrderBy[]{ orderBy( nameable.name() ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script19() throws SearchException
    {
        // should return all Nameable entities with a name > "B" sorted by name
        Nameable nameable = templateFor( Nameable.class );
        searchEngine.find(
            Nameable.class,
            gt( nameable.name(), "B" ),
            new OrderBy[]{ orderBy( nameable.name() ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script20() throws SearchException
    {
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        searchEngine.find(
            Person.class,
            gt( person.yearOfBirth(), 1973 ),
            new OrderBy[]{ orderBy( person.name(), OrderBy.Order.DESCENDING ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script21() throws SearchException
    {
        // should return all Persons sorted name of the city they were born
        Person person = templateFor( Person.class );
        searchEngine.find(
            Person.class,
            ALL,
            new OrderBy[]{ orderBy( person.placeOfBirth().get().name() ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script22() throws SearchException
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        searchEngine.find(
            Nameable.class,
            matches( nameable.name(), "J.*Doe" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

}