package org.qi4j.runtime.resolution;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.qi4j.dependency.DependencyInjectionContext;
import org.qi4j.dependency.DependencyResolution;
import org.qi4j.dependency.DependencyResolver;
import org.qi4j.dependency.FragmentDependencyInjectionContext;
import org.qi4j.dependency.InvalidDependencyException;
import org.qi4j.model.DependencyKey;
import org.qi4j.model.FragmentDependencyKey;

/**
 * TODO
 */
public class ThisCompositeAsDependencyResolver
    implements DependencyResolver
{
    public DependencyResolution resolveDependency( DependencyKey key )
        throws InvalidDependencyException
    {
        if( key instanceof FragmentDependencyKey )
        {
            FragmentDependencyKey fragmentKey = (FragmentDependencyKey) key;
            return new ThisCompositeAsDependencyResolution( key.getRawType() );

/* TODO Needs to be fixed to support internal mixins
            // Check if the composite implements the desired type
            if( key.getRawType().isAssignableFrom( fragmentKey.getCompositeType() ) )
            {
                return new ThisCompositeAsDependencyResolution(key.getRawType());
            }
            else
            {
                throw new InvalidDependencyException( "Composite " + fragmentKey.getCompositeType() + " does not implement @ThisCompositeAs type " + key.getDependencyType() + " in fragment " + key.getDependentType() );
            }
*/
        }
        else
        {
            throw new InvalidDependencyException( "Object " + key.getDependentType() + " may not use @ThisCompositeAs" );
        }
    }

    private class ThisCompositeAsDependencyResolution implements DependencyResolution
    {
        Class type;

        public ThisCompositeAsDependencyResolution( Class type )
        {
            this.type = type;
        }

        public Object getDependencyInjection( DependencyInjectionContext context )
        {
            FragmentDependencyInjectionContext fragmentContext = (FragmentDependencyInjectionContext) context;
            InvocationHandler handler = fragmentContext.getThisCompositeAs();
            return Proxy.newProxyInstance( type.getClassLoader(), new Class[]{ type }, handler );
        }
    }
}
