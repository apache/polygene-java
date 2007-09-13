package org.qi4j.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.ConstructorDependencyResolution;
import org.qi4j.api.DependencyInjectionContext;
import org.qi4j.api.FieldDependencyResolution;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.MethodDependencyResolution;
import org.qi4j.api.ParameterDependencyResolution;
import org.qi4j.api.model.FragmentResolution;

/**
 * TODO
 */
public class FragmentFactoryImpl
    implements FragmentFactory
{
    public <K> K newFragment( FragmentResolution<K> fragmentResolution, DependencyInjectionContext context )
        throws CompositeInstantiationException
    {
        K instance;
        try
        {
            // Constructor injection
            instance = newInstance( fragmentResolution, context );

            // Field injection
            injectFields( fragmentResolution, context, instance );

            // Method injection
            injectMethods(fragmentResolution, context, instance );

            return instance;
        } catch (CompositeInstantiationException e)
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( "Could not instantiate class " + fragmentResolution.getFragmentModel().getFragmentClass().getName(), e );
        }
    }

    private <K> K newInstance( FragmentResolution<K> fragmentResolution, DependencyInjectionContext context )
        throws InstantiationException, IllegalAccessException, InvocationTargetException
    {
        K instance;// Get constructor
        ConstructorDependencyResolution cdr = fragmentResolution.getConstructorResolutions().iterator().next();

        Constructor constructor = cdr.getConstructorDependency().getConstructor();
        Object[] parameters = new Object[constructor.getParameterTypes().length];

        // Resolve constructor dependencies
        Iterable<ParameterDependencyResolution> dr = cdr.getParameterDependencyResolutions();
        int i = 0;
        for( ParameterDependencyResolution dependencyResolution : dr )
        {
            Iterable parameter = dependencyResolution.getDepedencyResolution().getDependencyInjection(context);
            Class parameterType = constructor.getParameterTypes()[ i ];
            parameters[i] = getInjectedValue( parameter, parameterType);
            i++;
        }

        // Invoke constructor
        instance = fragmentResolution.getFragmentModel().getFragmentClass().cast( constructor.newInstance(parameters ));
        return instance;
    }

    private <K> void injectFields( FragmentResolution<K> fragmentResolution, DependencyInjectionContext context, K instance )
        throws IllegalAccessException
    {
        Iterable<FieldDependencyResolution> fieldResolutions = fragmentResolution.getFieldResolutions();
        for( FieldDependencyResolution fieldResolution : fieldResolutions )
        {
            Iterable iterable = fieldResolution.getDepedencyResolution().getDependencyInjection( context );
            Object value;
            Field field = fieldResolution.getFieldDependency().getField();
            value = getInjectedValue( iterable, field.getType() );
            field.set(instance, value );
        }
    }

    private <K> void injectMethods( FragmentResolution<K> fragmentResolution, DependencyInjectionContext context, K instance )
        throws InstantiationException, IllegalAccessException, InvocationTargetException
    {
        Iterable<MethodDependencyResolution> methodResolutions = fragmentResolution.getMethodResolutions();
        for( MethodDependencyResolution methodResolution : methodResolutions )
        {
            Method method = methodResolution.getMethodDependency().getMethod();
            Object[] parameters = new Object[] { method.getParameterTypes().length };

            // Resolve parameter dependencies
            Iterable<ParameterDependencyResolution> dr = methodResolution.getParameterDependencyResolutions();
            int i = 0;
            for( ParameterDependencyResolution dependencyResolution : dr )
            {
                Iterable parameter = dependencyResolution.getDepedencyResolution().getDependencyInjection(context);
                Class parameterType = method.getParameterTypes()[ i ];
                parameters[i] = getInjectedValue( parameter, parameterType);
                if (!Iterable.class.isAssignableFrom( parameterType ))
                {
                    // Single value
                    parameters[i] = getSingleValue( parameter );
                } else
                    parameters[i] = parameter;

                i++;
            }

            // Invoke method
            method.invoke( instance, parameters);
        }
    }

    private <K> Object getInjectedValue( Iterable iterable, Class type )
    {
        Object value;
        if (!Iterable.class.isAssignableFrom( type))
        {
            // Single value
            value = getSingleValue( iterable );


        } else
            value = iterable;
        return value;
    }

    private <K> Object getSingleValue( Iterable parameter )
    {
        Iterator iterator = parameter.iterator();
        if (iterator.hasNext())
            return iterator.next();
        else
            return null;
    }
}
