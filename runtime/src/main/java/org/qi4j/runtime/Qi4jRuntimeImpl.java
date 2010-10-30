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
import java.lang.reflect.Proxy;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.PropertyMapper;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.UnknownServiceReferenceType;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.ApplicationModelFactory;
import org.qi4j.bootstrap.Qi4jRuntime;
import org.qi4j.runtime.bootstrap.ApplicationAssemblyFactoryImpl;
import org.qi4j.runtime.bootstrap.ApplicationModelFactoryImpl;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.TransientInstance;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.service.ImportedServiceReferenceInstance;
import org.qi4j.runtime.service.ServiceInstance;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.service.ServiceReferenceInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.runtime.structure.ModuleVisitor;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.composite.TransientDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.value.ValueDescriptor;

import static java.lang.reflect.Proxy.getInvocationHandler;
import static org.qi4j.runtime.composite.TransientInstance.getCompositeInstance;

/**
 * Incarnation of Qi4j.
 */
public final class Qi4jRuntimeImpl
    implements Qi4jSPI, Qi4jRuntime, Serializable
{
    private ApplicationAssemblyFactory applicationAssemblyFactory;
    private ApplicationModelFactory applicationModelFactory;

    public Qi4jRuntimeImpl()
    {
        applicationAssemblyFactory = new ApplicationAssemblyFactoryImpl();
        applicationModelFactory = new ApplicationModelFactoryImpl();
    }

    public ApplicationAssemblyFactory applicationAssemblyFactory()
    {
        return applicationAssemblyFactory;
    }

    public ApplicationModelFactory applicationModelFactory()
    {
        return applicationModelFactory;
    }

    public Qi4jSPI spi()
    {
        return this;
    }

    // API

    public <T> T dereference( T composite )
    {
        InvocationHandler handler = getInvocationHandler( composite );
        if( handler instanceof ProxyReferenceInvocationHandler )
        {
            return (T) ( (ProxyReferenceInvocationHandler) handler ).proxy();
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

    public <T> T getConfigurationInstance( ServiceComposite serviceComposite, UnitOfWork uow )
        throws InstantiationException
    {
        ServiceModel serviceModel = (ServiceModel) TransientInstance.getCompositeInstance( serviceComposite )
            .compositeModel();

        String identity = serviceComposite.identity().get();
        T configuration;
        try
        {
            configuration = uow.get( serviceModel.<T>configurationType(), identity );
            uow.pause();
        }
        catch( NoSuchEntityException e )
        {
            return (T) initializeConfigurationInstance( serviceComposite, uow, serviceModel, identity );
        }
        catch( EntityTypeNotFoundException e )
        {
            return (T) initializeConfigurationInstance( serviceComposite, uow, serviceModel, identity );
        }
        return (T) configuration;
    }

    private <T> T initializeConfigurationInstance( ServiceComposite serviceComposite,
                                                   UnitOfWork uow,
                                                   ServiceModel serviceModel,
                                                   String identity
    )
        throws InstantiationException
    {
        T configuration;
        Module module = ServiceInstance.getCompositeInstance( serviceComposite ).module();
        UnitOfWork buildUow = module.unitOfWorkFactory().newUnitOfWork();

        EntityBuilder<T> configBuilder = buildUow.newEntityBuilder( serviceModel.<T>configurationType(), identity );

        // Check for defaults
        String s = identity + ".properties";
        InputStream asStream = serviceComposite.type().getResourceAsStream( s );
        if( asStream != null )
        {
            try
            {
                PropertyMapper.map( asStream, (Composite) configBuilder.instance() );
            }
            catch( IOException e1 )
            {
                InstantiationException exception = new InstantiationException(
                    "Could not read underlying Properties file." );
                exception.initCause( e1 );
                throw exception;
            }
        }

        try
        {
            configuration = configBuilder.newInstance();
            buildUow.complete();

            // Try again
            return (T) getConfigurationInstance( serviceComposite, uow );
        }
        catch( Exception e1 )
        {
            InstantiationException ex = new InstantiationException(
                "Could not instantiate configuration, and no Properties file was found (" + s + ")" );
            ex.initCause( e1 );
            throw ex;
        }
    }

    public Class<?> getConfigurationType( Composite serviceComposite )
    {
        ServiceModel descriptor = (ServiceModel) ServiceInstance.getCompositeInstance( serviceComposite )
            .compositeModel();
        return descriptor.calculateConfigurationType();
    }

    public Module getModule( UnitOfWork uow )
    {
        return ( (ModuleUnitOfWork) uow ).module();
    }

    public Module getModule( ServiceReference service )
    {
        if( service instanceof ServiceReferenceInstance )
        {
            return ( (ServiceReferenceInstance) service ).module();
        }
        else if( service instanceof ImportedServiceReferenceInstance )
        {
            return ( (ImportedServiceReferenceInstance) service ).module();
        }
        else
        {
            throw new UnknownServiceReferenceType( "ServiceReference type is not known: ", service );
        }
    }

    public Module getModule( Composite composite )
    {
        if( composite instanceof TransientComposite )
        {
            return TransientInstance.getCompositeInstance( composite ).module();
        }
        else if( composite instanceof EntityComposite )
        {
            return EntityInstance.getEntityInstance( (EntityComposite) composite ).module();
        }
        else if( composite instanceof ValueComposite )
        {
            return ValueInstance.getValueInstance( (ValueComposite) composite ).module();
        }
        else if( composite instanceof ServiceComposite )
        {
            return ( (ServiceReferenceInstance.ServiceInvocationHandler) Proxy.getInvocationHandler(
                composite ) ).module();
        }
        else
        {
            return null;
        }
    }

    // SPI

    public TransientDescriptor getTransientDescriptor( TransientComposite composite )
    {
        TransientInstance transientInstance = getCompositeInstance( composite );
        return (TransientDescriptor) transientInstance.compositeModel();
    }

    public StateHolder getState( TransientComposite composite )
    {
        return TransientInstance.getCompositeInstance( composite ).state();
    }

    public EntityDescriptor getEntityDescriptor( EntityComposite composite )
    {
        EntityInstance entityInstance = (EntityInstance) getInvocationHandler( composite );
        return entityInstance.entityModel();
    }

    class EntityFinder
        implements ModuleVisitor<RuntimeException>
    {
        Class type;
        EntityModel model;

        public boolean visitModule( ModuleInstance moduleInstance, ModuleModel moduleModel, Visibility visibility )
        {
            model = moduleModel.entities().getEntityModelFor( type, visibility );
            return model == null;
        }
    }

    public EntityState getEntityState( EntityComposite composite )
    {
        return EntityInstance.getEntityInstance( composite ).entityState();
    }

    public EntityStateHolder getState( EntityComposite composite )
    {
        return EntityInstance.getEntityInstance( composite ).state();
    }

    public ValueDescriptor getValueDescriptor( ValueComposite value )
    {
        ValueInstance valueInstance = ValueInstance.getValueInstance( value );
        return (ValueDescriptor) valueInstance.compositeModel();
    }

    public StateHolder getState( ValueComposite composite )
    {
        return ValueInstance.getValueInstance( composite ).state();
    }

    public ServiceDescriptor getServiceDescriptor( ServiceReference service )
    {
        if( service instanceof ServiceReferenceInstance )
        {
            ServiceReferenceInstance ref = (ServiceReferenceInstance) service;
            return ref.serviceDescriptor();
        }
        else
        {
            return null;
        }
    }
}
