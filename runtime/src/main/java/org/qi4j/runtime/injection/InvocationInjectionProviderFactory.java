package org.qi4j.runtime.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import org.qi4j.runtime.composite.qi.DependencyModel;
import org.qi4j.runtime.composite.qi.InjectionContext;
import org.qi4j.runtime.composite.qi.InjectionProvider;
import org.qi4j.runtime.composite.qi.InjectionProviderFactory;
import org.qi4j.runtime.composite.qi.Resolution;
import org.qi4j.spi.injection.InjectionProviderException;
import org.qi4j.spi.injection.InvalidInjectionException;

/**
 * TODO
 */
public final class InvocationInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel ) throws InvalidInjectionException
    {
        Class injectionClass = dependencyModel.injectionClass();
        if( injectionClass.equals( Method.class ) ||
            injectionClass.equals( AnnotatedElement.class ) ||
            Annotation.class.isAssignableFrom( injectionClass ) )
        {
            return new InvocationDependencyResolution( resolution, dependencyModel );
        }
        else
        {
            throw new InvalidInjectionException( "Invalid injection type " + injectionClass + " in " + dependencyModel.injectedClass().getName() );
        }
    }

    private class InvocationDependencyResolution implements InjectionProvider
    {
        private Resolution resolution;
        private DependencyModel dependencyModel;

        public InvocationDependencyResolution( Resolution resolution, DependencyModel dependencyModel )
        {
            this.resolution = resolution;
            this.dependencyModel = dependencyModel;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            Class injectedClass = dependencyModel.injectedClass();
            if( injectedClass.equals( Method.class ) )
            {
                // This needs to be updated to handle Apply and annotation aggregation correctly
                return resolution.method().method();
            }
            else if( injectedClass.equals( AnnotatedElement.class ) )
            {
                return resolution.method().annotatedElement();
            }
            else
            {
                return resolution.method().annotatedElement().getAnnotation( injectedClass );
            }
        }
    }
}