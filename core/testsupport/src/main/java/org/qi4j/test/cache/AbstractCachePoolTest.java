/*
 * Copyright 2010 Niclas Hedhman.
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
package org.qi4j.test.cache;

import java.util.Collection;
import java.util.Random;
import org.junit.Test;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.spi.cache.Cache;
import org.qi4j.spi.cache.CachePool;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Abstract satisfiedBy with tests for the CachePool interface.
 */
public abstract class AbstractCachePoolTest
    extends AbstractQi4jTest
{
    protected CachePool cachePool;
    protected Cache<String> cache;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        cachePool = module.findService( CachePool.class ).get();
        cache = cachePool.fetchCache( "1", String.class );
    }

    @Test
    public void givenInvalidCacheNameWhenFetchingCacheExpectNullArgumentException()
    {
        try
        {
            cache = cachePool.fetchCache( "", String.class );
            fail( "Expected " + NullArgumentException.class.getSimpleName() );
        }
        catch( NullArgumentException e )
        {
            // expected
        }
        try
        {
            cache = cachePool.fetchCache( null, String.class );
            fail( "Expected " + NullArgumentException.class.getSimpleName() );

        }
        catch( ConstraintViolationException e )
        {
            // expected
            Collection<ConstraintViolation> violations = e.constraintViolations();
            assertEquals( 1, violations.size() );
            ConstraintViolation violation = violations.iterator().next();
            assertEquals( "not optional", violation.constraint().toString() );
            assertEquals( "param1", violation.name() );
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
        assertNull( cache.get( "1" ) );
    }

    @Test
    public void givenCacheWithAValueWhenRequestingThatValueExpectItBack()
    {
        cache.put( "Habba", "Zout" );
        assertEquals( "Zout", cache.get( "Habba" ) );
    }

    @Test
    public void givenCacheWithAValueWhenReplacingValueExpectNewValue()
    {
        cache.put( "Habba", "Zout" );
        assertEquals( "Zout", cache.get( "Habba" ) );
        cache.put( "Habba", "Zout2" );
        assertEquals( "Zout2", cache.get( "Habba" ) );
    }

    @Test
    public void givenCacheWithValueWhenDroppingReferenceAndRequestNewCacheAndItsValueExpectItToBeGone()
    {
        cache.put( "Habba", "Zout" );
        assertEquals( "Zout", cache.get( "Habba" ) );
        cachePool.returnCache( cache );
        cache = cachePool.fetchCache( "1", String.class );
        assertNull( "Value not missing", cache.get( "Habba" ) );
    }

}
