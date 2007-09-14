package org.qi4j.runtime;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.DependencyKey;
import org.qi4j.api.DependencyResolution;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.InvalidDependencyException;

/**
 * TODO
 */
public final class DependencyResolverDelegator
    implements DependencyResolver
{
    Map<Class<? extends Annotation>, DependencyResolver> resolvers = new HashMap<Class<? extends Annotation>, DependencyResolver>();

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
