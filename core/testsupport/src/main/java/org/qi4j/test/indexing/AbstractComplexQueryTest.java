/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.test.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.query.IndexExporter;
import org.qi4j.test.indexing.model.Address;
import org.qi4j.test.indexing.model.Nameable;
import org.qi4j.test.indexing.model.Person;
import org.qi4j.test.indexing.model.Protocol;
import org.qi4j.test.indexing.model.QueryParam;
import org.qi4j.test.indexing.model.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.qi4j.api.query.QueryExpressions.contains;
import static org.qi4j.api.query.QueryExpressions.containsAll;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.not;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * Test Index/Query service for complex queries, ie. queries by "example values".
 */
public abstract class AbstractComplexQueryTest
    extends AbstractAnyQueryTest
{

    private static final String ANN = "Ann Doe";
    private static final String JOE = "Joe Doe";
    private static final String JACK = "Jack Doe";

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
    public void showNetwork()
        throws IOException
    {
        IndexExporter indexerExporter = module.<IndexExporter>findService( IndexExporter.class ).get();
        indexerExporter.exportReadableToStream( System.out );
    }

    @Test
    public void script01()
    {
        ValueBuilder<Address> addressBuilder = this.module.newValueBuilder( Address.class );
        Address address = addressBuilder.prototype();
        address.line1().set( "Qi Street 4j" );
        address.line2().set( "Off main Java Street" );
        address.zipcode().set( "12345" );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<Address> addressProp = templateFor( Person.class ).address();
        qb = qb.where( eq( addressProp, addressBuilder.newInstance() ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script01: " + query );

        verifyUnorderedResults( query, ANN );
    }

    @Test
    public void script02()
    {
        ValueBuilder<Address> addressBuilder = this.module.newValueBuilder( Address.class );
        Address address = addressBuilder.prototype();
        address.line1().set( "Qi Street 4j" );
        address.line2().set( "Off main Java Street" );
        address.zipcode().set( "12345" );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<Address> addressProp = templateFor( Person.class ).address();
        qb = qb.where( not( eq( addressProp, addressBuilder.newInstance() ) ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script02: " + query );

        verifyUnorderedResults( query, JOE, JACK );
    }

    @Test
    public void script03()
    {
        ValueBuilder<QueryParam> queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        List<QueryParam> queryParams = new ArrayList<QueryParam>();
        QueryParam param = queryParamBuilder.prototype();
        param.name().set( "user" );
        param.value().set( "jackdoe" );
        queryParams.add( queryParamBuilder.newInstance() );

        queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        param = queryParamBuilder.prototype();
        param.name().set( "password" );
        param.value().set( "somepassword" );
        queryParams.add( queryParamBuilder.newInstance() );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<Collection<QueryParam>> paramsProp = templateFor( Person.class ).personalWebsite().get().queryParams();
        qb = qb.where( eq( paramsProp, queryParams ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script03: " + query );

        verifyUnorderedResults( query, JACK );
    }

    @Test
    public void script04()
    {
        ValueBuilder<QueryParam> queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        List<QueryParam> queryParams = new ArrayList<QueryParam>();
        QueryParam param = queryParamBuilder.prototype();
        // Different order
        param.name().set( "password" );
        param.value().set( "somepassword" );
        queryParams.add( queryParamBuilder.newInstance() );

        queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        param = queryParamBuilder.prototype();
        param.name().set( "user" );
        param.value().set( "jackdoe" );
        queryParams.add( queryParamBuilder.newInstance() );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<Collection<QueryParam>> paramsProp = templateFor( Person.class ).personalWebsite().get().queryParams();
        qb = qb.where( eq( paramsProp, queryParams ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script04: " + query );

        verifyUnorderedResults( query );
    }

    @Test
    public void script05()
    {
        ValueBuilder<QueryParam> queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        List<QueryParam> queryParams = new ArrayList<QueryParam>();
        QueryParam param = queryParamBuilder.prototype();
        param.name().set( "user" );
        param.value().set( "jackdoe" );
        queryParams.add( queryParamBuilder.newInstance() );

        queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        param = queryParamBuilder.prototype();
        param.name().set( "password" );
        param.value().set( "somepassword" );
        queryParams.add( queryParamBuilder.newInstance() );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<Collection<QueryParam>> paramsProp = templateFor( Person.class ).personalWebsite().get().queryParams();
        qb = qb.where( not( eq( paramsProp, queryParams ) ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script05: " + query );

        verifyUnorderedResults( query, ANN, JOE );
    }

    @Test
    public void script06()
    {
        ValueBuilder<URL> urlBuilder = this.module.newValueBuilder( URL.class );
        ValueBuilder<Protocol> protocolBuilder = this.module.newValueBuilder( Protocol.class );
        ValueBuilder<QueryParam> queryParamBuilder = this.module.newValueBuilder( QueryParam.class );

        Protocol protocol = protocolBuilder.prototype();
        protocol.value().set( "http" );

        List<QueryParam> queryParams = new ArrayList<QueryParam>();
        QueryParam param = queryParamBuilder.prototype();
        param.name().set( "user" );
        param.value().set( "jackdoe" );
        queryParams.add( queryParamBuilder.newInstance() );

        queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        param = queryParamBuilder.prototype();
        param.name().set( "password" );
        param.value().set( "somepassword" );
        queryParams.add( queryParamBuilder.newInstance() );

        URL url = urlBuilder.prototype();
        url.protocol().set( protocolBuilder.newInstance() );
        url.queryParams().set( queryParams );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<URL> websiteProp = templateFor( Person.class ).personalWebsite();
        qb = qb.where( eq( websiteProp, urlBuilder.newInstance() ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script06: " + query );

        verifyUnorderedResults( query, JACK );
    }

    @Test
    public void script07()
    {
        ValueBuilder<URL> urlBuilder = this.module.newValueBuilder( URL.class );
        ValueBuilder<Protocol> protocolBuilder = this.module.newValueBuilder( Protocol.class );
        ValueBuilder<QueryParam> queryParamBuilder = this.module.newValueBuilder( QueryParam.class );

        Protocol protocol = protocolBuilder.prototype();
        protocol.value().set( "http" );

        List<QueryParam> queryParams = new ArrayList<QueryParam>();
        QueryParam param = queryParamBuilder.prototype();
        param.name().set( "user" );
        param.value().set( "jackdoe" );
        queryParams.add( queryParamBuilder.newInstance() );

        queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        param = queryParamBuilder.prototype();
        param.name().set( "password" );
        param.value().set( "somepassword" );
        queryParams.add( queryParamBuilder.newInstance() );

        URL url = urlBuilder.prototype();
        url.protocol().set( protocolBuilder.newInstance() );
        url.queryParams().set( queryParams );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<URL> websiteProp = templateFor( Person.class ).personalWebsite();
        qb = qb.where( not( eq( websiteProp, urlBuilder.newInstance() ) ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script07: " + query );

        verifyUnorderedResults( query, ANN, JOE );
    }

    @Test
    public void script08()
    {
        ValueBuilder<QueryParam> queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        QueryParam param = queryParamBuilder.prototype();
        param.name().set( "user" );
        param.value().set( "jackdoe" );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<Collection<QueryParam>> paramsProp = templateFor( Person.class ).personalWebsite().get().queryParams();
        qb = qb.where( contains( paramsProp, queryParamBuilder.newInstance() ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script08: " + query );
        verifyUnorderedResults( query, JACK );
    }

    @Test
    public void script09()
    {
        ValueBuilder<QueryParam> queryParamBuilder = this.module.newValueBuilder( QueryParam.class );

        QueryParam param = queryParamBuilder.prototype();
        param.name().set( "user" );
        param.value().set( "jackdoe" );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<Collection<QueryParam>> paramsProp = templateFor( Person.class ).personalWebsite().get().queryParams();
        qb = qb.where( not( contains( paramsProp, queryParamBuilder.newInstance() ) ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script09: " + query );
        verifyUnorderedResults( query, ANN, JOE );
    }

    @Test
    public void script10()
    {
        ValueBuilder<QueryParam> queryParamBuilder = this.module.newValueBuilder( QueryParam.class );

        List<QueryParam> queryParams = new ArrayList<QueryParam>();
        QueryParam param = queryParamBuilder.prototype();
        param.name().set( "user" );
        param.value().set( "jackdoe" );
        queryParams.add( queryParamBuilder.newInstance() );

        queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        param = queryParamBuilder.prototype();
        param.name().set( "password" );
        param.value().set( "somepassword" );
        queryParams.add( queryParamBuilder.newInstance() );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<Collection<QueryParam>> paramsProp = templateFor( Person.class ).personalWebsite().get().queryParams();
        qb = qb.where( containsAll( paramsProp, queryParams ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script10: " + query );
        verifyUnorderedResults( query, JACK );
    }

    @Test
    public void script11()
    {
        ValueBuilder<QueryParam> queryParamBuilder = this.module.newValueBuilder( QueryParam.class );

        List<QueryParam> queryParams = new ArrayList<QueryParam>();
        QueryParam param = queryParamBuilder.prototype();
        param.name().set( "user" );
        param.value().set( "jackdoe" );
        queryParams.add( queryParamBuilder.newInstance() );

        queryParamBuilder = this.module.newValueBuilder( QueryParam.class );
        param = queryParamBuilder.prototype();
        param.name().set( "password" );
        param.value().set( "somepassword" );
        queryParams.add( queryParamBuilder.newInstance() );

        QueryBuilder<Person> qb = this.module.newQueryBuilder( Person.class );
        Property<Collection<QueryParam>> paramsProp = templateFor( Person.class ).personalWebsite().get().queryParams();
        qb = qb.where( not( containsAll( paramsProp, queryParams ) ) );
        Query<Person> query = unitOfWork.newQuery( qb );
        System.out.println( "*** script11: " + query );
        verifyUnorderedResults( query, ANN, JOE );
    }
}
