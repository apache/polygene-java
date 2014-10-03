package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.util.function.Predicate;

/**
 * Specification that checks whether a given class implements InvocationHandler or not.
 */
public class GenericSpecification
    implements Predicate<Class<?>>
{
    public static final GenericSpecification INSTANCE = new GenericSpecification();

    @Override
    public boolean test( Class<?> item )
    {
        return InvocationHandler.class.isAssignableFrom( item );
    }
}
