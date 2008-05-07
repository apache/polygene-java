package org.qi4j.runtime.composite;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Iterator;
import org.qi4j.composite.Initializable;
import org.qi4j.composite.InstantiationException;
import org.qi4j.spi.composite.AbstractBinding;
import org.qi4j.spi.composite.ConstructorBinding;
import org.qi4j.spi.composite.FieldBinding;
import org.qi4j.spi.composite.MethodBinding;
import org.qi4j.spi.composite.ParameterBinding;
import org.qi4j.spi.injection.InjectionBinding;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;

/**
 * TODO
 */
public final class InstanceFactoryImpl
    implements InstanceFactory
{
    public Object newInstance( AbstractBinding abstractBinding, InjectionContext context )
        throws InstantiationException
    {
        // New instance
        Object instance;
        try
        {
            // Constructor injection
            ConstructorBinding constructorBinding = abstractBinding.getConstructorBinding();

            Constructor constructor = constructorBinding.getConstructorResolution().getConstructorModel().getConstructor();
            Object[] parameters = new Object[constructor.getParameterTypes().length];

            // Inject constructor parameters
            Iterable<ParameterBinding> parameterBindings = constructorBinding.getParameterBindings();
            int i = 0;
            for( ParameterBinding parameterBinding : parameterBindings )
            {
                InjectionBinding binding = parameterBinding.getInjectionBinding();
                InjectionProvider injectionProvider = binding.getInjectionProvider();
                Object parameter = injectionProvider.provideInjection( context );

                if( parameter == null && !parameterBinding.getInjectionBinding().getInjectionResolution().getInjectionModel().isOptional() )
                {
                    throw new InstantiationException( "Non-optional @" + parameterBinding.getInjectionBinding().getInjectionResolution().getInjectionModel().getInjectionAnnotationType().getSimpleName() + " parameter " + ( i + 1 ) + " of type " + parameterBinding.getInjectionBinding().getInjectionResolution().getInjectionModel().getInjectionType() + " in class " + abstractBinding.getAbstractResolution().getAbstractModel().getModelClass().getName() + " was null" );
                }

                Class parameterType = constructor.getParameterTypes()[ i ];
                parameters[ i ] = getInjectedValue( parameter, parameterType );
                i++;
            }

            // Invoke constructor
            instance = constructor.newInstance( parameters );
        }
        catch( InstantiationException e )
        {
            throw e;
        }
        catch( InvocationTargetException e )
        {
            throw new InstantiationException( "Could not instantiate class " + abstractBinding.getAbstractResolution().getAbstractModel().getModelClass().getName(), e.getTargetException() );
        }
        catch( Exception e )
        {
            throw new InstantiationException( "Could not instantiate class " + abstractBinding.getAbstractResolution().getAbstractModel().getModelClass().getName(), e );
        }

        // Inject fields and methods
        inject( instance, abstractBinding, context );

        return instance;
    }

    public void inject( Object instance, AbstractBinding abstractBinding, InjectionContext context )
        throws InstantiationException
    {
        try
        {
            // Field injection
            injectFields( abstractBinding, context, instance );

            // Method injection
            injectMethods( abstractBinding, context, instance );
        }
        catch( Exception e )
        {
            throw new InstantiationException( "Could not inject object of class " + instance.getClass().getName(), e );
        }

        // Check for Initializable
        if( instance instanceof Initializable )
        {
            ( (Initializable) instance ).initialize();
        }
    }

    private void injectFields( AbstractBinding binding, InjectionContext context, Object instance )
        throws IllegalAccessException
    {
        Iterable<FieldBinding> fieldBindings = binding.getFieldBindings();
        for( FieldBinding fieldBinding : fieldBindings )
        {
            InjectionBinding injectionBinding = fieldBinding.getInjectionBinding();
            if( injectionBinding != null )
            {
                InjectionProvider injectionProvider = injectionBinding.getInjectionProvider();
                Object value = injectionProvider.provideInjection( context );
                Field field = fieldBinding.getFieldResolution().getFieldModel().getField();
                value = getInjectedValue( value, field.getType() );
                if( value != null )
                {
                    try
                    {
                        field.set( instance, value );
                    }
                    catch( IllegalArgumentException e )
                    {
                        String valueTypeName = value.getClass().getName();
                        if( value instanceof Proxy )
                        {
                            valueTypeName = value.getClass().getInterfaces()[ 0 ].getName();
                        }
                        throw new InstantiationException( "Could not set field " + field.getName() + " of type " + field.getType().getName() + " in " + field.getDeclaringClass().getName() + " to value of type " + valueTypeName );
                    }
                    catch( IllegalAccessException e )
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    if( !injectionBinding.getInjectionResolution().getInjectionModel().isOptional() )
                    {
                        throw new InstantiationException( "Non-optional @" + injectionBinding.getInjectionResolution().getInjectionModel().getInjectionAnnotationType().getSimpleName() + " field " + fieldBinding.getFieldResolution().getFieldModel().getField().getName() + " of type " + fieldBinding.getFieldResolution().getFieldModel().getField().getGenericType() + " in class " + instance.getClass().getName() + " was null" );
                    }
                }
            }
        }
    }

    private void injectMethods( AbstractBinding abstractBinding, InjectionContext context, Object instance )
        throws java.lang.InstantiationException, IllegalAccessException, InvocationTargetException
    {
        Iterable<MethodBinding> methodBindings = abstractBinding.getInjectedMethodsBindings();
        for( MethodBinding methodBinding : methodBindings )
        {
            Method method = methodBinding.getMethodResolution().getMethodModel().getMethod();
            Object[] parameters = new Object[]{ method.getParameterTypes().length };

            // Resolve parameter dependencies
            Iterable<ParameterBinding> parameterBindings = methodBinding.getParameterBindings();
            int i = 0;
            for( ParameterBinding parameterBinding : parameterBindings )
            {
                InjectionProvider injectionProvider = parameterBinding.getInjectionBinding().getInjectionProvider();
                Object parameter = injectionProvider.provideInjection( context );
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

        if( injectionResult instanceof Iterable && !Iterable.class.isAssignableFrom( type ) && !type.isInstance( injectionResult ) )
        {
            Iterator iterator = ( (Iterable) injectionResult ).iterator();
            if( iterator.hasNext() )
            {
                return iterator.next();
            }
            else
            {
                return null;
            }
        }

        return injectionResult;
    }
}
