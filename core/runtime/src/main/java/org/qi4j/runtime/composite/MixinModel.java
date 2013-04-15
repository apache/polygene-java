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
import java.util.List;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.mixin.MixinDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;

import static org.qi4j.functional.Iterables.map;
import static org.qi4j.functional.Iterables.toList;
import static org.qi4j.functional.Iterables.unique;

/**
 * JAVADOC
 */
public final class MixinModel
    implements MixinDescriptor, VisitableHierarchy<Object, Object>
{
    private final Class<?> mixinClass;
    private final Class<?> instantiationClass;
    private final ConstructorsModel constructorsModel;
    private final InjectedFieldsModel injectedFieldsModel;
    private final InjectedMethodsModel injectedMethodsModel;
    private final Iterable<Class<?>> thisMixinTypes;

    public MixinModel( Class<?> declaredMixinClass, Class<?> instantiationClass )
    {
        injectedFieldsModel = new InjectedFieldsModel( declaredMixinClass );
        injectedMethodsModel = new InjectedMethodsModel( declaredMixinClass );

        this.mixinClass = declaredMixinClass;
        this.instantiationClass = instantiationClass;
        constructorsModel = new ConstructorsModel( instantiationClass );

        thisMixinTypes = buildThisMixinTypes();
    }

    @Override
    public Class<?> mixinClass()
    {
        return mixinClass;
    }

    public Class<?> instantiationClass()
    {
        return instantiationClass;
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( mixinClass );
    }

    public Iterable<DependencyModel> dependencies()
    {
        return Iterables.flatten( constructorsModel.dependencies(), injectedFieldsModel.dependencies(), injectedMethodsModel
            .dependencies() );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            if( constructorsModel.accept( visitor ) )
            {
                if( injectedFieldsModel.accept( visitor ) )
                {
                    injectedMethodsModel.accept( visitor );
                }
            }
        }
        return visitor.visitLeave( this );
    }

    // Context
    public Object newInstance( CompositeInstance compositeInstance, StateHolder state, UsesInstance uses )
    {
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        return newInstance( injectionContext );
    }

    public Object newInstance( InjectionContext injectionContext )
    {
        Object mixin;
        CompositeInstance compositeInstance = injectionContext.compositeInstance();

        mixin = constructorsModel.newInstance( injectionContext );

        if( FragmentClassLoader.isGenerated( instantiationClass ) )
        {
            try
            {
                instantiationClass.getDeclaredField( "_instance" ).set( mixin,
                                                                        injectionContext.compositeInstance() );
            }
            catch( IllegalAccessException e )
            {
                e.printStackTrace();
            }
            catch( NoSuchFieldException e )
            {
                e.printStackTrace();
            }
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
                List<Class<?>> compositeType = toList( compositeInstance.types() );
                String message = "Unable to initialize " + mixinClass + " in composite " + compositeType;
                throw new ConstructionException( message, e );
            }
        }
        return mixin;
    }

    public Iterable<Class<?>> thisMixinTypes()
    {
        return thisMixinTypes;
    }

    private Iterable<Class<?>> buildThisMixinTypes()
    {
        return map( new DependencyModel.InjectionTypeFunction(), unique( Iterables.filter( new DependencyModel.ScopeSpecification( This.class ), dependencies() ) ) );
    }

    protected FragmentInvocationHandler newInvocationHandler( Method method )
    {
        if( InvocationHandler.class.isAssignableFrom( mixinClass )
            && !method.getDeclaringClass().isAssignableFrom( mixinClass ) )
        {
            return new GenericFragmentInvocationHandler();
        }
        else
        {
            return new TypedModifierInvocationHandler();
        }
    }

    @Override
    public String toString()
    {
        return mixinClass.getName();
    }

}
