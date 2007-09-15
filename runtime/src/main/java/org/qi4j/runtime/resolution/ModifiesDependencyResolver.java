package org.qi4j.runtime.resolution;

import java.lang.reflect.InvocationHandler;
import org.qi4j.api.model.DependencyKey;
import org.qi4j.api.model.FragmentDependencyKey;
import org.qi4j.spi.dependency.DependencyInjectionContext;
import org.qi4j.spi.dependency.DependencyResolution;
import org.qi4j.spi.dependency.DependencyResolver;
import org.qi4j.spi.dependency.InvalidDependencyException;
import org.qi4j.spi.dependency.ModifierDependencyInjectionContext;

/**
 * TODO
 */
public class ModifiesDependencyResolver
    implements DependencyResolver
{
    public DependencyResolution resolveDependency( DependencyKey key )
        throws InvalidDependencyException
    {
        if( key instanceof FragmentDependencyKey )
        {
            FragmentDependencyKey fragmentKey = (FragmentDependencyKey) key;
            if( key.getDependencyType().isAssignableFrom( fragmentKey.getCompositeType() ) ||
                ( key.getDependentType().equals( InvocationHandler.class ) && key.getDependencyType().equals( InvocationHandler.class ) ) )
            {
                return new ModifiesDependencyResolution();
            }
            else
            {
                throw new InvalidDependencyException( "Composite " + fragmentKey.getCompositeType() + " does not implement @Modifies type " + key.getDependencyType() + " in modifier " + key.getDependentType() );
            }
        }
        else
        {
            throw new InvalidDependencyException( "Object " + key.getDependentType() + " may not use @Modifies" );
        }
    }

    private class ModifiesDependencyResolution implements DependencyResolution
    {
        public Object getDependencyInjection( DependencyInjectionContext context )
        {
            return ( (ModifierDependencyInjectionContext) context ).getModifies();
        }
    }
}