/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.polygene.runtime.composite;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.polygene.api.common.AppliesTo;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.composite.DefaultMethodsFilter;
import org.apache.polygene.api.injection.scope.This;

import static java.lang.invoke.MethodHandles.Lookup.PACKAGE;
import static java.lang.invoke.MethodHandles.Lookup.PRIVATE;
import static java.lang.invoke.MethodHandles.Lookup.PROTECTED;
import static java.lang.invoke.MethodHandles.Lookup.PUBLIC;
import static org.apache.polygene.api.util.AccessibleObjects.accessible;

@AppliesTo( { DefaultMethodsFilter.class } )
public class InterfaceDefaultMethodsMixin
    implements InvocationHandler
{
    // TODO (niclas): We have one instance of this class per mixin, so it seems a bit wasteful to have a ConcurrentHashMap. Maybe a small array 3 elements, which is changed to a Map if run out of space? Tricky concurrency on that, so leave it for later (a.k.a. will forget about it)
    private final ConcurrentMap<Method, MethodCallHandler> methodHandleCache = new ConcurrentHashMap<>();

    @This
    private Composite me;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if( method.isDefault() )
        {
            // Call the interface's default method
            MethodCallHandler callHandler = forMethod( method );
            return callHandler.invoke( proxy, args );
        }
        // call the composite's method instead.
        return method.invoke( me, args );
    }

    private MethodCallHandler forMethod( Method method )
    {
        return methodHandleCache.computeIfAbsent( method, this::createMethodCallHandler );
    }

    private MethodCallHandler createMethodCallHandler( Method method )
    {
        Class<?> declaringClass = method.getDeclaringClass();
        try
        {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor( Class.class, int.class );
            MethodHandles.Lookup lookup = accessible( constructor ).newInstance( declaringClass, PRIVATE | PUBLIC | PROTECTED | PACKAGE);
            MethodHandle handle = lookup.unreflectSpecial( method, declaringClass );
            return ( proxy, args ) -> handle.bindTo( proxy ).invokeWithArguments( args );
        }
        catch( IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e )
        {
            throw new RuntimeException( e );
        }
    }

    @FunctionalInterface
    private interface MethodCallHandler
    {
        Object invoke( Object proxy, Object[] args )
            throws Throwable;
    }
}
