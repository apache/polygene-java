/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.zest.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.zest.api.common.ConstructionException;
import org.apache.zest.api.composite.CompositeInstance;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Initializable;
import org.apache.zest.api.mixin.InitializationException;
import org.apache.zest.api.mixin.MixinDescriptor;
import org.apache.zest.api.property.StateHolder;
import org.apache.zest.functional.HierarchicalVisitor;
import org.apache.zest.functional.VisitableHierarchy;
import org.apache.zest.runtime.injection.Dependencies;
import org.apache.zest.runtime.injection.DependencyModel;
import org.apache.zest.runtime.injection.InjectedFieldsModel;
import org.apache.zest.runtime.injection.InjectedMethodsModel;
import org.apache.zest.runtime.injection.InjectionContext;

/**
 * JAVADOC
 */
public final class MixinModel
    implements MixinDescriptor, VisitableHierarchy<Object, Object>, Dependencies
{
    private final Class<?> mixinClass;
    private final Class<?> instantiationClass;
    private final ConstructorsModel constructorsModel;
    private final InjectedFieldsModel injectedFieldsModel;
    private final InjectedMethodsModel injectedMethodsModel;
    private final List<Class<?>> thisMixinTypes;

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

    public Stream<DependencyModel> dependencies()
    {
        Stream<? extends Dependencies> models = Stream.of( constructorsModel, injectedFieldsModel, injectedMethodsModel );
        return models.flatMap( Dependencies::dependencies );
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
            catch( IllegalAccessException | NoSuchFieldException e )
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
            catch( Exception e )
            {
                List<Class<?>> compositeType = compositeInstance.types().collect( Collectors.toList() );
                String message = "Unable to initialize " + mixinClass + " in composite " + compositeType;
                throw new ConstructionException( new InitializationException( message, e ) );
            }
        }
        return mixin;
    }

    public Iterable<Class<?>> thisMixinTypes()
    {
        return thisMixinTypes;
    }

    private List<Class<?>> buildThisMixinTypes()
    {
        return dependencies()
            .filter( new DependencyModel.ScopeSpecification( This.class ) )
            .distinct()
            .map( DependencyModel::rawInjectionType )
            .collect( Collectors.toList() );
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
