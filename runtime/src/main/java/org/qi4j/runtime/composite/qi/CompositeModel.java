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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.composite.State;
import org.qi4j.runtime.structure.qi.ModuleInstance;
import org.qi4j.runtime.structure.qi.ModuleModel;

/**
 * TODO
 */
public final class CompositeModel
{
    private ConcernsModel concernsModel;
    private MixinsModel mixinsModel;
    private CompositeMethodsModel compositeMethodsModel;
    private Class<? extends Composite> compositeType;
    private ModuleModel moduleModel;
    private Class<? extends Composite> proxyClass;

    public CompositeModel( Class<? extends Composite> compositeType, ModuleModel moduleModel )
    {
        this.compositeType = compositeType;
        this.moduleModel = moduleModel;

        // Create proxy class
        this.proxyClass = createProxyClass( compositeType );

        concernsModel = new ConcernsModel( compositeType );
        mixinsModel = new MixinsModel( compositeType );

        compositeMethodsModel = new CompositeMethodsModel( this );

        mixinsModel.implementThisUsing( this );
    }

    // Model
    public Class<? extends Composite> type()
    {
        return compositeType;
    }

    public Class<? extends Composite> proxyClass()
    {
        return proxyClass;
    }

    public ConstraintsDeclaration constraintDeclaration()
    {
        return null;
    }

    public ConcernsModel concerns()
    {
        return concernsModel;
    }

    public MixinsModel mixins()
    {
        return mixinsModel;
    }

    private Class createProxyClass( Class<? extends Composite> compositeType )
    {
        ClassLoader proxyClassloader = compositeType.getClassLoader();
        Class[] interfaces = new Class[]{ compositeType };
        Class proxyClass = Proxy.getProxyClass( proxyClassloader, interfaces );
        return proxyClass;
    }

    // Binding
    public void bind( BindingContext bindingContext )
    {
        bindingContext = new BindingContext( bindingContext.application(), bindingContext.layer(), bindingContext.module(), this );
        compositeMethodsModel.bind( bindingContext );
        concernsModel.bind( bindingContext );
        mixinsModel.bind( bindingContext );
    }

    // Context
    public Object invoke( Object[] mixins, Object proxy, Method method, Object[] args, ModuleInstance moduleInstance )
        throws Throwable
    {
        return compositeMethodsModel.invoke( mixins, proxy, method, args, moduleInstance );
    }

    public Composite newProxy( InvocationHandler invocationHandler )
        throws org.qi4j.composite.InstantiationException
    {
        // Instantiate proxy for given composite interface
        try
        {
            return Composite.class.cast( proxyClass.getConstructor( InvocationHandler.class ).newInstance( invocationHandler ) );
        }
        catch( Exception e )
        {
            throw new org.qi4j.composite.InstantiationException( e );
        }
    }

    public CompositeInstance newCompositeInstance( ModuleInstance moduleInstance,
                                                   Set<Object> uses,
                                                   State state )
    {
        Object[] mixins = mixinsModel.newMixinHolder();
        DefaultCompositeInstance compositeInstance = new DefaultCompositeInstance( this, moduleInstance, mixins );

        // Instantiate all mixins
        mixinsModel.newMixins( compositeInstance,
                               uses,
                               state,
                               mixins );

        // Invoke lifecycle create() method
/*
        if( proxy instanceof Lifecycle )
        {
            invokeCreate( proxy, compositeInstance );
        }
*/

        // Return
        return compositeInstance;
    }

    public void implementMixinType( Class mixinType )
    {
        compositeMethodsModel.implementMixinType( mixinType, mixinsModel );
    }
}