package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import org.qi4j.functional.Specification;

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
