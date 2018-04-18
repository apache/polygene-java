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

package org.apache.polygene.runtime.query;

import java.util.ArrayList;
import java.util.List;
import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.polygene.api.query.QueryExpressions.and;
import static org.apache.polygene.api.query.QueryExpressions.ge;
import static org.apache.polygene.api.query.QueryExpressions.lt;
import static org.apache.polygene.api.query.QueryExpressions.orderBy;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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

    @Override
    @BeforeEach
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
        assertThat( query.find().a().get(), equalTo( "A" ) );
        assertThat( query.count(), equalTo( 6L ) );
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
        StringBuilder actual = new StringBuilder();
        for( TestComposite testComposite : query )
        {
            actual.append( testComposite.b().get() );
        }

        assertThat( "Query is correct", actual.toString(), equalTo( expected ) );
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
