package org.qi4j.runtime.resolution;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.model.DependencyKey;
import org.qi4j.spi.dependency.DependencyResolution;
import org.qi4j.spi.dependency.DependencyResolver;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * TODO
 */
public class DependencyResolverDelegator
    implements DependencyResolver
{
    Map<Class<? extends Annotation>, DependencyResolver> resolvers;

    public DependencyResolverDelegator()
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

    public void setDependencyResolver( Class<? extends Annotation> annotationClass, DependencyResolver resolver )
    {
        resolvers.put( annotationClass, resolver );
    }
}
