/*
 * Copyright 2009 Niclas Hedhman.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Specification;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.IndexExporter;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.model.Domain;
import org.qi4j.test.indexing.model.Female;
import org.qi4j.test.indexing.model.Male;
import org.qi4j.test.indexing.model.Nameable;
import org.qi4j.test.indexing.model.Person;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

public abstract class AbstractNamedQueryTest
    extends AbstractAnyQueryTest
{

    Map<String, Specification<Composite>> queries = new HashMap<String, Specification<Composite>>();

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        new EntityTestAssembler().assemble( module );
        String[] query = queryStrings();
        for( int i = 0; i < query.length; i++ )
        {
            String queryName = String.format( "script%02d", i + 1 );
            if( query[ i ].length() != 0 )
            {
                Specification<Composite> expression = createNamedQueryDescriptor( queryName, query[ i ] );
                queries.put( queryName, expression );
            }
        }
    }

    protected abstract String[] queryStrings();

    protected abstract Specification<Composite> createNamedQueryDescriptor( String queryName, String queryString );

    @Test
    public void showNetwork()
        throws IOException
    {
        IndexExporter indexerExporter = module.findService( IndexExporter.class ).get();
        indexerExporter.exportReadableToStream( System.out );
    }

    private static void verifyUnorderedResults( final Iterable<? extends Nameable> results, final String... names )
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

    private static void verifyOrderedResults( final Iterable<? extends Nameable> results, final String... names )
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
    public void script01()
        throws EntityFinderException
    {
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script01" ) ) );
        System.out.println( "*** script01: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script02()
        throws EntityFinderException
    {
        final Query<Domain> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Domain.class )
                                                             .where( queries.get( "script02" ) ) );
        System.out.println( "*** script02: " + query );
        verifyUnorderedResults( query, "Gaming" );
    }

    @Test
    public void script03()
        throws EntityFinderException
    {
        final Query<Nameable> query = unitOfWork.newQuery( this.module
                                                               .newQueryBuilder( Nameable.class )
                                                               .where( queries.get( "script03" ) ) );
        System.out.println( "*** script03: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe", "Penang", "Kuala Lumpur", "Cooking", "Gaming",
                                "Programming", "Cars" );
    }

    @Test
    public void script04()
        throws EntityFinderException
    {
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script04" ) ) );
        System.out.println( "*** script04: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script05()
        throws EntityFinderException
    {
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script05" ) ) );
        System.out.println( "*** script05: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script06()
        throws EntityFinderException
    {
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script06" ) ) );
        System.out.println( "*** script06: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script07()
        throws EntityFinderException
    {
        final Query<Nameable> query = unitOfWork.newQuery( this.module
                                                               .newQueryBuilder( Nameable.class )
                                                               .where( queries.get( "script07" ) ) );
        System.out.println( "*** script07: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script08()
        throws EntityFinderException
    {
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script08" ) ) );
        System.out.println( "*** script08: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    public void script09()
        throws EntityFinderException
    {
        final Query<Female> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Female.class )
                                                             .where( queries.get( "script09" ) ) );
        System.out.println( "*** script09: " + query );
        verifyUnorderedResults( query, "Ann Doe" );
    }

    @Test
    public void script10()
        throws EntityFinderException
    {
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script10" ) ) );
        System.out.println( "*** script10: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script11()
        throws EntityFinderException
    {
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script11" ) ) );
        System.out.println( "*** script11: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script12()
        throws EntityFinderException
    {
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script12" ) ) );
        System.out.println( "*** script12: " + query );
        verifyUnorderedResults( query, "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script13()
        throws EntityFinderException
    {
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script13" ) ) );
        System.out.println( "*** script13: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script14()
        throws EntityFinderException
    {
        final Query<Male> query = unitOfWork.newQuery( this.module
                                                           .newQueryBuilder( Male.class )
                                                           .where( queries.get( "script14" ) ) );
        System.out.println( "*** script14: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script15()
        throws EntityFinderException
    {
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script15" ) ) );
        System.out.println( "*** script15: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script16()
        throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        final Query<Nameable> query = unitOfWork.newQuery( this.module
                                                               .newQueryBuilder( Nameable.class )
                                                               .where( queries.get( "script16" ) ) );
        query.orderBy( orderBy( nameable.name() ) );
        query.maxResults( 2 );
        System.out.println( "*** script16: " + query );
        verifyOrderedResults( query, "Ann Doe", "Cars" );
    }

    @Test
    public void script17()
        throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        final Query<Nameable> query = unitOfWork.newQuery( this.module
                                                               .newQueryBuilder( Nameable.class )
                                                               .where( queries.get( "script17" ) ) );
        query.orderBy( orderBy( nameable.name() ) );
        query.firstResult( 3 );
        query.maxResults( 3 );
        System.out.println( "*** script17: " + query );
        verifyOrderedResults( query, "Gaming", "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script18()
        throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        final Query<Nameable> query = unitOfWork.newQuery( this.module
                                                               .newQueryBuilder( Nameable.class )
                                                               .where( queries.get( "script18" ) ) );
        query.orderBy( orderBy( nameable.name() ) );
        System.out.println( "*** script18: " + query );
        verifyOrderedResults( query, "Ann Doe", "Cars", "Cooking", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur",
                              "Penang", "Programming" );
    }

    @Test
    public void script19()
        throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        final Query<Nameable> query = unitOfWork.newQuery( this.module
                                                               .newQueryBuilder( Nameable.class )
                                                               .where( queries.get( "script19" ) ) );
        query.orderBy( orderBy( nameable.name() ) );
        System.out.println( "*** script19: " + query );
        verifyOrderedResults( query, "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming" );
    }

    @Test
    public void script20()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script20" ) ) );
        query.orderBy( orderBy( person.name(), OrderBy.Order.DESCENDING ) );
        System.out.println( "*** script20: " + query );
        verifyOrderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script21()
        throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        final Query<Person> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Person.class )
                                                             .where( queries.get( "script21" ) ) );
        query.orderBy( orderBy( person.placeOfBirth().get().name() ), orderBy( person.yearOfBirth() ) );
        System.out.println( "*** script21: " + query );
        verifyOrderedResults( query, "Ann Doe", "Joe Doe", "Jack Doe" );
    }

    @Test
    public void script22()
        throws EntityFinderException
    {
        final Query<Nameable> query = unitOfWork.newQuery( this.module
                                                               .newQueryBuilder( Nameable.class )
                                                               .where( queries.get( "script22" ) ) );
        System.out.println( "*** script22: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script24()
        throws EntityFinderException
    {
        final Query<Domain> query = unitOfWork.newQuery( this.module
                                                             .newQueryBuilder( Domain.class )
                                                             .where( queries.get( "script24" ) ) );
        query.setVariable( "domain", "Gaming" );
        System.out.println( "*** script24: " + query );
        assertThat( query.find().name().get(), is( equalTo( "Gaming" ) ) );
    }
}