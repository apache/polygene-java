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
                ( InvocationHandler.class.isAssignableFrom( key.getDependentType() ) && key.getDependencyType().equals( InvocationHandler.class ) ) )
            {
                return new ModifiesDependencyResolution();
            }
            else
            {
                throw new InvalidDependencyException( "Composite " + fragmentKey.getCompositeType() + " does not implement @AssertionFor type " + key.getDependencyType() + " in modifier " + key.getDependentType() );
            }
        }
        else
        {
            throw new InvalidDependencyException( "Object " + key.getDependentType() + " may not use @AssertionFor" );
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