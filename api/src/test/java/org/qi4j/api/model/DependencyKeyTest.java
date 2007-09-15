package org.qi4j.api.model;
/**
 *  TODO
 */

import junit.framework.TestCase;
import org.qi4j.api.annotation.scope.Service;
import org.qi4j.api.query.Query;

public class DependencyKeyTest extends TestCase
{
    DependencyKey dependencyKey;

    public void testGetAnnotationType() throws Exception
    {
        DependencyKey key1 = new DependencyKey( Service.class, DependencyTest.class.getDeclaredField( "foo" ).getGenericType(), "foo", DependencyTest.class );
        DependencyKey key2 = new DependencyKey( Service.class, DependencyTest.class.getDeclaredField( "fooIterable" ).getGenericType(), "foo", DependencyTest.class );
        DependencyKey key3 = new DependencyKey( Service.class, DependencyTest.class.getDeclaredField( "fooQuery" ).getGenericType(), "foo", DependencyTest.class );

        assertEquals( Mixin1.class, key1.getDependencyType() );
        assertEquals( Mixin1.class, key1.getRawType() );

        assertEquals( Mixin1.class, key2.getDependencyType() );
        assertEquals( Iterable.class, key2.getRawType() );

        assertEquals( Mixin1.class, key3.getDependencyType() );
        assertEquals( Query.class, key3.getRawType() );
    }

    public class DependencyTest
    {
        @Service Mixin1 foo;
        @Service Iterable<Mixin1> fooIterable;
        @Service Query<Mixin1> fooQuery;
    }
}