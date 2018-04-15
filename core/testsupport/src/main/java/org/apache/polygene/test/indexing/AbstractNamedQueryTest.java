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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.grammar.OrderBy;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.spi.query.IndexExporter;
import org.apache.polygene.test.model.Domain;
import org.apache.polygene.test.model.Female;
import org.apache.polygene.test.model.Male;
import org.apache.polygene.test.model.Nameable;
import org.apache.polygene.test.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.polygene.api.query.QueryExpressions.orderBy;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;
import static org.apache.polygene.test.indexing.NameableAssert.verifyOrderedResults;
import static org.apache.polygene.test.indexing.NameableAssert.verifyUnorderedResults;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Abstract satisfiedBy with tests for named queries against Index/Query engines.
 */
public abstract class AbstractNamedQueryTest
    extends AbstractAnyQueryTest
{
    @Structure
    private Module moduleInstance;

    protected final Map<String, Predicate<Composite>> queries = new HashMap<>();

    @BeforeEach
    public void assembleQueryStrings()
    {
        String[] query = queryStrings();
        for( int i = 0; i < query.length; i++ )
        {
            String queryName = String.format( "script%02d", i + 1 );
            if( query[i].length() != 0 )
            {
                Predicate<Composite> expression = createNamedQueryDescriptor( queryName, query[i] );
                queries.put( queryName, expression );
            }
        }
    }

    protected abstract String[] queryStrings();

    protected abstract Predicate<Composite> createNamedQueryDescriptor( String queryName, String queryString );

    @Test
    public void showNetwork()
        throws IOException
    {
        IndexExporter indexerExporter = moduleInstance.findService( IndexExporter.class ).get();
        indexerExporter.exportReadableToStream( System.out );
    }

    @Test
    public void script01()
    {
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script01" ) ) );
        System.out.println( "*** script01: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script02()
    {
        final Query<Domain> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Domain.class )
            .where( queries.get( "script02" ) ) );
        System.out.println( "*** script02: " + query );
        verifyUnorderedResults( query, "Gaming" );
    }

    @Test
    public void script03()
    {
        final Query<Nameable> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Nameable.class )
            .where( queries.get( "script03" ) ) );
        System.out.println( "*** script03: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Felix", "Ann Doe", "Jack Doe", "Penang", "Kuala Lumpur", "Cooking",
                                "Gaming", "Programming", "Cars" );
    }

    @Test
    public void script04()
    {
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script04" ) ) );
        System.out.println( "*** script04: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script05()
    {
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script05" ) ) );
        System.out.println( "*** script05: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script06()
    {
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script06" ) ) );
        System.out.println( "*** script06: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script07()
    {
        final Query<Nameable> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Nameable.class )
            .where( queries.get( "script07" ) ) );
        System.out.println( "*** script07: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script08()
    {
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script08" ) ) );
        System.out.println( "*** script08: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    public void script09()
    {
        final Query<Female> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Female.class )
            .where( queries.get( "script09" ) ) );
        System.out.println( "*** script09: " + query );
        verifyUnorderedResults( query, "Ann Doe" );
    }

    @Test
    public void script10()
    {
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script10" ) ) );
        System.out.println( "*** script10: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script11()
    {
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script11" ) ) );
        System.out.println( "*** script11: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script12()
    {
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script12" ) ) );
        System.out.println( "*** script12: " + query );
        verifyUnorderedResults( query, "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script13()
    {
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script13" ) ) );
        System.out.println( "*** script13: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script14()
    {
        final Query<Male> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Male.class )
            .where( queries.get( "script14" ) ) );
        System.out.println( "*** script14: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script15()
    {
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script15" ) ) );
        System.out.println( "*** script15: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script16()
    {
        Nameable nameable = templateFor( Nameable.class );
        final Query<Nameable> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Nameable.class )
            .where( queries.get( "script16" ) ) );
        query.orderBy( orderBy( nameable.name() ) );
        query.maxResults( 2 );
        System.out.println( "*** script16: " + query );
        verifyOrderedResults( query, "Ann Doe", "Cars" );
    }

    @Test
    public void script17()
    {
        Nameable nameable = templateFor( Nameable.class );
        Predicate<Composite> predicate = queries.get( "script17" );
        final Query<Nameable> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Nameable.class )
            .where( predicate ) );
        query.orderBy( orderBy( nameable.name() ) );
        query.firstResult( 3 );
        query.maxResults( 3 );
        System.out.println( "*** script17: " + query );
        verifyOrderedResults( query, "Felix", "Gaming", "Jack Doe" );
    }

    @Test
    public void script18()
    {
        Nameable nameable = templateFor( Nameable.class );
        Predicate<Composite> predicate = queries.get( "script18" );
        final Query<Nameable> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Nameable.class )
            .where( predicate ) );
        query.orderBy( orderBy( nameable.name() ) );
        System.out.println( "*** script18: " + query );
        verifyOrderedResults( query, "Ann Doe", "Cars", "Cooking", "Felix", "Gaming", "Jack Doe", "Joe Doe",
                              "Kuala Lumpur", "Penang", "Programming" );
    }

    @Test
    public void script19()
    {
        Nameable nameable = templateFor( Nameable.class );
        final Query<Nameable> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Nameable.class )
            .where( queries.get( "script19" ) ) );
        query.orderBy( orderBy( nameable.name() ) );
        System.out.println( "*** script19: " + query );
        verifyOrderedResults( query, "Felix", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming" );
    }

    @Test
    public void script20()
    {
        Person person = templateFor( Person.class );
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script20" ) ) );
        query.orderBy( orderBy( person.name(), OrderBy.Order.DESCENDING ) );
        System.out.println( "*** script20: " + query );
        verifyOrderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script21()
    {
        Person person = templateFor( Person.class );
        final Query<Person> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Person.class )
            .where( queries.get( "script21" ) ) );
        query.orderBy( orderBy( person.placeOfBirth().get().name() ), orderBy( person.yearOfBirth() ) );
        System.out.println( "*** script21: " + query );
        verifyOrderedResults( query, "Ann Doe", "Joe Doe", "Jack Doe" );
    }

    @Test
    public void script22()
    {
        final Query<Nameable> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Nameable.class )
            .where( queries.get( "script22" ) ) );
        System.out.println( "*** script22: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script24()
    {
        final Query<Domain> query = unitOfWork.newQuery( this.moduleInstance
            .newQueryBuilder( Domain.class )
            .where( queries.get( "script24" ) ) );
        query.setVariable( "domain", "Gaming" );
        System.out.println( "*** script24: " + query );
        assertThat( query.find().name().get(), is( equalTo( "Gaming" ) ) );
    }
}
