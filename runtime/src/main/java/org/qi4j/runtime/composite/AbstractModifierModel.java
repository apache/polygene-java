/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.composite;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;

import static org.qi4j.api.util.Classes.*;

/**
 * JAVADOC
 */
public abstract class AbstractModifierModel
    implements Binder, Serializable
{
    private final static Map<Class, Class> enhancedClasses = Collections.synchronizedMap( new HashMap<Class, Class>() );

    private final Class modifierClass;

    private ConstructorsModel constructorsModel;
    private InjectedFieldsModel injectedFieldsModel;
    private InjectedMethodsModel injectedMethodsModel;

    private Class[] nextInterfaces;

    public AbstractModifierModel( Class declaredModifierClass )
    {
        this.modifierClass = instantiationClass( declaredModifierClass );
        constructorsModel = new ConstructorsModel( modifierClass );
        injectedFieldsModel = new InjectedFieldsModel( declaredModifierClass );
        injectedMethodsModel = new InjectedMethodsModel( declaredModifierClass );
        nextInterfaces = toClassArray( interfacesOf( declaredModifierClass ) );
    }

    public Class modifierClass()
    {
        return modifierClass;
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( modifierClass );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        constructorsModel.visitModel( modelVisitor );
        injectedFieldsModel.visitModel( modelVisitor );
        injectedMethodsModel.visitModel( modelVisitor );
    }

    // Binding

    public void bind( Resolution context )
        throws BindingException
    {
        constructorsModel.bind( context );
        injectedFieldsModel.bind( context );
        injectedMethodsModel.bind( context );
    }

    // Context

    public Object newInstance( ModuleInstance moduleInstance,
                               Object next,
                               ProxyReferenceInvocationHandler proxyHandler
    )
    {
        InjectionContext injectionContext = new InjectionContext( moduleInstance, wrapNext( next ), proxyHandler );
        if( Factory.class.isAssignableFrom( modifierClass ) )
        {
            Enhancer.registerCallbacks( modifierClass,
                                        new Callback[]{ NoOp.INSTANCE, proxyHandler } );
        }

        Object mixin = constructorsModel.newInstance( injectionContext );
        injectedFieldsModel.inject( injectionContext, mixin );
        injectedMethodsModel.inject( injectionContext, mixin );
        return mixin;
    }

    private Object wrapNext( Object next )
    {
        if( isGeneric() )
        {
            if( next instanceof InvocationHandler )
            {
                return next;
            }
            else
            {
                return new TypedModifierInvocationHandler( next );
            }
        }
        else
        {
            if( next instanceof InvocationHandler )
            {
                Object proxy = Proxy.newProxyInstance( modifierClass.getClassLoader(), nextInterfaces, (InvocationHandler) next );
                return proxy;
            }
            else
            {
                return next;
            }
        }
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        AbstractModifierModel that = (AbstractModifierModel) o;

        if( !modifierClass.equals( that.modifierClass ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return modifierClass.hashCode();
    }

    private Class instantiationClass( Class fragmentClass )
    {
        Class instantiationClass = fragmentClass;
        if( Modifier.isAbstract( fragmentClass.getModifiers() ) )
        {
            instantiationClass = enhancedClasses.get( fragmentClass );
            if( instantiationClass == null )
            {
                Enhancer enhancer = createEnhancer( fragmentClass );
                instantiationClass = enhancer.createClass();
                enhancedClasses.put( fragmentClass, instantiationClass );
            }
        }
        return instantiationClass;
    }

    private Enhancer createEnhancer( Class fragmentClass )
    {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass( fragmentClass );
        // TODO: make this configurable?
        enhancer.setClassLoader( new BridgeClassLoader( fragmentClass.getClassLoader() ) );
        enhancer.setCallbackTypes( new Class[]{ NoOp.class, net.sf.cglib.proxy.InvocationHandler.class } );
        enhancer.setCallbackFilter( new CallbackFilter()
        {
            public int accept( Method method )
            {
                return Modifier.isAbstract( method.getModifiers() ) ? 1 : 0;
            }
        } );
        return enhancer;
    }
}