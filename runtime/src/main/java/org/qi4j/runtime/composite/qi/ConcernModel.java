/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.TypedFragmentInvocationHandler;
import org.qi4j.runtime.structure.qi.ModuleInstance;
import static org.qi4j.util.ClassUtil.interfacesOf;
import static org.qi4j.util.ClassUtil.toClassArray;

/**
 * TODO
 */
public class ConcernModel
{
    private Class concernClass;
    private ConstructorsModel constructorsModel;
    private InjectedFieldsModel injectedFieldsModel;
    private InjectedMethodsModel injectedMethodsModel;

    private Class[] nextInterfaces;

    public ConcernModel( Class concernClass )
    {
        this.concernClass = concernClass;

        constructorsModel = new ConstructorsModel( concernClass );
        injectedFieldsModel = new InjectedFieldsModel( concernClass );
        injectedMethodsModel = new InjectedMethodsModel( concernClass );

        nextInterfaces = toClassArray( interfacesOf( concernClass ) );

    }

    public Class type()
    {
        return concernClass;
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( concernClass );
    }

    // Binding
    public void bind( BindingContext context )
    {
        constructorsModel.bind( context );
        injectedFieldsModel.bind( context );
        injectedMethodsModel.bind( context );
    }

    // Context
    public Object newInstance( ModuleInstance moduleInstance, Object nextConcern, ProxyReferenceInvocationHandler proxyHandler )
    {
        nextConcern = getNextConcern( nextConcern );

        InjectionContext injectionContext = new InjectionContext( moduleInstance, nextConcern, proxyHandler );
        Object mixin = constructorsModel.newInstance( injectionContext );
        injectedFieldsModel.inject( injectionContext, mixin );
        injectedMethodsModel.inject( injectionContext, mixin );
        return mixin;
    }

    private Object getNextConcern( Object next )
    {
        Object modifies;
        if( isGeneric() )
        {
            if( next instanceof InvocationHandler )
            {
                modifies = next;
            }
            else
            {
                modifies = new TypedFragmentInvocationHandler( next );
            }
        }
        else
        {
            if( next instanceof InvocationHandler )
            {
                modifies = Proxy.newProxyInstance( concernClass.getClassLoader(), nextInterfaces, (InvocationHandler) next );
            }
            else
            {
                modifies = next;
            }
        }
        return modifies;
    }

}
