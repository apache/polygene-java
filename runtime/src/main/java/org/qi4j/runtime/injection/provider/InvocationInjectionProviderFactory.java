package org.qi4j.runtime.injection.provider;

import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.composite.CompositeMethodModel;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * JAVADOC
 */
public final class InvocationInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        Class injectionClass = Classes.RAW_CLASS.map( dependencyModel.injectionType() );
        if( injectionClass.equals( Method.class ) ||
            injectionClass.equals( AnnotatedElement.class ) ||
            Annotation.class.isAssignableFrom( injectionClass ) )
        {
            return new InvocationDependencyResolution( resolution, dependencyModel );
        }
        else
        {
            throw new InvalidInjectionException( "Invalid injection type " + injectionClass + " in " + dependencyModel.injectedClass()
                .getName() );
        }
    }

    private class InvocationDependencyResolution
        implements InjectionProvider
    {
        private final Resolution resolution;
        private final DependencyModel dependencyModel;

        public InvocationDependencyResolution( Resolution resolution, DependencyModel dependencyModel )
        {
            this.resolution = resolution;
            this.dependencyModel = dependencyModel;
        }

        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            Class injectionClass = Classes.RAW_CLASS.map( dependencyModel.injectionType() );
            final CompositeMethodModel methodModel = resolution.method();
            if( injectionClass.equals( Method.class ) )
            {
                return methodModel.method();
            }

            final AnnotatedElement annotatedElement = methodModel.annotatedElement();
            if( injectionClass.equals( AnnotatedElement.class ) )
            {
                return annotatedElement;
            }
            final Annotation annotation = annotatedElement.getAnnotation( injectionClass );
            if( annotation != null )
            {
                return annotation;
            }
            if( dependencyModel.injectionType() instanceof Class<?> )
            {
                return annotatedElement.getAnnotation( (Class<Annotation>) dependencyModel.injectionType() );
            }
            return null;
        }
    }
}