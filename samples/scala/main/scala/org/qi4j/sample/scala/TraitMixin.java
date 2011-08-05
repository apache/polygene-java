package org.qi4j.sample.scala;

import org.qi4j.api.Qi4j;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic mixin that handles delegation to Scala trait implementations.
 */
@AppliesTo(TraitMixin.TraitFilter.class)
public class TraitMixin
    implements InvocationHandler
{
    private static Map<Class<?>, Map<Method, InvocationHandler>> methods = new HashMap<Class<?>, Map<Method, InvocationHandler>>();

    private Class<?> compositeType;

    public TraitMixin(@This Composite composite)
    {
        compositeType = Qi4j.DESCRIPTOR_FUNCTION.map( composite).type();
    }

    @Override
    public Object invoke( Object composite, Method method, Object[] args ) throws Throwable
    {
        InvocationHandler handler = methods.get( compositeType ).get( method );

        return handler.invoke( composite, method, args );
    }

    public static class TraitFilter
        implements AppliesToFilter
    {
        @Override
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
        {
            if (isScalaTrait(method.getDeclaringClass()))
            {
                // Service injection
                if (method.getAnnotation( Service.class ) != null)
                {
                    if (method.getReturnType().equals( ServiceReference.class ))
                    {
                        InvocationHandler handler = new InvocationHandler()
                        {
                            @Override
                            public Object invoke( Object composite, Method method, Object[] objects ) throws Throwable
                            {
                                return ((CompositeInstance)Proxy.getInvocationHandler( composite )).module().findService( method.getReturnType() );
                            }
                        };
                        getHandlers( compositeType ).put( method, handler );
                    } else
                    {
                        InvocationHandler handler = new InvocationHandler()
                        {
                            @Override
                            public Object invoke( Object composite, Method method, Object[] objects ) throws Throwable
                            {
                                return ((CompositeInstance)Proxy.getInvocationHandler( composite )).module().findService( method.getReturnType() ).get();
                            }
                        };
                        getHandlers( compositeType ).put( method, handler );
                    }

                    return true;
                }

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
                        }, Iterables.map( Classes.RAW_CLASS, Classes.INTERFACES_OF.map( compositeType ) ) ) );

                if (traitClass == null)
                    return false;

                try
                {
                    Class traitMixin = traitClass.getClassLoader().loadClass( traitClass.getName()+"$class" );
                    Class<?>[] methodParameterTypes = method.getParameterTypes();
                    Class[] parameterTypes = new Class[1+ methodParameterTypes.length];
                    parameterTypes[0] = traitClass;
                    System.arraycopy( methodParameterTypes, 0, parameterTypes, 1, methodParameterTypes.length );
                    final Method traitMethod = traitMixin.getMethod( method.getName(), parameterTypes );

                    Map<Method,InvocationHandler> handlers = getHandlers( compositeType );

                    handlers.put( method, new InvocationHandler()
                    {
                        @Override
                        public Object invoke( Object composite, Method method, Object[] args ) throws Throwable
                        {

                            if( args != null )
                            {
                                Object[] params = new Object[args.length + 1];
                                params[0] = composite;
                                System.arraycopy( args, 0, params, 1, args.length );

                                return traitMethod.invoke( null, params );
                            } else
                                return traitMethod.invoke( null, composite );
                        }
                    } );

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

        private Map<Method, InvocationHandler> getHandlers(Class<?> compositeType)
        {
            Map<Method,InvocationHandler> handlerMap = methods.get( compositeType );
            if (handlerMap == null)
            {
                handlerMap = new HashMap<Method, InvocationHandler>();
                methods.put( compositeType, handlerMap );
            }

            return handlerMap;
        }
    }
}
