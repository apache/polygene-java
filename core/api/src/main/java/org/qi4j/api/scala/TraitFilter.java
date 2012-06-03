package org.qi4j.api.scala;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.util.Classes;

import static org.qi4j.api.util.Classes.interfacesOf;
import static org.qi4j.functional.Iterables.last;
import static org.qi4j.functional.Iterables.map;

public class TraitFilter
    implements AppliesToFilter
{
    @Override
    public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
    {
        if( !isScalaTrait(method.getDeclaringClass()) )
        {
            return false;
        }
        boolean isServiceClass = method.getAnnotation( Service.class ) != null;
        if( isServiceClass )
        {
            return checkServiceMethod( method, compositeType );
        }
        else
        {
            // Map methods
            final Class<?> declaringClass = method.getDeclaringClass();
            Iterable<Class<?>> interfacesOfComposite = map( Classes.RAW_CLASS, interfacesOf( compositeType ) );
            Class traitClass = last( map( new ClassClassFunction( declaringClass ), interfacesOfComposite ) );

            if( traitClass == null )
            {
                return false;
            }

            try
            {
                Class traitMixin = TraitMixin.tryToLoadTraitClass( traitClass );
                Class<?>[] methodParameterTypes = method.getParameterTypes();
                Class[] parameterTypes = new Class[ 1 + methodParameterTypes.length ];
                parameterTypes[ 0 ] = traitClass;
                System.arraycopy( methodParameterTypes, 0, parameterTypes, 1, methodParameterTypes.length );
                final Method traitMethod = traitMixin.getMethod( method.getName(), parameterTypes );
                registerHandler( method, compositeType, new ScalaTraitInvocationHandler( traitMethod ) );
                return true;
            }
            catch( ClassNotFoundException e )
            {
                return false;
            }
            catch( NoSuchMethodException e )
            {
                return false;
            }
        }
    }

    private boolean checkServiceMethod( Method method, Class<?> compositeType )
    {
        if( method.getReturnType().equals( ServiceReference.class ) )
        {
            InvocationHandler handler = new InvocationHandler()
            {
                @Override
                public Object invoke( Object composite, Method method, Object[] objects )
                    throws Throwable
                {
                    return ( (CompositeInstance) Proxy.getInvocationHandler( composite ) ).module()
                        .findService( method.getReturnType() );
                }
            };
            registerHandler( method, compositeType, handler );
        }
        else
        {
            InvocationHandler handler = new InvocationHandler()
            {
                @Override
                public Object invoke( Object composite, Method method, Object[] objects )
                    throws Throwable
                {
                    return ( (CompositeInstance) Proxy.getInvocationHandler( composite ) ).module()
                        .findService( method.getReturnType() )
                        .get();
                }
            };
            registerHandler( method, compositeType, handler );
        }

        return true;
    }

    private void registerHandler( Method method, Class<?> compositeType, InvocationHandler handler )
    {
        System.out.println("Adding: " + method + " --> " + handler);
        getHandlers( compositeType ).put( method, handler );
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
        Map<Method,InvocationHandler> handlerMap = TraitMixin.methods.get( compositeType );
        if (handlerMap == null)
        {
            handlerMap = new HashMap<Method, InvocationHandler>();
            TraitMixin.methods.put( compositeType, handlerMap );
        }

        return handlerMap;
    }
}
