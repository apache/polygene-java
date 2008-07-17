/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

import java.lang.reflect.InvocationHandler;
import static java.lang.reflect.Proxy.getInvocationHandler;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.Composite;
import org.qi4j.entity.EntityComposite;
import org.qi4j.injection.scope.This;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.composite.DefaultCompositeInstance;
import static org.qi4j.runtime.composite.DefaultCompositeInstance.getCompositeInstance;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.CompositesInstance;
import org.qi4j.runtime.structure.CompositesModel;
import org.qi4j.runtime.structure.DependencyVisitor;
import org.qi4j.runtime.structure.EntitiesInstance;
import org.qi4j.runtime.structure.EntitiesModel;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.service.Configuration;
import org.qi4j.service.ServiceComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.structure.Module;

/**
 * Incarnation of Qi4j.
 */
public final class Qi4jRuntime
    implements Qi4jSPI
{
    public Qi4jRuntime()
    {
    }

    // API
    public Composite dereference( Composite composite )
    {
        InvocationHandler handler = getInvocationHandler( composite );
        if( handler instanceof ProxyReferenceInvocationHandler )
        {
            return (Composite) ( (ProxyReferenceInvocationHandler) handler ).proxy();
        }
        if( handler instanceof CompositeInstance )
        {
            return composite;
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public <S extends Composite, T extends S> Class<S> getSuperComposite( Class<T> compositeClass )
    {
        Class<?>[] extendedInterfaces = compositeClass.getInterfaces();
        for( Class<?> extendedInterface : extendedInterfaces )
        {
            if( Composite.class.isAssignableFrom( extendedInterface ) &&
                !Composite.class.equals( extendedInterface ) &&
                !EntityComposite.class.equals( extendedInterface ) &&
                !ServiceComposite.class.equals( extendedInterface )
                )
            {
                return (Class<S>) extendedInterface;
            }
        }
        return null; // No super Composite type found
    }

    public Class<?> getConfigurationType( Composite serviceComposite )
    {
        CompositeModel descriptor = (CompositeModel) getCompositeDescriptor( serviceComposite );
        final List<DependencyModel> dependencyModels = new ArrayList<DependencyModel>();
        descriptor.visitModel( new DependencyVisitor( new DependencyModel.ScopeSpecification( This.class ) )
        {
            @Override
            public void visitDependency( DependencyModel dependencyModel )
            {
                dependencyModels.add( dependencyModel );
            }
        } );

        for( DependencyModel dependencyModel : dependencyModels )
        {
            if( dependencyModel.rawInjectionType().equals( Configuration.class ) )
            {
                return dependencyModel.injectionClass();
            }
        }

        return null;
    }

    // SPI
    public CompositeDescriptor getCompositeDescriptor( Composite composite )
    {
        Class<? extends Composite> compositeClass = composite.getClass();
        if( EntityComposite.class.isAssignableFrom( compositeClass ) )
        {
            EntityInstance entityInstance = (EntityInstance) getInvocationHandler( composite );
            return entityInstance.entityModel();
        }
        else
        {
            DefaultCompositeInstance defaultCompositeInstance = getCompositeInstance( composite );
            return defaultCompositeInstance.compositeModel();
        }
    }

    @SuppressWarnings( "unchecked" )
    public CompositeDescriptor getCompositeDescriptor( Class mixinType, Module module )
    {
        ModuleInstance moduleInstance = (ModuleInstance) module;
        if( !Composite.class.isAssignableFrom( mixinType ) )
        {
            EntitiesInstance entitiesInstance = moduleInstance.entities();
            EntitiesModel entitiesModel = entitiesInstance.model();
            EntityModel entityModel = entitiesModel.getEntityModelFor( mixinType );

            if( entityModel != null )
            {
                return entityModel;
            }

            // It's not entities, let's try composite
            CompositesInstance compositesInstance = moduleInstance.composites();
            CompositesModel compositesModel = compositesInstance.model();
            return compositesModel.getCompositeModelFor( mixinType );
        }
        else if( EntityComposite.class.isAssignableFrom( mixinType ) )
        {
            return moduleInstance.findEntityCompositeFor( mixinType );
        }

        return moduleInstance.findCompositeFor( mixinType );
    }

    public void setMixins( Composite composite, Object[] mixins )
    {
        DefaultCompositeInstance instance = getCompositeInstance( composite );

        instance.setMixins( mixins );
    }
}
