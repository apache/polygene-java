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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.entity.EntityReference;
import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.ge;
import static org.qi4j.api.query.QueryExpressions.gt;
import static org.qi4j.api.query.QueryExpressions.isNotNull;
import static org.qi4j.api.query.QueryExpressions.isNull;
import static org.qi4j.api.query.QueryExpressions.matches;
import static org.qi4j.api.query.QueryExpressions.not;
import static org.qi4j.api.query.QueryExpressions.or;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import static org.qi4j.index.rdf.NameableAssert.assertNames;
import static org.qi4j.index.rdf.NameableAssert.toList;
import org.qi4j.index.rdf.assembly.RdfFactoryService;
import org.qi4j.index.rdf.assembly.RdfQueryService;
import org.qi4j.index.rdf.model.Domain;
import org.qi4j.index.rdf.model.Female;
import org.qi4j.index.rdf.model.File;
import org.qi4j.index.rdf.model.Host;
import org.qi4j.index.rdf.model.Male;
import org.qi4j.index.rdf.model.Nameable;
import org.qi4j.index.rdf.model.Person;
import org.qi4j.index.rdf.model.Port;
import org.qi4j.index.rdf.model.Protocol;
import org.qi4j.index.rdf.model.QueryParam;
import org.qi4j.index.rdf.model.URL;
import org.qi4j.index.rdf.model.Address;
import org.qi4j.index.rdf.model.entities.AccountEntity;
import org.qi4j.index.rdf.model.entities.CatEntity;
import org.qi4j.index.rdf.model.entities.CityEntity;
import org.qi4j.index.rdf.model.entities.DomainEntity;
import org.qi4j.index.rdf.model.entities.FemaleEntity;
import org.qi4j.index.rdf.model.entities.MaleEntity;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.test.EntityTestAssembler;
import org.openrdf.rio.RDFFormat;

public class RdfEntityFinderTest
{
    private static final BooleanExpression ALL = null;
    private static final OrderBy[] NO_SORTING = null;
    private static final Integer NO_FIRST_RESULT = null;
    private static final Integer NO_MAX_RESULTS = null;

    private SingletonAssembler assembler;
    private EntityFinder entityFinder;
    private static final String JACK = "Jack Doe";
    private static final String JOE = "Joe Doe";
    private static final String ANN = "Ann Doe";

    @Before
    public void setUp() throws UnitOfWorkCompletionException
    {
        assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addObjects( EntityStateSerializer.class, EntityTypeSerializer.class );
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
                    Address.class,
                    Protocol.class,
                    Host.class,
                    Port.class,
                    File.class,
                    QueryParam.class
                );
                new EntityTestAssembler().assemble( module );
                module.addServices(
                    RdfFactoryService.class,
                    RdfIndexerExporterComposite.class
                );
                module.addServices( MemoryRepositoryService.class ).identifiedBy( "rdf-indexing" );
//                module.addServices( NativeRdfRepositoryService.class ).identifiedBy( "rdf-indexing" );
//                module.addComposites( NativeRdfConfiguration.class );
            }
        };
        Network.populate( assembler );
        entityFinder = assembler.serviceFinder().<RdfQueryService>findService( RdfQueryService.class ).get();
    }

    @Test
    public void showNetwork() throws IOException
    {
        final ServiceReference<RdfIndexerExporterComposite> indexerService = assembler.serviceFinder().findService( RdfIndexerExporterComposite.class );
        final RdfIndexerExporterComposite exporter = indexerService.get();
        exporter.toRDF( System.out, RDFFormat.RDFXML );
        // todo asserts
    }

    @Test
    public void script01() throws EntityFinderException
    {
        // should return all persons (Joe, Ann, Jack Doe)
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            ALL,
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JOE, JACK, ANN );
    }

    @Test
    public void script02() throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Gaming domain
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Domain.class.getName(),
            eq( nameable.name(), "Gaming" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, "Gaming" );
    }

    @Test
    public void script03() throws EntityFinderException
    {
        // should return all entities
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Nameable.class.getName(),
            ALL,
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, NameableAssert.allNames() );
    }

    @Test
    public void script04() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            eq( person.placeOfBirth().get().name(), "Kuala Lumpur" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JOE, ANN );
    }

    @Test
    public void script05() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            eq( person.mother().get().placeOfBirth().get().name(), "Kuala Lumpur" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JOE );
    }

    @Test
    public void script06() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            ge( person.yearOfBirth(), 1973 ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JOE, ANN );
    }

    @Test
    public void script07() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Nameable.class.getName(),
            and(
                ge( person.yearOfBirth(), 1900 ),
                eq( person.placeOfBirth().get().name(), "Penang" )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JACK );
    }

    @Test
    public void script08() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Jack and Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JACK, ANN );
    }

    @Test
    public void script09() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Female.class.getName(),
            or(
                eq( person.yearOfBirth(), 1970 ),
                eq( person.yearOfBirth(), 1975 )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, ANN );
    }

    @Test
    public void script10() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            not(
                eq( person.yearOfBirth(), 1975 )
            ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JOE, JACK );
    }

    @Test
    public void script11() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            isNotNull( person.email() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JOE );
    }

    @Test
    public void script12() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Ann and Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            isNull( person.email() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, ANN, JACK );
    }

    @Test
    public void script13() throws EntityFinderException
    {
        Male person = templateFor( Male.class );
        // should return Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            isNotNull( person.wife() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JACK );
    }

    @Test
    public void script14() throws EntityFinderException
    {
        Male person = templateFor( Male.class );
        // should return Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Male.class.getName(),
            isNull( person.wife() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JOE );
    }

    @Test
    public void script15() throws EntityFinderException
    {
        Male person = templateFor( Male.class );
        // should return Ann and Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            isNull( person.wife() ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, ANN, JOE );
    }

    @Test
    public void script16()
        throws EntityFinderException
    {
        // should return only 2 entities
        final List<EntityReference> references = toList( entityFinder.findEntities(
            Nameable.class.getName(),
            ALL,
            NO_SORTING, NO_FIRST_RESULT, 2
        ) );
        assertEquals( "2 identitities", 2, references.size() );
    }

    @Test
    public void script17() throws EntityFinderException
    {
        // should return only 2 entities starting with third one
        final List<EntityReference> references = toList( entityFinder.findEntities(
            Nameable.class.getName(),
            ALL,
            NO_SORTING, 3, 2
        ) );
        assertEquals( "2 identitities", 2, references.size() );
    }

    @Test
    public void script18() throws EntityFinderException
    {
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        final String[] allNames = NameableAssert.allNames();
        Arrays.sort( allNames );

        Iterable<EntityReference> entities = entityFinder.findEntities(
            Nameable.class.getName(),
            ALL,
            new OrderBy[]{ orderBy( nameable.name() ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( false, entities, allNames );
    }

    @Test
    public void script19() throws EntityFinderException
    {
        // should return all Nameable entities with a name > "B" sorted by name
        Nameable nameable = templateFor( Nameable.class );
        List<String> largerThanB = new ArrayList<String>();
        for( String name : NameableAssert.allNames() )
        {
            if( name.compareTo( "B" ) > 0 )
            {
                largerThanB.add( name );
            }
        }
        Collections.sort( largerThanB );
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Nameable.class.getName(),
            gt( nameable.name(), "B" ),
            new OrderBy[]{ orderBy( nameable.name() ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( false, entities, largerThanB.toArray( new String[largerThanB.size()] ) );
    }

    @Test
    public void script20() throws EntityFinderException
    {
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            gt( person.yearOfBirth(), 1973 ),
            new OrderBy[]{ orderBy( person.name(), OrderBy.Order.DESCENDING ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( false, entities, JOE, ANN );
    }

    @Test
    public void script21() throws EntityFinderException
    {
        // should return all Persons sorted name of the city they were born
        Person person = templateFor( Person.class );
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class.getName(),
            ALL,
            new OrderBy[]{ orderBy( person.placeOfBirth().get().name() ),
                           orderBy( person.name() ) },
            NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( false, entities, ANN, JOE, JACK );
    }

    @Test
    public void script22() throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Nameable.class.getName(),
            matches( nameable.name(), "J.*Doe" ),
            NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS
        );
        assertNames( entities, JACK, JOE );
    }

}