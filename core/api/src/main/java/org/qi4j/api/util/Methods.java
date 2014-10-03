package org.qi4j.api.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.qi4j.functional.Iterables.iterable;

/**
 * Useful methods for handling Methods.
 */
public class Methods
{
    public static final Predicate<Type> HAS_METHODS = new Predicate<Type>()
    {
        @Override
        public boolean test( Type item )
        {
            return Classes.RAW_CLASS.apply( item ).getDeclaredMethods().length > 0;
        }
    };

    public static final Function<Type, Iterable<Method>> METHODS_OF = Classes.forTypes( new Function<Type, Iterable<Method>>()
    {
        @Override
        public Iterable<Method> apply( Type type )
        {
            return iterable( Classes.RAW_CLASS.apply( type ).getDeclaredMethods() );
        }
    } );
}
