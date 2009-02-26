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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import static java.lang.reflect.Proxy.getInvocationHandler;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.PropertyMapper;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.runtime.composite.DefaultCompositeInstance;
import static org.qi4j.runtime.composite.DefaultCompositeInstance.getCompositeInstance;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.structure.CompositesInstance;
import org.qi4j.runtime.structure.CompositesModel;
import org.qi4j.runtime.structure.DependencyVisitor;
import org.qi4j.runtime.structure.EntitiesInstance;
import org.qi4j.runtime.structure.EntitiesModel;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.runtime.bootstrap.ApplicationAssemblyFactoryImpl;
import org.qi4j.runtime.bootstrap.ApplicationFactoryImpl;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.bootstrap.spi.Qi4jRuntime;
import org.qi4j.bootstrap.spi.ApplicationFactory;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;

/**
 * Incarnation of Qi4j.
 */
public final class Qi4jRuntimeImpl
    implements Qi4jSPI, Qi4jRuntime, Serializable
{
    ApplicationAssemblyFactory applicationAssemblyFactory;
    ApplicationFactory applicationFactory;

    public Qi4jRuntimeImpl()
    {
        applicationAssemblyFactory = new ApplicationAssemblyFactoryImpl();
        applicationFactory = new ApplicationFactoryImpl(this);
    }

    public ApplicationAssemblyFactory applicationAssemblyFactory()
    {
        return applicationAssemblyFactory;
    }

    public ApplicationFactory applicationFactory()
    {
        return applicationFactory;
    }

    public Qi4jSPI spi()
    {
        return this;
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

    public <T> T getConfigurationInstance( Composite serviceComposite, UnitOfWork uow )
        throws InstantiationException
    {
        ServiceModel serviceModel = (ServiceModel) DefaultCompositeInstance.getCompositeInstance( serviceComposite ).compositeModel();

        String identity = ((ServiceComposite) serviceComposite).identity().get();
        Object configuration;
        try
        {
            configuration = uow.find( identity, serviceModel.configurationType() );
        }
        catch( NoSuchEntityException e )
        {

            EntityBuilder<? extends T> configBuilder = uow.newEntityBuilder( identity, serviceModel.configurationType() );
            // Check for defaults
            String s = identity + ".properties";
            InputStream asStream = serviceComposite.type().getResourceAsStream( s );
            if( asStream != null )
            {
                try
                {
                    PropertyMapper.map( asStream, (Composite) configBuilder.stateOfComposite() );
                }
                catch( IOException e1 )
                {
                    InstantiationException exception = new InstantiationException( "Could not read underlying Properties file." );
                    exception.initCause( e1 );
                    throw exception;
                }
            }
            configuration = configBuilder.newInstance();
            try
            {
                uow.apply();
            }
            catch( UnitOfWorkCompletionException e1 )
            {
                InstantiationException exception = new InstantiationException( "Underlying EntityStore is unable to complete." );
                exception.initCause( e1 );
                throw exception;
            }
        }
        finally
        {
            uow.pause();
        }
        return (T) configuration;
    }

    public Class<?> getConfigurationType( Composite serviceComposite )
    {
        ServiceModel descriptor = (ServiceModel) getCompositeDescriptor( serviceComposite );
        final List<DependencyModel> dependencyModels = new ArrayList<DependencyModel>();
        descriptor.visitModel( new DependencyVisitor( new DependencyModel.ScopeSpecification( This.class ) )
        {
            @Override
            public void visitDependency( DependencyModel dependencyModel )
            {
                dependencyModels.add( dependencyModel );
            }
        } );

        Class injectionClass = null;
        for( DependencyModel dependencyModel : dependencyModels )
        {
            if( dependencyModel.rawInjectionType().equals( Configuration.class ) )
            {
                if( injectionClass == null )
                {
                    injectionClass = dependencyModel.injectionClass();
                }
                else
                {
                    if( injectionClass.isAssignableFrom( dependencyModel.injectionClass() ) )
                    {
                        injectionClass = dependencyModel.injectionClass();
                    }
                }
            }
        }

        return injectionClass;
    }

    public Module getModule( UnitOfWork uow )
    {
        return ((UnitOfWorkInstance) uow).module();
    }

    public Module getModule( Composite composite )
    {
        return ((CompositeInstance) composite).module();
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

    public StateHolder getState( Composite composite )
    {
        return DefaultCompositeInstance.getCompositeInstance( composite ).state();
    }

    public EntityDescriptor getEntityDescriptor( EntityComposite composite )
    {
        EntityInstance entityInstance = (EntityInstance) getInvocationHandler( composite );
        return entityInstance.entityModel();
    }

    @SuppressWarnings( "unchecked" )
    public EntityDescriptor getEntityDescriptor( Class mixinType, Module module )
    {
        ModuleInstance moduleInstance = (ModuleInstance) module;
        return moduleInstance.findEntityCompositeFor( mixinType );
    }

    public EntityState getEntityState( EntityComposite composite )
    {
        return EntityInstance.getEntityInstance( composite ).load();
    }

    public EntityStateHolder getState( EntityComposite composite )
    {
        return EntityInstance.getEntityInstance( composite ).state();
    }

    @SuppressWarnings( "unchecked" )
    public ObjectDescriptor getObjectDescriptor( Class objectType, Module module )
    {
        ModuleInstance moduleInstance = (ModuleInstance) module;
        return moduleInstance.findObjectFor( objectType );
    }

    public void setMixins( Composite composite, Object[] mixins )
    {
        DefaultCompositeInstance instance = getCompositeInstance( composite );

        instance.setMixins( mixins );
    }
}
