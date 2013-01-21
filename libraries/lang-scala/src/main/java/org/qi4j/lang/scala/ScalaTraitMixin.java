/*
 * Copyright 2011 Rickard Oberg.
 * Copyright 2012 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.lang.scala;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
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

import static org.qi4j.api.util.Classes.interfacesOf;

/**
 * Generic mixin that handles delegation to Scala trait implementations.
 */
@AppliesTo(ScalaTraitMixin.TraitFilter.class)
public class ScalaTraitMixin
    implements InvocationHandler
{
    private static Map<Class<?>, Map<Method, InvocationHandler>> methods = new HashMap<Class<?>, Map<Method, InvocationHandler>>();

    private Class<?> compositeType;

    public ScalaTraitMixin( @This Composite composite )
    {
        compositeType = Qi4j.FUNCTION_DESCRIPTOR_FOR.map( composite).primaryType();
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
                }, Iterables.map( Classes.RAW_CLASS, interfacesOf( compositeType ) ) ) );

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
