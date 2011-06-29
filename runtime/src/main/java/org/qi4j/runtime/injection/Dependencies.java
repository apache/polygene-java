package org.qi4j.runtime.injection;

import org.qi4j.functional.Function;

/**
 * TODO
 */
public interface Dependencies
{
    public static Function<Dependencies, Iterable<DependencyModel>> DEPENDENCIES_FUNCTION = new Function<Dependencies, Iterable<DependencyModel>>()
    {
        @Override
        public Iterable<DependencyModel> map( Dependencies dependencies )
        {
            return dependencies.dependencies();
        }
    };

    Iterable<DependencyModel> dependencies();
}
