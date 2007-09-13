package org.qi4j.runtime;

import java.util.Collections;
import org.qi4j.api.DependencyInjectionContext;
import org.qi4j.api.DependencyKey;
import org.qi4j.api.DependencyResolution;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.InvalidDependencyException;

/**
 * TODO
 */
public class ThisAsDependencyResolver
    implements DependencyResolver
{
    public DependencyResolution resolveDependency( DependencyKey key )
        throws InvalidDependencyException
    {
        // Check if the composite implements the desired type
        if (key.getRawClass().isAssignableFrom( key.getCompositeType()))
        {
            return new ThisAsDependencyResolution();
        } else
            throw new InvalidDependencyException("Composite "+key.getCompositeType()+" does not implement @ThisAs type "+key.getDependencyType()+" in fragment "+key.getFragmentType());
    }

    private class ThisAsDependencyResolution implements DependencyResolution
    {
        public Iterable getDependencyInjection( DependencyInjectionContext context )
        {
            return Collections.singleton( context.getThisAs());
        }
    }
}
