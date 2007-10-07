package org.qi4j.runtime.resolution;

import org.qi4j.spi.dependency.DependencyResolver;

/**
 * TODO
 */
public abstract class FragmentModelResolver<K extends FragmentResolution>
    extends ObjectModelResolver<K>
{
    public FragmentModelResolver( DependencyResolver dependencyResolver )
    {
        super( dependencyResolver );
    }

}