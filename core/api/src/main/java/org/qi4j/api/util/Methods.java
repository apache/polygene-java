package org.qi4j.api.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.qi4j.functional.Function;
import org.qi4j.functional.Specification;

import static org.qi4j.functional.Iterables.iterable;

/**
 * Useful methods for handling Methods.
 */
public class Methods
{
    public static final Specification<Type> HAS_METHODS = new Specification<Type>()
    {
        @Override
        public boolean satisfiedBy( Type item )
        {
            return Classes.RAW_CLASS.map( item ).getDeclaredMethods().length > 0;
        }
    };

    public static final Function<Type, Iterable<Method>> METHODS_OF = Classes.forTypes( new Function<Type, Iterable<Method>>()
    {
        @Override
        public Iterable<Method> map( Type type )
        {
            return iterable( Classes.RAW_CLASS.map( type ).getDeclaredMethods() );
        }
    } );
}
