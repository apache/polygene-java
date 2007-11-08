/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

import org.qi4j.CompositeBuilderFactory;
import org.qi4j.CompositeRegistry;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.annotation.scope.Adapt;
import org.qi4j.annotation.scope.ConcernFor;
import org.qi4j.annotation.scope.Decorate;
import org.qi4j.annotation.scope.Invocation;
import org.qi4j.annotation.scope.PropertyField;
import org.qi4j.annotation.scope.PropertyParameter;
import org.qi4j.annotation.scope.Service;
import org.qi4j.annotation.scope.SideEffectFor;
import org.qi4j.annotation.scope.Structure;
import org.qi4j.annotation.scope.ThisCompositeAs;
import org.qi4j.dependency.DependencyResolverRegistry;
import org.qi4j.runtime.resolution.AdaptDependencyResolver;
import org.qi4j.runtime.resolution.CompositeModelResolver;
import org.qi4j.runtime.resolution.ConcernModelResolver;
import org.qi4j.runtime.resolution.DecorateDependencyResolver;
import org.qi4j.runtime.resolution.DependencyResolverRegistryImpl;
import org.qi4j.runtime.resolution.InvocationDependencyResolver;
import org.qi4j.runtime.resolution.MixinModelResolver;
import org.qi4j.runtime.resolution.ModifiesDependencyResolver;
import org.qi4j.runtime.resolution.ObjectModelResolver;
import org.qi4j.runtime.resolution.PropertyDependencyResolver;
import org.qi4j.runtime.resolution.ServiceDependencyResolver;
import org.qi4j.runtime.resolution.SideEffectModelResolver;
import org.qi4j.runtime.resolution.StructureDependencyResolver;
import org.qi4j.runtime.resolution.ThisCompositeAsDependencyResolver;
import org.qi4j.runtime.structure.ApplicationBuilderFactoryImpl;
import org.qi4j.runtime.structure.AssemblyRegistryImpl;
import org.qi4j.structure.ApplicationBuilderFactory;
import org.qi4j.structure.AssemblyRegistry;
import org.qi4j.structure.CompositeMapper;

/**
 * Incarnation of Qi4j.
 */
public class Energy4Java
    implements Qi4jRuntime
{
    Qi4jRuntime delegate;

    // API
    private ApplicationBuilderFactory applicationBuilderFactory;
    private CompositeBuilderFactory compositeBuilderFactory;
    private CompositeRegistry compositeRegistry;
    private ObjectBuilderFactory objectBuilderFactory;
    private AssemblyRegistry assemblyRegistry;

    // SPI
    private DependencyResolverRegistryImpl dependencyResolverRegistry;

    // Runtime
    private InstanceFactory instanceFactory;
    private CompositeModelFactory compositeModelFactory;
    private ObjectModelFactory objectModelFactory;
    private ServiceDependencyResolver serviceDependencyResolver;

    public Energy4Java()
    {
        this( null, null, null, null, null, null, null, null );
    }

    public Energy4Java( InstanceFactory instanceFactory, // Runtime
                        CompositeModelFactory compositeModelFactory,
                        ObjectModelFactory objectModelFactory,
                        DependencyResolverRegistryImpl dependencyResolverRegistry, // SPI
                        CompositeBuilderFactory compositeBuilderFactory, // API
                        CompositeRegistry compositeRegistry,
                        ObjectBuilderFactory objectBuilderFactory,
                        AssemblyRegistry assemblyRegistry )
    {
        // Runtime
        if( instanceFactory == null )
        {
            instanceFactory = new InstanceFactoryImpl();
        }

        if( compositeModelFactory == null )
        {
            compositeModelFactory = new CompositeModelFactory();
        }

        if( objectModelFactory == null )
        {
            objectModelFactory = new ObjectModelFactory();
        }

        // SPI
        if( dependencyResolverRegistry == null )
        {
            dependencyResolverRegistry = new DependencyResolverRegistryImpl();
            dependencyResolverRegistry.setDependencyResolver( ThisCompositeAs.class, new ThisCompositeAsDependencyResolver() );
            dependencyResolverRegistry.setDependencyResolver( ConcernFor.class, new ModifiesDependencyResolver() );
            dependencyResolverRegistry.setDependencyResolver( SideEffectFor.class, new ModifiesDependencyResolver() );
            dependencyResolverRegistry.setDependencyResolver( Invocation.class, new InvocationDependencyResolver() );
            dependencyResolverRegistry.setDependencyResolver( Adapt.class, new AdaptDependencyResolver() );
            dependencyResolverRegistry.setDependencyResolver( Decorate.class, new DecorateDependencyResolver() );
            PropertyDependencyResolver dependencyResolver = new PropertyDependencyResolver();
            dependencyResolverRegistry.setDependencyResolver( PropertyField.class, dependencyResolver );
            dependencyResolverRegistry.setDependencyResolver( PropertyParameter.class, dependencyResolver );
            dependencyResolverRegistry.setDependencyResolver( Structure.class, new StructureDependencyResolver( this ) );
            serviceDependencyResolver = new ServiceDependencyResolver( this );
            dependencyResolverRegistry.setDependencyResolver( Service.class, serviceDependencyResolver );
        }

        // API
        if( compositeRegistry == null )
        {
            compositeRegistry = new CompositeRegistryImpl();
        }

        if( compositeBuilderFactory == null )
        {
            ConcernModelResolver concernModelResolver = new ConcernModelResolver( dependencyResolverRegistry );
            SideEffectModelResolver sideEffectModelResolver = new SideEffectModelResolver( dependencyResolverRegistry );
            MixinModelResolver mixinModelResolver = new MixinModelResolver( dependencyResolverRegistry );
            CompositeModelResolver compositeModelResolver = new CompositeModelResolver( concernModelResolver, sideEffectModelResolver, mixinModelResolver );
            compositeBuilderFactory = new CompositeBuilderFactoryImpl( instanceFactory, compositeModelFactory, compositeModelResolver );
        }

        if( objectBuilderFactory == null )
        {
            ObjectModelResolver modelResolver = new ObjectModelResolver( dependencyResolverRegistry );
            objectBuilderFactory = new ObjectBuilderFactoryImpl( instanceFactory, objectModelFactory, modelResolver );
        }

        if( assemblyRegistry == null )
        {
            assemblyRegistry = new AssemblyRegistryImpl( this );
        }

        if( applicationBuilderFactory == null )
        {
            applicationBuilderFactory = new ApplicationBuilderFactoryImpl( this );
        }

        // Runtime
        this.instanceFactory = instanceFactory;
        this.compositeModelFactory = compositeModelFactory;

        // SPI
        this.dependencyResolverRegistry = dependencyResolverRegistry;

        // API
        this.compositeBuilderFactory = compositeBuilderFactory;
        this.compositeRegistry = compositeRegistry;
        this.objectBuilderFactory = objectBuilderFactory;
        this.assemblyRegistry = assemblyRegistry;
    }

    // API
    public ApplicationBuilderFactory getApplicationBuilderFactory()
    {
        return applicationBuilderFactory;
    }

    public CompositeBuilderFactory newCompositeBuilderFactory()
    {
        ConcernModelResolver concernModelResolver = new ConcernModelResolver( dependencyResolverRegistry );
        SideEffectModelResolver sideEffectModelResolver = new SideEffectModelResolver( dependencyResolverRegistry );
        MixinModelResolver mixinModelResolver = new MixinModelResolver( dependencyResolverRegistry );
        CompositeModelResolver compositeModelResolver = new CompositeModelResolver( concernModelResolver, sideEffectModelResolver, mixinModelResolver );
        return new CompositeBuilderFactoryImpl( instanceFactory, compositeModelFactory, compositeModelResolver );
    }

    public ObjectBuilderFactory newObjectBuilderFactory()
    {
        ObjectModelResolver modelResolver = new ObjectModelResolver( dependencyResolverRegistry );
        return new ObjectBuilderFactoryImpl( instanceFactory, objectModelFactory, modelResolver );
    }

    public CompositeRegistry getCompositeRegistry()
    {
        return compositeRegistry;
    }

    public CompositeMapper getCompositeMapper()
    {
        return compositeRegistry;
    }

    public AssemblyRegistry getAssemblyRegistry()
    {
        return assemblyRegistry;
    }

    // SPI
    public DependencyResolverRegistry getDependencyResolverRegistry()
    {
        return dependencyResolverRegistry;
    }

    // Runtime
    public InstanceFactory newInstanceFactory()
    {
        return new InstanceFactoryImpl();
    }

    public CompositeModelFactory newCompositeModelFactory()
    {
        return new CompositeModelFactory();
    }

    public ObjectModelFactory newObjectModelFactory()
    {
        return new ObjectModelFactory();
    }

    public ServiceDependencyResolver getServiceDependencyResolver()
    {
        return serviceDependencyResolver;
    }
}
