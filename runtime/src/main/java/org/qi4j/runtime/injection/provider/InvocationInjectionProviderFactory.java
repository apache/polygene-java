package org.qi4j.runtime.injection.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.composite.CompositeMethodModel;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;

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
            final CompositeMethodModel methodModel = resolution.method();
            if( injectedClass.equals( Method.class ) )
            {
                // This needs to be updated to handle Apply and annotation aggregation correctly
                return methodModel.method();
            }

            final AnnotatedElement method = methodModel.annotatedElement();
            if( injectedClass.equals( AnnotatedElement.class ) )
            {
                return method;
            }
            final Annotation annotation = method.getAnnotation( injectedClass );
            if ( annotation !=null)
            {
                return annotation;
            }
            if (dependencyModel.injectionType() instanceof Class<?>) {
                return method.getAnnotation( (Class<Annotation>) dependencyModel.injectionType() );
            }
            return null;
        }
    }
}