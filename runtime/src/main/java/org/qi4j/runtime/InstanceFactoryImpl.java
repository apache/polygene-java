package org.qi4j.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.runtime.resolution.ConstructorDependencyResolution;
import org.qi4j.runtime.resolution.FieldDependencyResolution;
import org.qi4j.runtime.resolution.MethodDependencyResolution;
import org.qi4j.runtime.resolution.ObjectResolution;
import org.qi4j.runtime.resolution.ParameterDependencyResolution;
import org.qi4j.spi.dependency.DependencyInjectionContext;

/**
 * TODO
 */
public class InstanceFactoryImpl
    implements InstanceFactory
{
    public <K> K newInstance( ObjectResolution<K> objectResolution, DependencyInjectionContext context )
        throws CompositeInstantiationException
    {
        // New instance
        K instance;
        try
        {
            // Constructor injection
            ConstructorDependencyResolution cdr = objectResolution.getConstructorResolutions().iterator().next();

            Constructor constructor = cdr.getConstructorDependency().getConstructor();
            Object[] parameters = new Object[constructor.getParameterTypes().length];

            // Resolve constructor dependencies
            Iterable<ParameterDependencyResolution> dr = cdr.getParameterDependencyResolutions();
            int i = 0;
            for( ParameterDependencyResolution dependencyResolution : dr )
            {
                Object parameter = dependencyResolution.getDependencyResolution().getDependencyInjection( context );

                if( parameter == null && !dependencyResolution.getParameter().isOptional() )
                {
                    throw new CompositeInstantiationException( "Non-optional @" + dependencyResolution.getParameter().getKey().getAnnotationType().getSimpleName() + " parameter " + ( i + 1 ) + " of type " + dependencyResolution.getParameter().getKey().getDependencyType() + " in class " + objectResolution.getObjectModel().getModelClass().getName() + " was null" );
                }

                Class parameterType = constructor.getParameterTypes()[ i ];
                parameters[ i ] = getInjectedValue( parameter, parameterType );
                i++;
            }

            // Invoke constructor
            instance = objectResolution.getObjectModel().getModelClass().cast( constructor.newInstance( parameters ) );

        }
        catch( CompositeInstantiationException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( "Could not instantiate class " + objectResolution.getObjectModel().getModelClass().getName(), e );
        }

        // Inject fields and methods
        inject( instance, objectResolution, context );

        return instance;
    }

    public <K> void inject( K instance, ObjectResolution<K> objectResolution, DependencyInjectionContext context )
        throws CompositeInstantiationException
    {
        try
        {
            // Field injection
            injectFields( objectResolution, context, instance );

            // Method injection
            injectMethods( objectResolution, context, instance );
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( "Could not inject object of class " + objectResolution.getObjectModel().getModelClass().getName(), e );
        }
    }

    private <K> void injectFields( ObjectResolution<K> fragmentResolution, DependencyInjectionContext context, K instance )
        throws IllegalAccessException
    {
        Iterable<FieldDependencyResolution> fieldResolutions = fragmentResolution.getFieldResolutions();
        for( FieldDependencyResolution fieldResolution : fieldResolutions )
        {
            Object value = fieldResolution.getDependencyResolution().getDependencyInjection( context );
            Field field = fieldResolution.getFieldDependency().getField();
            value = getInjectedValue( value, field.getType() );
            if( value != null )
            {
                try
                {
                    field.set( instance, value );
                }
                catch( IllegalArgumentException e )
                {
                    throw new CompositeInstantiationException( "Could not set field " + field.getName() + " in " + field.getDeclaringClass().getName() + " to value of type " + value.getClass().getName() );
                }
                catch( IllegalAccessException e )
                {
                    e.printStackTrace();
                }
            }
            else
            {
                if( !fieldResolution.getFieldDependency().isOptional() )
                {
                    throw new CompositeInstantiationException( "Non-optional @" + fieldResolution.getFieldDependency().getKey().getAnnotationType().getSimpleName() + " field " + fieldResolution.getFieldDependency().getField().getName() + " of type " + fieldResolution.getFieldDependency().getKey().getDependencyType() + " in class " + fragmentResolution.getObjectModel().getModelClass().getName() + " was null" );
                }
            }
        }
    }

    private <K> void injectMethods( ObjectResolution<K> fragmentResolution, DependencyInjectionContext context, K instance )
        throws InstantiationException, IllegalAccessException, InvocationTargetException
    {
        Iterable<MethodDependencyResolution> methodResolutions = fragmentResolution.getMethodResolutions();
        for( MethodDependencyResolution methodResolution : methodResolutions )
        {
            Method method = methodResolution.getMethodDependency().getMethod();
            Object[] parameters = new Object[]{ method.getParameterTypes().length };

            // Resolve parameter dependencies
            Iterable<ParameterDependencyResolution> dr = methodResolution.getParameterDependencyResolutions();
            int i = 0;
            for( ParameterDependencyResolution dependencyResolution : dr )
            {
                Object parameter = dependencyResolution.getDependencyResolution().getDependencyInjection( context );
                Class parameterType = method.getParameterTypes()[ i ];
                parameters[ i ] = getInjectedValue( parameter, parameterType );

                i++;
            }

            // Invoke method
            method.invoke( instance, parameters );
        }
    }

    private <K> Object getInjectedValue( Object injectionResult, Class type )
    {
        if( injectionResult == null )
        {
            return null;
        }

        if( Iterable.class.equals( type ) && !Iterable.class.isAssignableFrom( injectionResult.getClass() ) )
        {
            return Collections.singleton( injectionResult );
        }

        return injectionResult;
    }
}
