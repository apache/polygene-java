/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.MixinFactory;
import org.qi4j.api.ObjectFactory;
import org.qi4j.api.ObjectInstantiationException;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.persistence.Identity;
import org.qi4j.spi.object.InvocationInstance;
import org.qi4j.spi.object.ModifierInstance;
import org.qi4j.spi.object.ObjectContext;

/**
 * TODO
 */
public class ObjectInvocationHandler
    implements InvocationHandler
{
    private ObjectContext context;
    private Map<Class, Object> mixins;

    public ObjectInvocationHandler( ObjectContext aContext )
    {
        this.context = aContext;
        mixins = new IdentityHashMap<Class, Object>();
    }

    public static ObjectInvocationHandler getInvocationHandler( Object aProxy )
    {
        return (ObjectInvocationHandler) Proxy.getInvocationHandler( aProxy );
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        Class proxyInterface = method.getDeclaringClass();

        if( proxyInterface.equals( Object.class ) )
        {
            if( method.getName().equals( "hashCode" ) )
            {
                if( Identity.class.isAssignableFrom( context.getBindingType() ) )
                {
                    String id = ( (Identity) proxy ).getIdentity();
                    if( id != null )
                    {
                        return id.hashCode();
                    }
                    else
                    {
                        return 0;
                    }
                }
                else
                {
                    return 0; // TODO ?
                }
            }
            if( method.getName().equals( "equals" ) )
            {
                if( Identity.class.isAssignableFrom( context.getBindingType() ) )
                {
                    String id = ( (Identity) proxy ).getIdentity();
                    return id != null && id.equals( ( (Identity) args[ 0 ] ).getIdentity() );
                }
                else
                {
                    return false;
                }
            }
            if( method.getName().equals( "toString" ) )
            {
                if( Identity.class.isAssignableFrom( context.getBindingType() ) )
                {
                    String id = ( (Identity) proxy ).getIdentity();
                    return id != null ? id : "";
                }
                else
                {
                    return "";
                }
            }
        }

        Object mixin = mixins.get( proxyInterface );

        if( mixin == null )
        {
            mixin = initializeMixin( proxyInterface, proxy, getWrappedInstance() );
        }

        Object invokedObject = mixin;

        // Get interface modifiers
        List<InvocationInstance> instances = context.getMethodToInvocationInstanceMap().get( method );
        if( instances == null )
        {
            instances = new ArrayList<InvocationInstance>();
            context.getMethodToInvocationInstanceMap().put( method, instances );
        }
        InvocationInstance invocationInstance;
        int size = instances.size();
        if( size > 0 )
        {
            invocationInstance = instances.remove( size - 1 );
        }
        else
        {
            invocationInstance = context.newInvocationInstance( method, mixin, instances );
        }

        ModifierInstance interfaceModifierInstance = invocationInstance.getInterfaceInstance();
        ModifierInstance mixinModifierInstance = invocationInstance.getMixinInstance();
        Object interfaceFirstModifier = interfaceModifierInstance.getFirstModifier();
        Object mixinFirstModifier = mixinModifierInstance.getFirstModifier();
        if( interfaceFirstModifier != null )
        {
            invokedObject = interfaceFirstModifier;

            if( mixinFirstModifier != null )
            {
                interfaceModifierInstance.setNextModifier( mixinFirstModifier );
                mixinModifierInstance.setNextModifier( mixin );
            }
            else
            {
                interfaceModifierInstance.setNextModifier( mixin );
            }
        }
        else
        {
            if( mixinFirstModifier != null )
            {
                invokedObject = mixinFirstModifier;
                mixinModifierInstance.setNextModifier( mixin );
            }
        }

        invocationInstance.getProxyHandler().setContext( proxy, mixin, proxyInterface );

        // Invoke
        try
        {
            return method.invoke( invokedObject, args );
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
        catch( UndeclaredThrowableException e )
        {
            throw e.getUndeclaredThrowable();
        }
        finally
        {
            invocationInstance.release();
        }
    }

    public Map<Class, Object> getMixins()
    {
        return mixins;
    }

    // Private -------------------------------------------------------
    private void findImplementations( Class aMethodClass, Class aType, List<Class> anImplementationClasses )
    {
        ImplementedBy impls = (ImplementedBy) aType.getAnnotation( ImplementedBy.class );
        if( impls != null )
        {
            Class[] implementationClasses = impls.value();
            for( Class implementationClass : implementationClasses )
            {
                if( aMethodClass.isAssignableFrom( implementationClass ) )
                {
                    anImplementationClasses.add( implementationClass );
                }
            }
        }

        // Check subinterfaces
        Class[] subTypes = aType.getInterfaces();
        for( Class subType : subTypes )
        {
            findImplementations( aMethodClass, subType, anImplementationClasses );
        }
    }

    protected Object initializeMixin( Class aProxyInterface, Object proxy, Object wrappedInstance )
        throws IllegalAccessException
    {
        if( aProxyInterface.isInstance( wrappedInstance ) )
        {
            return wrappedInstance;
        }

        Object mixin;
        List<Class> implementationClasses = new ArrayList<Class>();
        findImplementations( aProxyInterface, context.getBindingType(), implementationClasses );
        if( implementationClasses.size() == 0 && wrappedInstance != null )
        {
            findImplementations( aProxyInterface, wrappedInstance.getClass().getInterfaces()[ 0 ], implementationClasses );
        }

        if( implementationClasses.size() == 0 )
        {
            throw new ObjectInstantiationException( "Could not find implementation for " + aProxyInterface.getName() + " in binding " + context.getBindingType().getName() );
        }

        ObjectInstantiationException ex = null;
        mixins:
        for( Class mixinClass : implementationClasses )
        {
            mixin = context.getMixinFactory().newInstance( mixinClass );

            Class currentClass = mixin.getClass();
            while( currentClass != Object.class )
            {
                for( Field field : currentClass.getDeclaredFields() )
                {
                    Uses uses = field.getAnnotation( Uses.class );
                    if( uses != null )
                    {
                        field.setAccessible( true );
                        if( field.getType().isInstance( proxy ) )
                        {
                            field.set( mixin, proxy );
                        }
                        else if( field.getType().isInstance( wrappedInstance ) )
                        {
                            field.set( mixin, wrappedInstance );
                        }
                        else
                        {
                            Object current = wrappedInstance;
                            boolean done = false;
                            while( current instanceof Proxy )
                            {
                                InvocationHandler handler = Proxy.getInvocationHandler( current );
                                if( handler instanceof WrappedObjectInvocationHandler )
                                {
                                    current = ( (WrappedObjectInvocationHandler) handler ).getWrappedInstance();
                                    if( field.getType().isInstance( current ) )
                                    {
                                        field.set( mixin, current );
                                        done = true;
                                    }
                                }
                                else
                                {
                                    break;
                                }
                            }

                            if( !done )
                            {
                                ex = new ObjectInstantiationException( "@Uses field " + field.getName() + " in class " + currentClass.getName() + " could not be resolved for binding " + context.getBindingType().getName() + "." );
                                continue mixins;
                            }
                        }
                    }
                    Dependency dependency = field.getAnnotation( Dependency.class );
                    if( dependency != null )
                    {
                        field.setAccessible( true );
                        if( field.getType().equals( ObjectFactory.class ) )
                        {
                            field.set( mixin, context.getObjectFactory() );
                        }
                        else if( field.getType().equals( MixinFactory.class ) )
                        {
                            field.set( mixin, context.getMixinFactory() );
                        }
                        else
                        {
                            ex = new ObjectInstantiationException( "@Dependency field " + field.getName() + " in class " + currentClass.getName() + " could not be resolved." );
                            continue mixins;
                        }
                    }
                }
                currentClass = currentClass.getSuperclass();
            }
            mixins.put( aProxyInterface, mixin );
            return mixin;
        }

        throw ex;
    }

    protected Object getWrappedInstance()
    {
        return null;
    }
}
