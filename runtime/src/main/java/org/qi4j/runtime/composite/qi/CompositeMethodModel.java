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

import java.lang.reflect.Method;
import org.qi4j.runtime.composite.CompositeMethodInstancePool;
import org.qi4j.runtime.composite.FragmentInvocationHandler;
import org.qi4j.runtime.composite.SynchronizedCompositeMethodInstancePool;
import org.qi4j.runtime.structure.qi.ModuleInstance;

/**
 * TODO
 */
public final class CompositeMethodModel
{
    // Model
    private Method method;
    private ParametersModel parameters;

    // Resolution
    private CompositeModel composite;

    // Context
    private CompositeMethodInstancePool instancePool = new SynchronizedCompositeMethodInstancePool();

    public CompositeMethodModel( Method method, ParametersModel parameters, CompositeModel composite )
    {
        this.method = method;
        this.parameters = parameters;
        this.composite = composite;

        composite.concerns().concernsFor( method );
        composite.sideEffects().sideEffectsFor( method );
    }

    // Model
    public Method method()
    {
        return method;
    }

    public ParametersModel parameters()
    {
        return parameters;
    }

    // Resolution
    public CompositeModel composite()
    {
        return composite;
    }

    // Binding
    public void bind( BindingContext bindingContext )
    {
        // Bind constraints
        // Bind concerns
        // Bind side-effects
    }

    // Context
    public Object invoke( Object composite, Object[] params, Object[] mixins, ModuleInstance moduleInstance ) throws Throwable
    {
        parameters.checkConstraints( params );

        CompositeMethodInstance methodInstance = getInstance( moduleInstance );
        return composite().mixins().invoke( composite, params, mixins, methodInstance );
    }

    private CompositeMethodInstance getInstance( ModuleInstance moduleInstance )
    {
        CompositeMethodInstance methodInstance = newCompositeMethodInstance( moduleInstance );

        return methodInstance;
    }

    private CompositeMethodInstance newCompositeMethodInstance( ModuleInstance moduleInstance )
        throws org.qi4j.composite.InstantiationException
    {
        FragmentInvocationHandler mixinInvocationHandler = composite.mixins().mixinFor( method ).newInvocationHandler( method.getDeclaringClass() );

        ConcernsInstance concernsInstance = composite.concerns().newInstance( moduleInstance, method, mixinInvocationHandler );
        SideEffectsInstance sideEffectsInstance = composite.sideEffects().newInstance( moduleInstance, method );
        return new CompositeMethodInstance( concernsInstance, sideEffectsInstance, method );
    }

}
