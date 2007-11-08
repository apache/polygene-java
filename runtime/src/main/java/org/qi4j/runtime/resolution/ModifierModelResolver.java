package org.qi4j.runtime.resolution;

import org.qi4j.dependency.DependencyResolver;

/**
 * TODO
 */
public abstract class ModifierModelResolver<K extends ModifierResolution>
    extends FragmentModelResolver<K>
{
    public ModifierModelResolver( DependencyResolver dependencyResolver )
    {
        super( dependencyResolver );
    }

}
