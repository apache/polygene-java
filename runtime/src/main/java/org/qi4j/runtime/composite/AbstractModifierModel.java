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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import static org.qi4j.api.util.Classes.interfacesOf;
import static org.qi4j.api.util.Classes.toClassArray;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * TODO
 */
public abstract class AbstractModifierModel
    implements Binder
{
    private final Class modifierClass;

    private final ConstructorsModel constructorsModel;
    private final InjectedFieldsModel injectedFieldsModel;
    private final InjectedMethodsModel injectedMethodsModel;

    private final Class[] nextInterfaces;

    public AbstractModifierModel( Class modifierClass )
    {
        this.modifierClass = modifierClass;

        constructorsModel = new ConstructorsModel( modifierClass );
        injectedFieldsModel = new InjectedFieldsModel( modifierClass );
        injectedMethodsModel = new InjectedMethodsModel( modifierClass );

        nextInterfaces = toClassArray( interfacesOf( modifierClass ) );
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
    public void bind( Resolution context ) throws BindingException
    {
        constructorsModel.bind( context );
        injectedFieldsModel.bind( context );
        injectedMethodsModel.bind( context );
    }

    // Context
    public Object newInstance( ModuleInstance moduleInstance, Object next, ProxyReferenceInvocationHandler proxyHandler )
    {
        InjectionContext injectionContext = new InjectionContext( moduleInstance, wrapNext( next ), proxyHandler );
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
                return new TypedFragmentInvocationHandler( next );
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

}