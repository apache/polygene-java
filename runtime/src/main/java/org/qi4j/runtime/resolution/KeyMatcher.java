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

        if( !dependency.getRawType().isAssignableFrom( injectedObject.getRawType() ) )
        {
            // Match raw types (e.g. Iterable<Foo> matches List<Foo> and Iterable<Foo>)
            return false;
        }

        // Match dependent types, if set
        if( injectedObject.getDependentType() != null && !dependency.getDependentType().equals( injectedObject.getDependentType() ) )
        {
            if( !dependency.getDependentType().isAssignableFrom( injectedObject.getDependentType() ) )
            {
                return false;
            }
        }

        // Match names, if set
        if( injectedObject.getName() != null )
        {
            // if injection key has a name, the name must match dependency key
            if( dependency.getName() == null || !dependency.getName().equals( injectedObject.getName() ) )
            {
                return false;
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
