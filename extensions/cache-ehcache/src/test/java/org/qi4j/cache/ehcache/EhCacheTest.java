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
package org.qi4j.cache.ehcache;

import java.util.Collection;
import java.util.Random;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.cache.ehcache.assembly.EhCacheAssembler;
import org.qi4j.spi.cache.Cache;
import org.qi4j.spi.cache.CachePool;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.*;

public class EhCacheTest extends AbstractQi4jTest
{
    private CachePool caching;
    private Cache<String> cache;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EhCacheAssembler( Visibility.module ).assemble( module );
        ModuleAssembly confModule = module.layer().module( "confModule" );
        new EntityTestAssembler( Visibility.layer ).assemble( confModule );
    }

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        caching = module.findService( CachePool.class ).get();
        cache = caching.fetchCache( "1", String.class );
    }

    @Test
    public void givenInvalidCacheNameWhenFetchingCacheExpectNullArgumentException()
    {
        try
        {
            cache = caching.fetchCache( "", String.class );
            fail( "Expected " + NullArgumentException.class.getSimpleName() );
        }
        catch( NullArgumentException e )
        {
            // expected
        }
        try
        {
            cache = caching.fetchCache( null, String.class );
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
        cache = caching.fetchCache( longName.toString(), String.class );
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
        caching.returnCache( cache );
        cache = caching.fetchCache( "1", String.class );
        assertNull( "Value not missing", cache.get( "Habba" ) );
    }
}
