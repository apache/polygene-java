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

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.TypedFragmentInvocationHandler;
import org.qi4j.runtime.structure.qi.Binder;
import org.qi4j.runtime.structure.qi.ModuleInstance;
import static org.qi4j.util.ClassUtil.interfacesOf;
import static org.qi4j.util.ClassUtil.toClassArray;

/**
 * TODO
 */
public abstract class AbstractModifierModel
    implements Binder
{
    private Class modifierClass;
    protected ConstructorsModel constructorsModel;
    protected InjectedFieldsModel injectedFieldsModel;
    protected InjectedMethodsModel injectedMethodsModel;

    private Class[] nextInterfaces;

    public AbstractModifierModel( Class modifierClass )
    {
        this.modifierClass = modifierClass;

        constructorsModel = new ConstructorsModel( modifierClass );
        injectedFieldsModel = new InjectedFieldsModel( modifierClass );
        injectedMethodsModel = new InjectedMethodsModel( modifierClass );

        nextInterfaces = toClassArray( interfacesOf( modifierClass ) );
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( modifierClass );
    }


    public void visitDependencies( DependencyVisitor visitor )
    {
        constructorsModel.visitDependencies( visitor );
        injectedFieldsModel.visitDependencies( visitor );
        injectedMethodsModel.visitDependencies( visitor );
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
                return Proxy.newProxyInstance( modifierClass.getClassLoader(), nextInterfaces, (InvocationHandler) next );
            }
            else
            {
                return next;
            }
        }
    }

}