package org.qi4j.runtime.resolution;

import java.lang.reflect.Method;
import org.qi4j.api.InvocationContext;
import org.qi4j.api.model.DependencyKey;
import org.qi4j.spi.dependency.DependencyInjectionContext;
import org.qi4j.spi.dependency.DependencyResolution;
import org.qi4j.spi.dependency.DependencyResolver;
import org.qi4j.spi.dependency.InvalidDependencyException;
import org.qi4j.spi.dependency.ModifierDependencyInjectionContext;

/**
 * TODO
 */
public class ModifierDependencyResolver
    implements DependencyResolver
{
    public DependencyResolution resolveDependency( DependencyKey key )
        throws InvalidDependencyException
    {
        if( key.getDependencyType().equals( Method.class ) || key.getDependencyType().equals( InvocationContext.class ) )
        {
            return new ModifierDependencyResolution( key );
        }
        else
        {
            throw new InvalidDependencyException( "Invalid dependency type " + key.getDependencyType() + " in " + key.getDependentType() );
        }
    }

    private class ModifierDependencyResolution implements DependencyResolution
    {
        private DependencyKey key;

        public ModifierDependencyResolution( DependencyKey key )
        {
            this.key = key;
        }

        public Object getDependencyInjection( DependencyInjectionContext context )
        {
            ModifierDependencyInjectionContext modifierContext = (ModifierDependencyInjectionContext) context;

            if( key.getDependencyType().equals( Method.class ) )
            {
                // This needs to be updated to handle Apply and annotation aggregation correctly
                return modifierContext.getMethod();
            }
            else if( key.getDependencyType().equals( InvocationContext.class ) )
            {
                return modifierContext.getInvocationContext();
            }
            else
            {
                return null;
            }
        }
    }
}