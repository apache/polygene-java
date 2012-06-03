package org.qi4j.api.scala;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.Qi4j;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.This;

import static org.qi4j.functional.Iterables.toList;

/**
 * Generic mixin that handles delegation to Scala trait implementations.
 */
@AppliesTo( TraitFilter.class )
public class TraitMixin
    implements InvocationHandler
{
    static Map<Class<?>, Map<Method, InvocationHandler>> methods = new HashMap<Class<?>, Map<Method, InvocationHandler>>();

    private List<Class<?>> types;

    public TraitMixin( @This Composite composite )
    {
        types = toList( Qi4j.DESCRIPTOR_FUNCTION.map( composite ).mixinTypes() );
        System.out.println( "Types: " + types );
    }

    @Override
    public Object invoke( Object composite, Method method, Object[] args )
        throws Throwable
    {
        Class<?> invokedMixin = method.getDeclaringClass();
        System.out.println( "Invoked Mixin: " + invokedMixin );
        Map<Method, InvocationHandler> handlerMap = methods.get( invokedMixin );
        InvocationHandler handler = handlerMap.get( method );
        return handler.invoke( composite, method, args );
    }

    static Class<?> tryToLoadTraitClass( Class<?> scalaClass )
        throws ClassNotFoundException
    {
        return scalaClass.getClassLoader().loadClass( scalaClass.getName() + "$class" );
    }
}
