package org.qi4j.api;

import java.util.Collections;

/**
 * If a dependency resolution is static, use this class as a simple holder
 * that always returns the same object.
 */
public class StaticDependencyResolution
    implements DependencyResolution
{
    private Iterable iterable;

    public StaticDependencyResolution( Iterable iterable )
    {
        this.iterable = iterable;
    }

    public StaticDependencyResolution(Object object)
    {
        this.iterable = Collections.singleton( object );
    }

    public Iterable getDependencyInjection( DependencyInjectionContext context )
    {
        return iterable;
    }
}
