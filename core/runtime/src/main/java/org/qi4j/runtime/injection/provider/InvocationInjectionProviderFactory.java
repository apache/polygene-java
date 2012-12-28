package org.qi4j.runtime.injection.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.composite.CompositeMethodModel;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;

/**
 * JAVADOC
 */
public final class InvocationInjectionProviderFactory
    implements InjectionProviderFactory
{
    @Override
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        Class injectionClass = Classes.RAW_CLASS.map( dependencyModel.injectionType() );
        if( injectionClass.equals( Method.class ) ||
            injectionClass.equals( AnnotatedElement.class ) ||
            injectionClass.equals( Iterable.class ) ||
            Annotation.class.isAssignableFrom( injectionClass ) )
        {
            return new InvocationDependencyResolution( resolution, dependencyModel );
        }
        else
        {
            String injectedTo = dependencyModel.injectedClass().getName();
            throw new InvalidInjectionException( "Invalid injection type " + injectionClass + " in " + injectedTo );
        }
    }

    private static class InvocationDependencyResolution
        implements InjectionProvider
    {
        private final Resolution resolution;
        private final DependencyModel dependencyModel;

        public InvocationDependencyResolution( Resolution resolution, DependencyModel dependencyModel )
        {
            this.resolution = resolution;
            this.dependencyModel = dependencyModel;
        }

        @Override
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
            if( dependencyModel.injectionType() instanceof ParameterizedType )
            {
                ParameterizedType injectionType = (ParameterizedType) dependencyModel.injectionType();
                Type rawType = injectionType.getRawType();
                Type[] actualTypeArguments = injectionType.getActualTypeArguments();
                boolean isAnIterable = rawType.equals( Iterable.class );
                boolean haveOneGenericType = actualTypeArguments.length == 1;
                boolean thatIsOfTypeMethod = actualTypeArguments[ 0 ].equals( Method.class );
                if( isAnIterable && haveOneGenericType && thatIsOfTypeMethod )
                {
                    Class<?> injectedClass = dependencyModel.injectedClass();
                    Iterable<Method> result = methodModel.invocationsFor( injectedClass );
                    return result;
                }
            }
            return null;
        }
    }
}