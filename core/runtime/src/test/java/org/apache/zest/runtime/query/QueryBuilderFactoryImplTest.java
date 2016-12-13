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

package org.apache.zest.runtime.query;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractPolygeneTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.apache.zest.api.query.QueryExpressions.and;
import static org.apache.zest.api.query.QueryExpressions.ge;
import static org.apache.zest.api.query.QueryExpressions.lt;
import static org.apache.zest.api.query.QueryExpressions.orderBy;
import static org.apache.zest.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
public class QueryBuilderFactoryImplTest
    extends AbstractPolygeneTest
{
    private List<TestComposite> composites;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestComposite.class );
    }

    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();
        composites = new ArrayList<>();
        composites.add( newInstance( "A", 6 ) );
        composites.add( newInstance( "B", 2 ) );
        composites.add( newInstance( "C", 3 ) );
        composites.add( newInstance( "C", 4 ) );
        composites.add( newInstance( "E", 5 ) );
        composites.add( newInstance( "A", 1 ) );
    }

    @Test
    public void givenPlainQueryWhenFindEntityExpectFirstEntityReturned()
    {
        Query<TestComposite> query = queryBuilderFactory.newQueryBuilder( TestComposite.class ).newQuery( composites );
        assertEquals( "A", query.find().a().get() );
        assertEquals( 6, query.count() );
    }

    @Test
    public void givenPlainQueryWhenOrderByFirstPropertyExpectOrderedResult()
    {
        Query<TestComposite> query = queryBuilderFactory.newQueryBuilder( TestComposite.class ).newQuery( composites );
        TestComposite template = templateFor( TestComposite.class );
        query.orderBy( orderBy( template.a() ) );
        verifyOrder( query, "612345" );
    }

    @Test
    public void givenPlainQueryWhenOrderBySecondPropertyExpectOrderedResult()
    {
        Query<TestComposite> query = queryBuilderFactory.newQueryBuilder( TestComposite.class ).newQuery( composites );
        TestComposite template = templateFor( TestComposite.class );
        query.orderBy( orderBy( template.b() ) );
        verifyOrder( query, "123456" );
    }

    @Test
    public void givenPlainQueryWhenOrderByTwoPropertiesExpectOrderedResult()
    {
        Query<TestComposite> query = queryBuilderFactory.newQueryBuilder( TestComposite.class ).newQuery( composites );
        TestComposite template = templateFor( TestComposite.class );
        query.orderBy( orderBy( template.a() ), orderBy( template.b() ) );
        verifyOrder( query, "162345" );
    }

    @Test
    public void givenPlainQueryWhenMaxedResultExpectLimitedResult()
    {
        Query<TestComposite> query = queryBuilderFactory.newQueryBuilder( TestComposite.class ).newQuery( composites );
        query.maxResults( 5 );
        verifyOrder( query, "62345" );
    }

    @Test
    public void givenPlainQueryWhenFirstResultIsBeyondFirstElementExpectLimitedResult()
    {
        Query<TestComposite> query = queryBuilderFactory.newQueryBuilder( TestComposite.class ).newQuery( composites );
        query.firstResult( 2 );
        verifyOrder( query, "3451" );
    }

    @Test
    public void givenWhereQueryWhenWhereClauseLimitsToRangeExpectLimitedResult()
    {
        final QueryBuilder<TestComposite> qb = queryBuilderFactory.newQueryBuilder( TestComposite.class );
        TestComposite template = templateFor( TestComposite.class );
        Query<TestComposite> query = qb.where(
            and(
                ge( template.b(), 3 ),
                lt( template.b(), 5 )
            )
        ).newQuery( composites );
        verifyOrder( query, "34" );
    }

    private void verifyOrder( Query<TestComposite> query, String expected )
    {
        String actual = "";
        for( TestComposite testComposite : query )
        {
            actual = actual + testComposite.b().get();
        }

        assertThat( "Query is correct", actual, equalTo( expected ) );
        assertThat( "Count is correct", query.count(), equalTo( (long) expected.length() ) );
    }

    private TestComposite newInstance( String a, int b )
    {
        TransientBuilder<TestComposite> builder =
            transientBuilderFactory.newTransientBuilder( TestComposite.class );
        TestComposite instance = builder.prototype();
        instance.a().set( a );
        instance.b().set( b );
        return builder.newInstance();
    }

    public interface TestComposite
        extends TransientComposite
    {
        Property<String> a();

        Property<Integer> b();
    }
}
