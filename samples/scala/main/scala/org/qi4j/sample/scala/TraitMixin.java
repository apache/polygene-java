package org.qi4j.sample.scala;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic mixin that handles delegation to Scala trait implementations.
 */
@AppliesTo(TraitMixin.TraitFilter.class)
public class TraitMixin
    implements InvocationHandler
{
    private static Map<Class<?>, Map<Method, Method>> methods = new HashMap<Class<?>, Map<Method, Method>>();

    private Class<? extends Composite> compositeType;

    public TraitMixin(@This Composite composite)
    {
        compositeType = composite.type();
    }

    @Override
    public Object invoke( Object composite, Method method, Object[] args ) throws Throwable
    {
        Method traitMethod = methods.get( compositeType ).get( method );

        if (args != null)
        {
            Object[] params = new Object[args.length+1];
            params[0] = composite;
            System.arraycopy( args, 0, params, 1, args.length );

            return traitMethod.invoke( null, params );
        } else
            return traitMethod.invoke( null, composite );
    }

    public static class TraitFilter
        implements AppliesToFilter
    {
        @Override
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            if (isScalaTrait(method.getDeclaringClass()))
            {
                // Map methods
                
                final Class<?> declaringClass = method.getDeclaringClass();
                Class traitClass = Iterables.last( Iterables.map( new Function<Class, Class>()
                        {
                            Class current;

                            @Override
                            public Class map( Class aClass )
                            {
                                if ( declaringClass.isAssignableFrom(aClass))
                                {
                                    try
                                    {
                                        aClass.getClassLoader().loadClass( aClass.getName()+"$class" );

                                        if (current == null)
                                            current = aClass;
                                        else
                                            current = current.isAssignableFrom( aClass ) ? aClass : current;
                                    } catch( ClassNotFoundException e )
                                    {
                                        // Ignore - no trait implementation found
                                    }
                                }

                                return current;
                            }
                        }, Classes.interfacesOf( compositeType ) ) );

                if (traitClass == null)
                    return false;

                try
                {
                    Class traitMixin = traitClass.getClassLoader().loadClass( traitClass.getName()+"$class" );
                    Class<?>[] methodParameterTypes = method.getParameterTypes();
                    Class[] parameterTypes = new Class[1+ methodParameterTypes.length];
                    parameterTypes[0] = traitClass;
                    System.arraycopy( methodParameterTypes, 0, parameterTypes, 1, methodParameterTypes.length );
                    Method traitMethod = traitMixin.getMethod( method.getName(), parameterTypes );

                    Map<Method,Method> methodsMap = methods.get( compositeType );
                    if (methodsMap == null)
                    {
                        methodsMap = new HashMap<Method, Method>();
                        methods.put( compositeType, methodsMap );
                    }

                    methodsMap.put( method, traitMethod );

                    return true;
                } catch( ClassNotFoundException e )
                {
                    return false;
                } catch( NoSuchMethodException e )
                {
                    return false;
                }
            } else
            {
                return false;
            }
        }

        private boolean isScalaTrait( Class<?> declaringClass )
        {
            for( Annotation annotation : declaringClass.getAnnotations() )
            {
                if (annotation.annotationType().getSimpleName().equals( "ScalaSignature" ))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
