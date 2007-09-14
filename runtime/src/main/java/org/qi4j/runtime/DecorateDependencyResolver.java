package org.qi4j.runtime;

import java.util.Collections;
import org.qi4j.api.DependencyInjectionContext;
import org.qi4j.api.DependencyKey;
import org.qi4j.api.DependencyResolution;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.MixinDependencyInjectionContext;

/**
 * TODO
 */
public final class DecorateDependencyResolver
    implements DependencyResolver
{
    public DependencyResolution resolveDependency( DependencyKey key )
    {
        return new DecorateDependencyResolution( key );
    }

    private class DecorateDependencyResolution implements DependencyResolution
    {
        private DependencyKey key;

        public DecorateDependencyResolution( DependencyKey key )
        {
            this.key = key;
        }

        public Iterable getDependencyInjection( DependencyInjectionContext context )
        {
            if( context instanceof MixinDependencyInjectionContext )
            {
                MixinDependencyInjectionContext mixinContext = (MixinDependencyInjectionContext) context;
                Iterable decorate = mixinContext.getDecorate();
                for( Object decoratable : decorate )
                {
                    if( key.getDependencyType().isInstance( decoratable ) )
                    {
                        return Collections.singletonList( decoratable );
                    }
                }

                return Collections.EMPTY_LIST;
            }

            return Collections.EMPTY_LIST;
        }
    }
}