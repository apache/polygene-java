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
package org.apache.polygene.test.cache;

import java.util.Collection;
import java.util.Random;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.constraint.ValueConstraintViolation;
import org.apache.polygene.spi.cache.Cache;
import org.apache.polygene.spi.cache.CachePool;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Abstract satisfiedBy with tests for the CachePool interface.
 */
public abstract class AbstractCachePoolTest
    extends AbstractPolygeneTest
{
    protected CachePool cachePool;
    protected Cache<String> cache;

    @Override
    @BeforeEach
    public void setUp()
        throws Exception
    {
        super.setUp();
        cachePool = module.instance().findService( CachePool.class ).get();
        cache = cachePool.fetchCache( "1", String.class );
    }

    @Test
    public void givenInvalidCacheNameWhenFetchingCacheExpectIllegalArgumentException()
    {
        try
        {
            cache = cachePool.fetchCache( "", String.class );
            fail( "Expected " + IllegalArgumentException.class.getSimpleName() );
        }
        catch( IllegalArgumentException e )
        {
            // expected
        }
    }

    @Test
    public void givenNullKeyWhenFetchingCacheExpectConstraintViolationException()
    {
        try
        {
            cache = cachePool.fetchCache( null, String.class );
            fail( "Expected " + ConstraintViolationException.class.getSimpleName() );
        }
        catch( ConstraintViolationException e )
        {
            // expected
            Collection<ValueConstraintViolation> violations = e.constraintViolations();
            assertThat( violations.size(), equalTo( 1 ) );
            ValueConstraintViolation violation = violations.iterator().next();
            assertThat( violation.constraint().toString(), equalTo( "not optional" ) );
            assertThat( violation.name(), anyOf( equalTo( "cacheId" ), equalTo( "arg0" ) ) );  // depends on whether -parameters was given at compile time.
        }
    }

    @Test
    public void givenLoooongCacheNameWhenFetchingCacheExpectOk()
    {
        Random random = new Random();
        StringBuilder longName = new StringBuilder();
        for( int i = 0; i < 10000; i++ )
        {
            longName.append( (char) ( random.nextInt( 26 ) + 65 ) );
        }
        cache = cachePool.fetchCache( longName.toString(), String.class );
    }

    @Test
    public void givenEmptyCacheWhenFetchingValueExpectNull()
    {
        assertThat( cache.get( "1" ), nullValue() );
    }

    @Test
    public void givenCacheWithAValueWhenRequestingThatValueExpectItBack()
    {
        cache.put( "Habba", "Zout" );
        assertThat( cache.get( "Habba" ), equalTo( "Zout" ) );
    }

    @Test
    public void givenCacheWithAValueWhenReplacingValueExpectNewValue()
    {
        cache.put( "Habba", "Zout" );
        assertThat( cache.get( "Habba" ), equalTo( "Zout" ) );
        cache.put( "Habba", "Zout2" );
        assertThat( cache.get( "Habba" ), equalTo( "Zout2" ) );
    }

    @Test
    public void givenCacheWithValueWhenDroppingReferenceAndRequestNewCacheAndItsValueExpectItToBeGone()
    {
        cache.put( "Habba", "Zout" );
        assertThat( cache.get( "Habba" ), equalTo( "Zout" ) );
        cachePool.returnCache( cache );
        cache = cachePool.fetchCache( "1", String.class );
        assertThat( "Value not missing", cache.get( "Habba" ), nullValue() );
    }
}
