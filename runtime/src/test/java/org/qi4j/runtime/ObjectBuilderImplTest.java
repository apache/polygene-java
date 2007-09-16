package org.qi4j.runtime;
/**
 *  TODO
 */

import java.security.Guard;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.qi4j.api.ObjectBuilder;
import static org.qi4j.api.PropertyValue.property;
import org.qi4j.api.annotation.scope.Adapt;
import org.qi4j.api.annotation.scope.Decorate;
import static org.qi4j.api.model.Binding.bind;
import static org.qi4j.api.model.InjectionKey.key;

public class ObjectBuilderImplTest extends TestCase
{
    public void testNewInstance() throws Exception
    {
        ObjectBuilder<TestClass> builder = new ObjectBuilderFactoryImpl().newObjectBuilder( TestClass.class );

        List<String> strings = new ArrayList<String>();
        strings.add( "Foo" );
        strings.add( "Bar" );
        builder.adapt( bind( key( List.class, String.class ), strings ) );

        Set<Integer> numbers = new HashSet<Integer>();
        numbers.add( 3 );
        numbers.add( 4 );
        numbers.add( 5 );
        builder.adapt( bind( key( Set.class, Integer.class ), numbers ) );

        builder.decorate( new Guard()
        {
            public void checkGuard( Object o ) throws SecurityException
            {

            }
        } );
        builder.properties( 3, 4.5f, "c", property( "foo", "foo" ) );

        TestClass test = builder.newInstance();

        assertEquals( strings, test.strings );
    }

    public static class TestClass
        implements Guard
    {
        String foo;
        int a;
        float b;
        String c;

        @Adapt List<String> strings;
        @Adapt Iterable<Number> numbers;
        @Adapt Collection<Comparable> strings2;
        @Adapt Set<Comparable> numbers2;

        @Decorate Guard guard;

        public TestClass( Integer a, Float b, String c )
        {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public void checkGuard( Object o ) throws SecurityException
        {
        }
    }
}