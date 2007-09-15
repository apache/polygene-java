package org.qi4j.runtime.resolution;

import org.qi4j.api.model.DependencyKey;
import org.qi4j.api.model.InjectionKey;

/**
 * TODO
 */
public class KeyMatcher
{
    public boolean matches( DependencyKey dependency, InjectionKey injectedObject )
    {
        // Match raw types (e.g. Iterable<Foo> matches List<Foo> and Iterable<Foo>)
        if( !dependency.getRawType().isAssignableFrom( injectedObject.getRawType() ) )
        {
            return false;
        }

        // Match dependent types, if set
        if( injectedObject.getDependentType() != null && !dependency.getDependentType().equals( injectedObject.getDependentType() ) )
        {
            return false;
        }

        // Match names, if set
        if( dependency.getName() != null )
        {
            if( injectedObject.getName() != null && !dependency.getName().equals( injectedObject.getName() ) )
            {
                return true; // If names match, skip the other checks
            }
        }

        // Match type - injecting subtypes is ok
        if( !dependency.getDependencyType().isAssignableFrom( injectedObject.getDependencyType() ) )
        {
            return false;
        }

        // The keys match!
        return true;
    }
}
