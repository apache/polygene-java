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

import java.lang.reflect.AnnotatedElement;
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
    private MethodConstraintsModel methodConstraints;
    private MethodConcernsModel methodConcerns;
    private MethodSideEffectsModel methodSideEffects;

    // Resolution
    private CompositeModel composite;

    // Context
    private CompositeMethodInstancePool instancePool = new SynchronizedCompositeMethodInstancePool();
    private MethodConstraintsInstance methodConstraintsInstance;

    public CompositeMethodModel( Method method, CompositeModel composite )
    {
        this.method = method;
        this.composite = composite;

        methodConcerns = composite.concerns().concernsFor( method );
        methodSideEffects = composite.sideEffects().sideEffectsFor( method );
        methodConstraints = new MethodConstraintsModel( method, composite.constraints() );
        composite.sideEffects().sideEffectsFor( method );
    }

    // Model
    public Method method()
    {
        return method;
    }

    // Resolution
    public CompositeModel composite()
    {
        return composite;
    }

    // Binding
    public void bind( Resolution resolution )
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), resolution.composite(), this );

        methodConcerns.bind( resolution );
        methodSideEffects.bind( resolution );

        methodConstraintsInstance = methodConstraints.newInstance();
    }

    // Context
    public Object invoke( Object composite, Object[] params, Object[] mixins, ModuleInstance moduleInstance ) throws Throwable
    {
        methodConstraintsInstance.checkValid( params );

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
        FragmentInvocationHandler mixinInvocationHandler = composite.mixins().newInvocationHandler( method );

        MethodConcernsInstance concernsInstance = methodConcerns.newInstance( moduleInstance, method, mixinInvocationHandler );
        MethodSideEffectsInstance sideEffectsInstance = methodSideEffects.newInstance( moduleInstance, method );
        return new CompositeMethodInstance( concernsInstance, sideEffectsInstance, method );
    }

    public AnnotatedElement annotatedElement()
    {
        // TODO Calc sum of composite + mixin
        return method;
    }
}
