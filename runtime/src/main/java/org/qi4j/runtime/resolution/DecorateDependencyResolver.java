package org.qi4j.runtime.resolution;

import java.util.Map;
import org.qi4j.dependency.DependencyInjectionContext;
import org.qi4j.dependency.DependencyResolution;
import org.qi4j.dependency.DependencyResolver;
import org.qi4j.dependency.InjectionKey;
import org.qi4j.dependency.ObjectDependencyInjectionContext;
import org.qi4j.model.DependencyKey;

/**
 * TODO
 */
public class DecorateDependencyResolver
    implements DependencyResolver
{
    KeyMatcher matcher;

    public DecorateDependencyResolver()
    {
        matcher = new KeyMatcher();
    }

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

        public Object getDependencyInjection( DependencyInjectionContext context )
        {
            if( context instanceof ObjectDependencyInjectionContext )
            {
                ObjectDependencyInjectionContext mixinContext = (ObjectDependencyInjectionContext) context;
                Map<InjectionKey, Object> decorate = mixinContext.getDecorate();
                for( Map.Entry<InjectionKey, Object> entry : decorate.entrySet() )
                {
                    if( matcher.matches( key, entry.getKey() ) )
                    {
                        return entry.getValue();
                    }
                }

                return null;
            }

            return null;
        }
    }
}