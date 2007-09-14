package org.qi4j.runtime;

import java.lang.reflect.InvocationHandler;
import java.util.Collections;
import org.qi4j.api.DependencyInjectionContext;
import org.qi4j.api.DependencyKey;
import org.qi4j.api.DependencyResolution;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.InvalidDependencyException;
import org.qi4j.api.ModifierDependencyInjectionContext;

/**
 * TODO
 */
public final class ModifiesDependencyResolver
    implements DependencyResolver
{
    public DependencyResolution resolveDependency( DependencyKey key )
        throws InvalidDependencyException
    {
        if( key.getDependencyType().isAssignableFrom( key.getCompositeType() ) ||
            ( key.getFragmentType().equals( InvocationHandler.class ) && key.getDependencyType().equals( InvocationHandler.class ) ) )
        {
            return new ModifiesDependencyResolution();
        }
        else
        {
            throw new InvalidDependencyException( "Composite " + key.getCompositeType() + " does not implement @Modifies type " + key.getDependencyType() + " in modifier " + key.getFragmentType() );
        }
    }

    private class ModifiesDependencyResolution implements DependencyResolution
    {
        public Iterable getDependencyInjection( DependencyInjectionContext context )
        {
            return Collections.singleton( ( (ModifierDependencyInjectionContext) context ).getModifies() );
        }
    }
}