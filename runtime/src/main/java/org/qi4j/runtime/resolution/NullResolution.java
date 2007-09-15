package org.qi4j.runtime.resolution;

import org.qi4j.spi.dependency.DependencyInjectionContext;
import org.qi4j.spi.dependency.DependencyResolution;

/**
 * TODO
 */
public class NullResolution
    implements DependencyResolution
{
    public Object getDependencyInjection( DependencyInjectionContext context )
    {
        return null;
    }
}
