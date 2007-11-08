package org.qi4j.runtime.resolution;

import java.util.Map;
import org.qi4j.PropertyValue;
import org.qi4j.dependency.DependencyInjectionContext;
import org.qi4j.dependency.DependencyResolution;
import org.qi4j.dependency.DependencyResolver;
import org.qi4j.dependency.InjectionKey;
import org.qi4j.dependency.ObjectDependencyInjectionContext;
import org.qi4j.model.DependencyKey;

/**
 * TODO
 */
public class PropertyDependencyResolver
    implements DependencyResolver
{
    KeyMatcher matcher;

    public PropertyDependencyResolver()
    {
        this.matcher = new KeyMatcher();
    }

    public DependencyResolution resolveDependency( DependencyKey key )
    {
        return new PropertyDependencyResolution( key );
    }

    private class PropertyDependencyResolution implements DependencyResolution
    {
        private DependencyKey key;

        public PropertyDependencyResolution( DependencyKey key )
        {
            this.key = key;
        }

        public Object getDependencyInjection( DependencyInjectionContext context )
        {
            if( context instanceof ObjectDependencyInjectionContext )
            {
                ObjectDependencyInjectionContext mixinContext = (ObjectDependencyInjectionContext) context;

                // Check whether one or more properties should be injected
                if( key.getDependencyType().equals( PropertyValue.class ) && key.getRawType().equals( Iterable.class ) )
                {
                    return mixinContext.getProperties().values();
                }
                else
                {
                    for( Map.Entry<InjectionKey, PropertyValue> entry : mixinContext.getProperties().entrySet() )
                    {
                        if( matcher.matches( key, entry.getKey() ) )
                        {
                            return entry.getValue().getValue();
                        }

                    }
                }

                return null;
            }

            return null;
        }
    }
}