package org.qi4j.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import org.qi4j.CompositeInstantiationException;
import org.qi4j.spi.composite.ConstructorBinding;
import org.qi4j.spi.composite.FieldBinding;
import org.qi4j.spi.composite.MethodBinding;
import org.qi4j.spi.composite.ObjectBinding;
import org.qi4j.spi.composite.ParameterBinding;
import org.qi4j.spi.dependency.InjectionBinding;
import org.qi4j.spi.dependency.InjectionContext;
import org.qi4j.spi.dependency.InjectionProvider;

/**
 * TODO
 */
public class InstanceFactoryImpl
    implements InstanceFactory
{
    public Object newInstance( ObjectBinding objectBinding, InjectionContext context )
        throws CompositeInstantiationException
    {
        // New instance
        Object instance;
        try
        {
            // Constructor injection
            ConstructorBinding constructorBinding = objectBinding.getConstructorBinding();

            Constructor constructor = constructorBinding.getConstructorResolution().getConstructorModel().getConstructor();
            Object[] parameters = new Object[constructor.getParameterTypes().length];

            // Inject constructor parameters
            Iterable<ParameterBinding> parameterBindings = constructorBinding.getParameterBindings();
            int i = 0;
            for( ParameterBinding parameterBinding : parameterBindings )
            {
                InjectionProvider injectionProvider = parameterBinding.getInjectionBinding().getInjectionProvider();
                Object parameter = injectionProvider.provideInjection( context );

                if( parameter == null && !parameterBinding.getInjectionBinding().getInjectionResolution().getInjectionModel().isOptional() )
                {
                    throw new CompositeInstantiationException( "Non-optional @" + parameterBinding.getInjectionBinding().getInjectionResolution().getInjectionModel().getInjectionAnnotationType().getSimpleName() + " parameter " + ( i + 1 ) + " of type " + parameterBinding.getInjectionBinding().getInjectionResolution().getInjectionModel().getInjectionType() + " in class " + objectBinding.getObjectResolution().getObjectModel().getModelClass().getName() + " was null" );
                }

                Class parameterType = constructor.getParameterTypes()[ i ];
                parameters[ i ] = getInjectedValue( parameter, parameterType );
                i++;
            }

            // Invoke constructor
            instance = constructor.newInstance( parameters );
        }
        catch( CompositeInstantiationException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( "Could not instantiate class " + objectBinding.getObjectResolution().getObjectModel().getModelClass().getName(), e );
        }

        // Inject fields and methods
        inject( instance, objectBinding, context );

        return instance;
    }

    public void inject( Object instance, ObjectBinding objectBinding, InjectionContext context )
        throws CompositeInstantiationException
    {
        try
        {
            // Field injection
            injectFields( objectBinding, context, instance );

            // Method injection
            injectMethods( objectBinding, context, instance );
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( "Could not inject object of class " + instance.getClass().getName(), e );
        }
    }

    private void injectFields( ObjectBinding binding, InjectionContext context, Object instance )
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
                        throw new CompositeInstantiationException( "Could not set field " + field.getName() + " in " + field.getDeclaringClass().getName() + " to value of type " + value.getClass().getName() );
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
                        throw new CompositeInstantiationException( "Non-optional @" + injectionBinding.getInjectionResolution().getInjectionModel().getInjectionAnnotationType().getSimpleName() + " field " + fieldBinding.getFieldResolution().getFieldModel().getField().getName() + " of type " + fieldBinding.getFieldResolution().getFieldModel().getField().getGenericType() + " in class " + instance.getClass().getName() + " was null" );
                    }
                }
            }
        }
    }

    private void injectMethods( ObjectBinding objectBinding, InjectionContext context, Object instance )
        throws InstantiationException, IllegalAccessException, InvocationTargetException
    {
        Iterable<MethodBinding> methodBindings = objectBinding.getInjectedMethodsBindings();
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

        if( injectionResult instanceof Iterable && !Iterable.class.isAssignableFrom( type ) )
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
