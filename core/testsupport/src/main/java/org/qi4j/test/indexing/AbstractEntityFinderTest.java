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
package org.qi4j.test.indexing;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Specification;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.IndexExporter;
import org.qi4j.test.indexing.model.*;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.qi4j.api.query.QueryExpressions.*;
import static org.qi4j.test.indexing.NameableAssert.assertNames;
import static org.qi4j.test.indexing.NameableAssert.toList;

public abstract class AbstractEntityFinderTest
    extends AbstractAnyQueryTest
{
    private static final Specification<Composite> ALL = null;
    private static final OrderBy[] NO_SORTING = null;
    private static final Integer NO_FIRST_RESULT = null;
    private static final Integer NO_MAX_RESULTS = null;

    private EntityFinder entityFinder;
    private static final String JACK = "Jack Doe";
    private static final String JOE = "Joe Doe";
    private static final String ANN = "Ann Doe";

    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();
        entityFinder = this.module.findService( EntityFinder.class ).get();
    }

    @Test
    public void showNetwork()
        throws IOException
    {
        final ServiceReference<IndexExporter> indexerService = this.module.findService( IndexExporter.class );
        final IndexExporter exporter = indexerService.get();
        exporter.exportReadableToStream( System.out );
        // todo asserts
    }

    @Test
    public void script01()
        throws EntityFinderException
    {
        // should return all persons (Joe, Ann, Jack Doe)
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class, ALL, NO_SORTING, NO_FIRST_RESULT,
                                                                        NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JOE, JACK, ANN );
    }

    @Test
    public void script02()
        throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Gaming domain
        Iterable<EntityReference> entities = entityFinder.findEntities( Domain.class, QueryExpressions.eq( nameable.name(), "Gaming" ),
                                                                        NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, "Gaming" );
    }

    @Test
    public void script03()
        throws EntityFinderException
    {
        // should return all entities
        Iterable<EntityReference> entities = entityFinder.findEntities( Nameable.class, ALL, NO_SORTING,
                                                                        NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, NameableAssert.allNames() );
    }

    @Test
    public void script04()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class,
                                                                        eq( person.placeOfBirth()
                                                                                .get()
                                                                                .name(), "Kuala Lumpur" ), NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JOE, ANN );
    }

    @Test
    public void script05()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class,
                                                                        eq( person.mother()
                                                                                .get()
                                                                                .placeOfBirth()
                                                                                .get()
                                                                                .name(), "Kuala Lumpur" ), NO_SORTING, NO_FIRST_RESULT,
                                                                        NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JOE );
    }

    @Test
    public void script06()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class, ge( person.yearOfBirth(), 1973 ),
                                                                        NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JOE, ANN );
    }

    @Test
    public void script07()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Nameable.class,
                                                                        and( ge( person.yearOfBirth(), 1900 ), eq( person
                                                                                                                       .placeOfBirth()
                                                                                                                       .get()
                                                                                                                       .name(), "Penang" ) ), NO_SORTING,
                                                                        NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JACK );
    }

    @Test
    public void script08()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Jack and Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class,
                                                                        or( eq( person.yearOfBirth(), 1970 ), eq( person
                                                                                                                      .yearOfBirth(), 1975 ) ), NO_SORTING, NO_FIRST_RESULT,
                                                                        NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JACK, ANN );
    }

    @Test
    public void script09()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Ann Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Female.class,
                                                                        or( eq( person.yearOfBirth(), 1970 ), eq( person
                                                                                                                      .yearOfBirth(), 1975 ) ), NO_SORTING, NO_FIRST_RESULT,
                                                                        NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, ANN );
    }

    @Test
    public void script10()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe and Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class,
                                                                        not( eq( person.yearOfBirth(), 1975 ) ), NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JOE, JACK );
    }

    @Test
    public void script11()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class, isNotNull( person.email() ),
                                                                        NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JOE );
    }

    @Test
    public void script12()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        // should return Ann and Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class, isNull( person.email() ),
                                                                        NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, ANN, JACK );
    }

    @Test
    public void script13()
        throws EntityFinderException
    {
        Male person = templateFor( Male.class );
        // should return Jack Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class, isNotNull( person.wife() ),
                                                                        NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JACK );
    }

    @Test
    public void script14()
        throws EntityFinderException
    {
        Male person = templateFor( Male.class );
        // should return Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Male.class, isNull( person.wife() ),
                                                                        NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JOE );
    }

    @Test
    public void script15()
        throws EntityFinderException
    {
        Male person = templateFor( Male.class );
        // should return Ann and Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class, isNull( person.wife() ),
                                                                        NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, ANN, JOE );
    }

    @Test
    public void script16()
        throws EntityFinderException
    {
        // should return only 2 entities
        final List<EntityReference> references = toList( entityFinder.findEntities( Nameable.class, ALL, NO_SORTING,
                                                                                    NO_FIRST_RESULT, 2, Collections.<String, Object>emptyMap() ) );
        assertEquals( "2 identitities", 2, references.size() );
    }

    @Test
    public void script17()
        throws EntityFinderException
    {
        // should return only 2 entities starting with third one
        final List<EntityReference> references = toList( entityFinder.findEntities( Nameable.class, ALL, NO_SORTING, 3,
                                                                                    2, Collections.<String, Object>emptyMap() ) );
        assertEquals( "2 identitities", 2, references.size() );
    }

    @Test
    public void script18()
        throws EntityFinderException
    {
        // should return all Nameable entities sorted by name
        Nameable nameable = templateFor( Nameable.class );
        final String[] allNames = NameableAssert.allNames();
        Arrays.sort( allNames );

        Iterable<EntityReference> entities = entityFinder.findEntities( Nameable.class, ALL, new OrderBy[]
            {
                orderBy( nameable.name() )
            }, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( false, entities, allNames );
    }

    @Test
    public void script19()
        throws EntityFinderException
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
        Iterable<EntityReference> entities = entityFinder.findEntities( Nameable.class, gt( nameable.name(), "B" ),
                                                                        new OrderBy[]
                                                                            {
                                                                                orderBy( nameable.name() )
                                                                            }, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( false, entities, largerThanB.toArray( new String[ largerThanB.size() ] ) );
    }

    @Test
    public void script20()
        throws EntityFinderException
    {
        // should return all Persons born after 1973 (Ann and Joe Doe) sorted descending by name
        Person person = templateFor( Person.class );
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class, gt( person.yearOfBirth(), 1973 ),
                                                                        new OrderBy[]
                                                                            {
                                                                                orderBy( person.name(), OrderBy.Order.DESCENDING )
                                                                            }, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( false, entities, JOE, ANN );
    }

    @Test
    public void script21()
        throws EntityFinderException
    {
        // should return all Persons sorted name of the city they were born
        Person person = templateFor( Person.class );
        Iterable<EntityReference> entities = entityFinder.findEntities( Person.class, ALL, new OrderBy[]
            {
                orderBy( person.placeOfBirth().get().name() ), orderBy( person.name() )
            }, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( false, entities, ANN, JOE, JACK );
    }

    @Test
    public void script22()
        throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        // should return Jack and Joe Doe
        Iterable<EntityReference> entities = entityFinder.findEntities( Nameable.class,
                                                                        matches( nameable.name(), "J.*Doe" ), NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, Collections.<String, Object>emptyMap() );
        assertNames( entities, JACK, JOE );
    }

    @Test
    public void script23()
        throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );

        // Try using variables
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("domain", "Gaming");
        Iterable<EntityReference> entities = entityFinder.findEntities( Domain.class, QueryExpressions.eq( nameable.name(), QueryExpressions.<String>variable( "domain" ) ),
                                                                        NO_SORTING, NO_FIRST_RESULT, NO_MAX_RESULTS, variables );
        assertNames( entities, "Gaming" );
    }
}