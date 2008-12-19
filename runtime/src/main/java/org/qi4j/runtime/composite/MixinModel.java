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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.DependencyVisitor;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.mixin.MixinDescriptor;
import org.qi4j.spi.composite.InvalidCompositeException;

/**
 * TODO
 */
public final class MixinModel
    implements Binder, MixinDescriptor
{
    private final Class mixinClass;
    private final ConstructorsModel constructorsModel;
    private final InjectedFieldsModel injectedFieldsModel;
    private final InjectedMethodsModel injectedMethodsModel;
    private final ConcernsDeclaration concernsDeclaration;
    private final SideEffectsDeclaration sideEffectsDeclaration;
    private final Set<Class> thisMixinTypes;

    public MixinModel( Class mixinClass )
    {
        this.mixinClass = mixinClass;

        constructorsModel = new ConstructorsModel( mixinClass );
        injectedFieldsModel = new InjectedFieldsModel( mixinClass );
        injectedMethodsModel = new InjectedMethodsModel( mixinClass );

        concernsDeclaration = new ConcernsDeclaration( mixinClass );
        sideEffectsDeclaration = new SideEffectsDeclaration( mixinClass );

        thisMixinTypes = buildThisMixinTypes();
    }

    public Class mixinClass()
    {
        return mixinClass;
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( mixinClass );
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
    public Object newInstance( CompositeInstance compositeInstance, UsesInstance uses, StateHolder state )
    {
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        Object mixin;
        try
        {
            mixin = constructorsModel.newInstance( injectionContext );
        }
        catch( InvalidCompositeException e )
        {
            e.setMixinClass( mixinClass );
            throw e;
        }
        injectedFieldsModel.inject( injectionContext, mixin );
        injectedMethodsModel.inject( injectionContext, mixin );
        if( mixin instanceof Initializable )
        {
            try
            {
                ( (Initializable) mixin ).initialize();
            }
            catch( InitializationException e )
            {
                throw new ConstructionException( "Unable to initialize " + mixinClass + " in composite " + compositeInstance.type(), e );
            }
        }
        return mixin;
    }

    public Set<Class> thisMixinTypes()
    {
        return thisMixinTypes;
    }

    private Set<Class> buildThisMixinTypes()
    {
        final Set<Class> thisDependencies = new HashSet<Class>();
        visitModel(
            new DependencyVisitor( new DependencyModel.ScopeSpecification( This.class ) )
            {
                public void visitDependency( DependencyModel dependencyModel )
                {
                    thisDependencies.add( dependencyModel.rawInjectionType() );
                }
            }
        );
        if( thisDependencies.isEmpty() )
        {
            return Collections.emptySet();
        }
        else
        {
            return thisDependencies;
        }
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

    public MethodConcernsModel concernsFor( Method method, Class<? extends Composite> type )
    {
        return concernsDeclaration.concernsFor( method, type );
    }


    public MethodSideEffectsModel sideEffectsFor( Method method, Class<? extends Composite> type )
    {
        return sideEffectsDeclaration.sideEffectsFor( method, type );
    }

    @Override public String toString()
    {
        return mixinClass.getName();
    }

    public void addThisInjections( final Set<Class> thisDependencies )
    {
        visitModel(
            new DependencyVisitor( new DependencyModel.ScopeSpecification( This.class ) )
            {
                public void visitDependency( DependencyModel dependencyModel )
                {
                    thisDependencies.add( dependencyModel.rawInjectionType() );
                }
            }
        );
    }
}
