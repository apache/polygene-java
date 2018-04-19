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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.query.grammar.OrderBy;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.spi.query.EntityFinder;
import org.apache.polygene.spi.query.IndexExporter;
import org.apache.polygene.test.model.Domain;
import org.apache.polygene.test.model.Female;
import org.apache.polygene.test.model.Male;
import org.apache.polygene.test.model.Nameable;
import org.apache.polygene.test.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.apache.polygene.api.query.QueryExpressions.and;
import static org.apache.polygene.api.query.QueryExpressions.eq;
import static org.apache.polygene.api.query.QueryExpressions.ge;
import static org.apache.polygene.api.query.QueryExpressions.gt;
import static org.apache.polygene.api.query.QueryExpressions.isNotNull;
import static org.apache.polygene.api.query.QueryExpressions.isNull;
import static org.apache.polygene.api.query.QueryExpressions.matches;
import static org.apache.polygene.api.query.QueryExpressions.not;
import static org.apache.polygene.api.query.QueryExpressions.or;
import static org.apache.polygene.api.query.QueryExpressions.orderBy;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;
import static org.apache.polygene.api.query.QueryExpressions.variable;
import static org.apache.polygene.test.indexing.NameableAssert.assertNames;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Abstract satisfiedBy with tests for the EntityFinder interface.
 */
public abstract class AbstractEntityFinderTest extends AbstractAnyQueryTest
{

    private static final Predicate<Composite> ALL = null;

    private static final OrderBy[] NO_SORTING = null;
    private static final List<OrderBy> NO_SORTING2 = null;

    private static final Integer NO_FIRST_RESULT = null;

    private static final Integer NO_MAX_RESULTS = null;

    private static final Map<String, Object> NO_VARIABLES = Collections.emptyMap();

    private static final String JACK = "Jack Doe";

    private static final String JOE = "Joe Doe";

    private static final String ANN = "Ann Doe";

    @Structure
    Module moduleInstance;

    private EntityFinder entityFinder;

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        entityFinder = this.moduleInstance.findService( EntityFinder.class ).get();
    }

    @Test
    public void showNetwork()
        throws IOException
    {
        final ServiceReference<IndexExporter> indexerService = this.moduleInstance.findService( IndexExporter.class );
        final IndexExporter exporter = indexerService.get();
        exporter.exportReadableToStream( System.out );
        // todo asserts
    }

    @Test
    public void script01()
    {
        // should return all persons (Joe, Ann, Jack Doe)
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            ALL,
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JOE, JACK, ANN );
    }

    @Test
    public void script02()
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Gaming domain
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Domain.class,
            eq( nameable.name(), "Gaming" ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, "Gaming" );
    }

    @Test
    public void script03()
    {
        // should return all entities
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Nameable.class,
            ALL,
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, NameableAssert.allNames() );
    }

    @Test
    public void script04()
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            eq( person.placeOfBirth().get().name(), "Kuala Lumpur" ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JOE, ANN );
    }

    @Test
    public void script05()
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            eq( person.mother().get().placeOfBirth().get().name(), "Kuala Lumpur" ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JOE );
    }

    @Test
    public void script06()
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            ge( person.yearOfBirth(), 1973 ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JOE, ANN );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void script07()
    {
        Person person = templateFor( Person.class );
        // should return Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Nameable.class,
            and( ge( person.yearOfBirth(), 1900 ), eq( person.placeOfBirth().get().name(), "Penang" ) ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JACK );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void script08()
    {
        Person person = templateFor( Person.class );
        // should return Jack and Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            or( eq( person.yearOfBirth(), 1970 ), eq( person.yearOfBirth(), 1975 ) ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JACK, ANN );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void script09()
    {
        Person person = templateFor( Person.class );
        // should return Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Female.class,
            or( eq( person.yearOfBirth(), 1970 ), eq( person.yearOfBirth(), 1975 ) ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, ANN );
    }

    @Test
    public void script10()
    {
        Person person = templateFor( Person.class );
        // should return Joe and Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            not( eq( person.yearOfBirth(), 1975 ) ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JOE, JACK );
    }

    @Test
    public void script11()
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            isNotNull( person.email() ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JOE );
    }

    @Test
    public void script12()
    {
        Person person = templateFor( Person.class );
        // should return Ann and Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            isNull( person.email() ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, ANN, JACK );
    }

    @Test
    public void script13()
    {
        Male person = templateFor( Male.class );
        // should return Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            isNotNull( person.wife() ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JACK );
    }

    @Test
    public void script14()
    {
        Male person = templateFor( Male.class );
        // should return Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Male.class,
            isNull( person.wife() ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JOE );
    }

    @Test
    public void script15()
    {
        Male person = templateFor( Male.class );
        // should return Ann and Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            isNull( person.wife() ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, ANN, JOE );
    }

    @Test
    public void script16()
    {
        // should return only 2 entities
        Stream<EntityReference> references = entityFinder.findEntities(
            Nameable.class,
            ALL,
            NO_SORTING2,
            NO_FIRST_RESULT, 2,
            NO_VARIABLES );
        assertThat( "2 identities", references.count(), equalTo( 2L ) );
    }

    @Test
    public void script17()
    {
        // should return only 2 entities starting with third one
        Stream<EntityReference> references = entityFinder.findEntities(
            Nameable.class,
            ALL,
            NO_SORTING2,
            3, 2,
            NO_VARIABLES );
        assertThat( "2 identitities", references.count(), equalTo( 2L ) );
    }

    @Test
    public void script18()
    {
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        final String[] allNames = NameableAssert.allNames();
        Arrays.sort( allNames );

        Iterable<EntityReference> entities = entityFinder.findEntities(
            Nameable.class,
            ALL,
            Collections.singletonList( orderBy( nameable.name() ) ),
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( false, entities, allNames );
    }

    @Test
    public void script19()
    {
        // should return all Nameable entities with a name > "B" sorted by name
        Nameable nameable = templateFor( Nameable.class );
        List<String> largerThanB = new ArrayList<>();
        for( String name : NameableAssert.allNames() )
        {
            if( name.compareTo( "B" ) > 0 )
            {
                largerThanB.add( name );
            }
        }
        Collections.sort( largerThanB );
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Nameable.class,
            gt( nameable.name(), "B" ),
            Collections.singletonList( orderBy( nameable.name() ) ),
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( false, entities, largerThanB.toArray( new String[ largerThanB.size() ] ) );
    }

    @Test
    public void script20()
    {
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            gt( person.yearOfBirth(), 1973 ),
            Collections.singletonList( orderBy( person.name(), OrderBy.Order.DESCENDING ) ),
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( false, entities, JOE, ANN );
    }

    @Test
    public void script21()
    {
        // should return all Persons sorted name of the city they were born
        Person person = templateFor( Person.class );
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Person.class,
            ALL,
            Arrays.asList( orderBy( person.placeOfBirth().get().name() ), orderBy( person.name() ) ),
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( false, entities, ANN, JOE, JACK );
    }

    @Test
    public void script22()
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Nameable.class,
            matches( nameable.name(), "J.*Doe" ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            NO_VARIABLES ).collect( toList() );
        assertNames( entities, JACK, JOE );
    }

    @Test
    public void script23()
    {
        Nameable nameable = templateFor( Nameable.class );
        // Try using variables
        Map<String, Object> variables = new HashMap<>( 1 );
        variables.put( "domain", "Gaming" );
        Iterable<EntityReference> entities = entityFinder.findEntities(
            Domain.class,
            eq( nameable.name(), variable( "domain" ) ),
            NO_SORTING2,
            NO_FIRST_RESULT, NO_MAX_RESULTS,
            variables ).collect( toList() );
        assertNames( entities, "Gaming" );
    }
}
