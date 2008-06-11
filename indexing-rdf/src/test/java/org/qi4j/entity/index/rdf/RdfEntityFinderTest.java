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
import org.qi4j.entity.index.rdf.memory.MemoryRepositoryService;
import org.qi4j.entity.memory.IndexedMemoryEntityStoreService;
import static org.qi4j.query.QueryExpressions.and;
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
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;

public class RdfEntityFinderTest
{
    private static final BooleanExpression ALL = null;
    private static final OrderBy[] NO_SORTING = null;
    private static final Integer NO_FIRST_RESULT = null;
    private static final Integer NO_MAX_RESULTS = null;

    private SingletonAssembler assembler;
    private EntityFinder entityFinder;

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
                    RdfIndexerExporterComposite.class
                );
                module.addServices( MemoryRepositoryService.class ).identifiedBy( "rdf-indexing" );
//                module.addServices( NativeRdfRepositoryService.class ).identifiedBy( "rdf-indexing" );
//                module.addComposites( NativeRdfConfiguration.class );
            }
        };
        Network.populate( assembler.unitOfWorkFactory().newUnitOfWork() );
        entityFinder = assembler.serviceFinder().findService( RdfQueryService.class ).get();
    }

    @Test
    public void showNetwork()
    {
        assembler.serviceFinder().findService( RdfIndexerExporterComposite.class ).get().toRDF( System.out );
    }

    @Test
    public void script01() throws EntityFinderException
    {
        // should return all persons (Joe, Ann, Jack Doe)
        entityFinder.findEntities(
            PersonComposite.class,
            ALL,
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script02() throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Gaming domain
        entityFinder.findEntities(
            Domain.class,
            eq( nameable.name(), "Gaming" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script03() throws EntityFinderException
    {
        // should return all entities
        entityFinder.findEntities(
            Nameable.class,
            ALL,
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script04() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        entityFinder.findEntities(
            Person.class,
            eq( person.placeOfBirth().get().name(), "Kuala Lumpur" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script05() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        entityFinder.findEntities(
            Person.class,
            eq( person.mother().get().placeOfBirth().get().name(), "Kuala Lumpur" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script06() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        entityFinder.findEntities(
            Person.class,
            ge( person.yearOfBirth(), 1973 ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script07() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Jack Doe
        entityFinder.findEntities(
            Nameable.class,
            and(
                ge( person.yearOfBirth(), 1900 ),
                eq( person.placeOfBirth().get().name(), "Penang" )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script08() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Jack and Ann Doe
        entityFinder.findEntities(
            Person.class,
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script09() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Ann Doe
        entityFinder.findEntities(
            Female.class,
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script10() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Jack Doe
        entityFinder.findEntities(
            Person.class,
            not(
                eq( person.yearOfBirth(), 1975 )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script11() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        entityFinder.findEntities(
            Person.class,
            isNotNull( person.email() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script12() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Ann and Jack Doe
        entityFinder.findEntities(
            Person.class,
            isNull( person.email() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script13() throws EntityFinderException
    {
        Male person = templateFor( Male.class );
        // should return Jack Doe
        entityFinder.findEntities(
            Person.class,
            isNotNull( person.wife() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script14() throws EntityFinderException
    {
        Male person = templateFor( Male.class );
        // should return Joe Doe
        entityFinder.findEntities(
            Male.class,
            isNull( person.wife() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script15() throws EntityFinderException
    {
        Male person = templateFor( Male.class );
        // should return Ann and Joe Doe
        entityFinder.findEntities(
            Person.class,
            isNull( person.wife() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script16() throws EntityFinderException
    {
        // should return only 2 entities
        entityFinder.findEntities(
            Nameable.class,
            ALL,
            NO_SORTING, NO_FIRST_RESULT, 2
        );
    }

    @Test
    public void script17() throws EntityFinderException
    {
        // should return only 2 entities starting with third one
        entityFinder.findEntities(
            Nameable.class,
            ALL,
            NO_SORTING, 3, 2
        );
    }

    @Test
    public void script18() throws EntityFinderException
    {
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        entityFinder.findEntities(
            Nameable.class,
            ALL,
            new OrderBy[]{ orderBy( nameable.name() ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script19() throws EntityFinderException
    {
        // should return all Nameable entities with a name > "B" sorted by name
        Nameable nameable = templateFor( Nameable.class );
        entityFinder.findEntities(
            Nameable.class,
            gt( nameable.name(), "B" ),
            new OrderBy[]{ orderBy( nameable.name() ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script20() throws EntityFinderException
    {
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        entityFinder.findEntities(
            Person.class,
            gt( person.yearOfBirth(), 1973 ),
            new OrderBy[]{ orderBy( person.name(), OrderBy.Order.DESCENDING ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script21() throws EntityFinderException
    {
        // should return all Persons sorted name of the city they were born
        Person person = templateFor( Person.class );
        entityFinder.findEntities(
            Person.class,
            ALL,
            new OrderBy[]{ orderBy( person.placeOfBirth().get().name() ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

    @Test
    public void script22() throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        entityFinder.findEntities(
            Nameable.class,
            matches( nameable.name(), "J.*Doe" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
    }

}