package org.qi4.cache.ehcache;

import java.util.Collection;
import java.util.Random;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.cache.ehcache.assembly.EhCacheAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.cache.Cache;
import org.qi4j.spi.cache.CachePool;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class EhCacheTest extends AbstractQi4jTest
{
    private CachePool caching;
    private Cache<String> cache;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EhCacheAssembler( Visibility.module ).assemble( module );
        ModuleAssembly confModule = module.layer().module( "confModule" );
        confModule.services( MemoryEntityStoreService.class ).visibleIn( Visibility.layer );
        confModule.services( UuidIdentityGeneratorService.class );
    }

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        ServiceReference<Object> service = serviceLocator.findService( CachePool.class );
        caching = (CachePool) service.get();
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
        StringBuffer longName = new StringBuffer();
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
