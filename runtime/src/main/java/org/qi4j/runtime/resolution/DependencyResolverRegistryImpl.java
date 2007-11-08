package org.qi4j.runtime.resolution;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.dependency.DependencyResolution;
import org.qi4j.dependency.DependencyResolver;
import org.qi4j.dependency.DependencyResolverRegistry;
import org.qi4j.dependency.InvalidDependencyException;
import org.qi4j.model.DependencyKey;

/**
 * TODO
 */
public class DependencyResolverRegistryImpl
    implements DependencyResolver, DependencyResolverRegistry
{
    Map<Class<? extends Annotation>, DependencyResolver> resolvers;

    public DependencyResolverRegistryImpl()
    {
        resolvers = new HashMap<Class<? extends Annotation>, DependencyResolver>();
    }

    public DependencyResolution resolveDependency( DependencyKey key )
        throws InvalidDependencyException
    {
        Class<? extends Annotation> annotationType = key.getAnnotationType();
        DependencyResolver annotationResolver = resolvers.get( annotationType );
        if( annotationResolver == null )
        {
            return null;
        }
        else
        {
            return annotationResolver.resolveDependency( key );
        }
    }

    public DependencyResolver getDependencyResolver( Class<? extends Annotation> annotationClass )
    {
        return resolvers.get( annotationClass );
    }

    public void setDependencyResolver( Class<? extends Annotation> annotationClass, DependencyResolver resolver )
    {
        resolvers.put( annotationClass, resolver );
    }
}
