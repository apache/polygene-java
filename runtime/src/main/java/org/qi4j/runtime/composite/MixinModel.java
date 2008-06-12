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
import java.util.HashSet;
import java.util.Set;
import org.qi4j.composite.State;
import org.qi4j.injection.scope.This;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.DependencyVisitor;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.composite.CompositeInstance;

/**
 * TODO
 */
public final class MixinModel
    implements Binder
{
    // Model
    private Class mixinClass;
    private ConstructorsModel constructorsModel;
    private InjectedFieldsModel injectedFieldsModel;
    private InjectedMethodsModel injectedMethodsModel;

    public MixinModel( Class mixinClass )
    {
        this.mixinClass = mixinClass;

        constructorsModel = new ConstructorsModel( mixinClass );
        injectedFieldsModel = new InjectedFieldsModel( mixinClass );
        injectedMethodsModel = new InjectedMethodsModel( mixinClass );
    }

    public Class mixinClass()
    {
        return mixinClass;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

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
    public Object newInstance( CompositeInstance compositeInstance, UsesInstance uses, State state )
    {
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        Object mixin = constructorsModel.newInstance( injectionContext );
        injectedFieldsModel.inject( injectionContext, mixin );
        injectedMethodsModel.inject( injectionContext, mixin );
        return mixin;
    }

    public Set<Class> thisMixinTypes()
    {
        final Set<Class> mixinTypes = new HashSet<Class>();

        DependencyVisitor visitor = new DependencyVisitor( new DependencyVisitor.AnnotationSpecification( This.class ) )
        {
            public void visitDependency( DependencyModel dependencyModel )
            {
                mixinTypes.add( dependencyModel.injectionClass() );
            }
        };

        visitModel( visitor );

        return mixinTypes;
    }

    protected FragmentInvocationHandler newInvocationHandler( Class methodClass )
    {
        if( InvocationHandler.class.isAssignableFrom( mixinClass ) && !methodClass.isAssignableFrom( mixinClass ) )
        {
            return new GenericFragmentInvocationHandler();
        }
        else
        {
            return new TypedFragmentInvocationHandler();
        }

    }
}
