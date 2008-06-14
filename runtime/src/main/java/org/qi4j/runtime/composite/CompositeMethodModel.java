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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * TODO
 */
public final class CompositeMethodModel
    implements Binder
{
    // Model
    private Method method;
    private MethodConstraintsModel methodConstraints;
    private MethodConcernsModel methodConcerns;
    private MethodSideEffectsModel methodSideEffects;

    // Context
    private CompositeMethodInstancePool instancePool = new SynchronizedCompositeMethodInstancePool();
    private MethodConstraintsInstance methodConstraintsInstance;

    private AbstractMixinsModel mixins;

    public CompositeMethodModel( Method method,
                                 MethodConstraintsModel methodConstraintsModel,
                                 MethodConcernsModel methodConcernsModel,
                                 MethodSideEffectsModel methodSideEffectsModel,
                                 AbstractMixinsModel mixinsModel )
    {
        this.method = method;
        mixins = mixinsModel;
        methodConcerns = methodConcernsModel;
        methodSideEffects = methodSideEffectsModel;
        methodConstraints = methodConstraintsModel;
    }

    // Model
    public Method method()
    {
        return method;
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), resolution.composite(), this, null );

        methodConcerns.bind( resolution );
        methodSideEffects.bind( resolution );

        methodConstraintsInstance = methodConstraints.newInstance();
    }

    // Context
    public Object invoke( Object composite, Object[] params, MixinsInstance mixins, ModuleInstance moduleInstance ) throws Throwable
    {
        methodConstraintsInstance.checkValid( params );
        CompositeMethodInstance methodInstance = getInstance( moduleInstance );
        try
        {
            return mixins.invoke( composite, params, methodInstance );
        }
        finally
        {
            instancePool.returnInstance( methodInstance );
        }
    }

    private CompositeMethodInstance getInstance( ModuleInstance moduleInstance )
    {
        CompositeMethodInstance methodInstance = instancePool.getInstance();
        if( methodInstance == null )
        {
            methodInstance = newCompositeMethodInstance( moduleInstance );
        }

        return methodInstance;
    }

    private CompositeMethodInstance newCompositeMethodInstance( ModuleInstance moduleInstance )
        throws org.qi4j.composite.InstantiationException
    {
        FragmentInvocationHandler mixinInvocationHandler = mixins.newInvocationHandler( method );

        MethodConcernsInstance concernsInstance = methodConcerns.newInstance( moduleInstance, method, mixinInvocationHandler );
        MethodSideEffectsInstance sideEffectsInstance = methodSideEffects.newInstance( moduleInstance, method );
        return new CompositeMethodInstance( concernsInstance, sideEffectsInstance, method );
    }

    public AnnotatedElement annotatedElement()
    {
        // TODO Calc sum of composite + mixin
        return method;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        methodConstraints.visitModel( modelVisitor );
        methodConcerns.visitModel( modelVisitor );
        methodSideEffects.visitModel( modelVisitor );
    }
}
