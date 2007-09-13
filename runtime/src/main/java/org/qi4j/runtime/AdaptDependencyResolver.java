package org.qi4j.runtime;

import java.util.Collections;
import java.util.List;
import org.qi4j.api.DependencyInjectionContext;
import org.qi4j.api.DependencyKey;
import org.qi4j.api.DependencyResolution;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.MixinDependencyInjectionContext;

/**
 * TODO
 */
public class AdaptDependencyResolver
    implements DependencyResolver
{
    public DependencyResolution resolveDependency( DependencyKey key )
    {
        return new AdaptDependencyResolution(key);
    }

    private class AdaptDependencyResolution implements DependencyResolution
    {
        private DependencyKey key;

        public AdaptDependencyResolution( DependencyKey key )
        {
            this.key = key;
        }

        public Iterable getDependencyInjection( DependencyInjectionContext context )
        {
            if (context instanceof MixinDependencyInjectionContext )
            {
                MixinDependencyInjectionContext mixinContext = ( MixinDependencyInjectionContext ) context;
                Iterable adapt = mixinContext.getAdapt();
                for( Object adaptable : adapt )
                {
                    if (key.getDependencyType().isInstance( adaptable))
                        return Collections.singletonList( adaptable);
                }

                return Collections.EMPTY_LIST;
            }

            return Collections.EMPTY_LIST;
        }
    }
}