package org.qi4j.runtime.injection;

import java.util.function.Function;

/**
 * TODO
 */
public interface Dependencies
{
    public static Function<Dependencies, Iterable<DependencyModel>> DEPENDENCIES_FUNCTION = new Function<Dependencies, Iterable<DependencyModel>>()
    {
        @Override
        public Iterable<DependencyModel> apply( Dependencies dependencies )
        {
            return dependencies.dependencies();
        }
    };

    Iterable<DependencyModel> dependencies();
}
