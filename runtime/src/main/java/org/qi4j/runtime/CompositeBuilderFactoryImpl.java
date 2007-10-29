/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.annotation.scope.Adapt;
import org.qi4j.api.annotation.scope.ConcernFor;
import org.qi4j.api.annotation.scope.Decorate;
import org.qi4j.api.annotation.scope.Invocation;
import org.qi4j.api.annotation.scope.PropertyField;
import org.qi4j.api.annotation.scope.PropertyParameter;
import org.qi4j.api.annotation.scope.Qi4j;
import org.qi4j.api.annotation.scope.SideEffectFor;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.runtime.resolution.AdaptDependencyResolver;
import org.qi4j.runtime.resolution.CompositeModelResolver;
import org.qi4j.runtime.resolution.CompositeResolution;
import org.qi4j.runtime.resolution.ConcernModelResolver;
import org.qi4j.runtime.resolution.DecorateDependencyResolver;
import org.qi4j.runtime.resolution.DependencyResolverDelegator;
import org.qi4j.runtime.resolution.InvocationDependencyResolver;
import org.qi4j.runtime.resolution.MixinModelResolver;
import org.qi4j.runtime.resolution.ModifiesDependencyResolver;
import org.qi4j.runtime.resolution.PropertyDependencyResolver;
import org.qi4j.runtime.resolution.Qi4jDependencyResolver;
import org.qi4j.runtime.resolution.SideEffectModelResolver;
import org.qi4j.runtime.resolution.ThisAsDependencyResolver;

/**
 * Default implementation of CompositeBuilderFactory
 */
public final class CompositeBuilderFactoryImpl
    implements CompositeBuilderFactory
{
    private Map<Class<? extends Composite>, CompositeContextImpl> objectContexts;
    private CompositeModelFactory modelFactory;
    private InstanceFactory instanceFactory;
    private CompositeModelResolver compositeModelResolver;
    private DependencyResolverDelegator dependencyResolverDelegator;

    public CompositeBuilderFactoryImpl()
    {
        dependencyResolverDelegator = new DependencyResolverDelegator();

        dependencyResolverDelegator.setDependencyResolver( ThisAs.class, new ThisAsDependencyResolver() );
        dependencyResolverDelegator.setDependencyResolver( ConcernFor.class, new ModifiesDependencyResolver() );
        dependencyResolverDelegator.setDependencyResolver( SideEffectFor.class, new ModifiesDependencyResolver() );
        dependencyResolverDelegator.setDependencyResolver( Invocation.class, new InvocationDependencyResolver() );
        dependencyResolverDelegator.setDependencyResolver( Adapt.class, new AdaptDependencyResolver() );
        dependencyResolverDelegator.setDependencyResolver( Decorate.class, new DecorateDependencyResolver() );
        PropertyDependencyResolver dependencyResolver = new PropertyDependencyResolver();
        dependencyResolverDelegator.setDependencyResolver( PropertyField.class, dependencyResolver );
        dependencyResolverDelegator.setDependencyResolver( PropertyParameter.class, dependencyResolver );
        dependencyResolverDelegator.setDependencyResolver( Qi4j.class, new Qi4jDependencyResolver( this ) );

        ConcernModelResolver concernModelResolver = new ConcernModelResolver( dependencyResolverDelegator );
        SideEffectModelResolver sideEffectModelResolver = new SideEffectModelResolver( dependencyResolverDelegator );
        MixinModelResolver mixinModelResolver = new MixinModelResolver( dependencyResolverDelegator );
        compositeModelResolver = new CompositeModelResolver( concernModelResolver, sideEffectModelResolver, mixinModelResolver );

        modelFactory = new CompositeModelFactory();
        objectContexts = new ConcurrentHashMap<Class<? extends Composite>, CompositeContextImpl>();
        instanceFactory = new InstanceFactoryImpl();
    }

    public <T extends Composite> CompositeBuilder<T> newCompositeBuilder( Class<T> compositeType )
    {
        CompositeContextImpl<T> context = getCompositeContext( compositeType );
        CompositeBuilder<T> builder = new CompositeBuilderImpl<T>( context, instanceFactory );
        return builder;
    }

    public DependencyResolverDelegator getDependencyResolverDelegator()
    {
        return dependencyResolverDelegator;
    }

    private <T extends Composite> CompositeContextImpl<T> getCompositeContext( Class<T> compositeType )
    {
        CompositeContextImpl<T> context = objectContexts.get( compositeType );
        if( context == null )
        {
            CompositeModel<T> model = modelFactory.newCompositeModel( compositeType );
            CompositeResolution<T> resolution = compositeModelResolver.resolveCompositeModel( model );
            context = new CompositeContextImpl<T>( resolution, this, instanceFactory );
            objectContexts.put( compositeType, context );
        }
        return context;
    }
}