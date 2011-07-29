package org.qi4j.runtime.composite;

import org.qi4j.functional.Specification;

import java.lang.reflect.InvocationHandler;

/**
 * Specification that checks whether a given class implements InvocationHandler or not.
 */
public class GenericSpecification
    implements Specification<Class<?>>
{
    public static final GenericSpecification INSTANCE = new GenericSpecification();

    @Override
    public boolean satisfiedBy( Class<?> item )
    {
        return InvocationHandler.class.isAssignableFrom( item );
    }
}
