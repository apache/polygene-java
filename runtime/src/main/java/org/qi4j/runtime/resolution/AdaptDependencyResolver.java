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
public class AdaptDependencyResolver
    implements DependencyResolver
{
    KeyMatcher matcher;

    public AdaptDependencyResolver()
    {
        matcher = new KeyMatcher();
    }

    public DependencyResolution resolveDependency( DependencyKey key )
    {
        return new AdaptDependencyResolution( key );
    }

    private class AdaptDependencyResolution implements DependencyResolution
    {
        private DependencyKey key;

        public AdaptDependencyResolution( DependencyKey key )
        {
            this.key = key;
        }

        public Object getDependencyInjection( DependencyInjectionContext context )
        {
            if( context instanceof ObjectDependencyInjectionContext )
            {
                ObjectDependencyInjectionContext mixinContext = (ObjectDependencyInjectionContext) context;
                Map<InjectionKey, Object> adapt = mixinContext.getAdapt();
                for( Map.Entry<InjectionKey, Object> entry : adapt.entrySet() )
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