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
import java.lang.reflect.Proxy;
import org.qi4j.api.Qi4j;
import org.qi4j.api.association.AbstractAssociation;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.AssociationWrapper;
import org.qi4j.api.association.ManyAssociationWrapper;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.PropertyWrapper;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.ApplicationModelFactory;
import org.qi4j.bootstrap.Qi4jRuntime;
import org.qi4j.runtime.association.AbstractAssociationInstance;
import org.qi4j.runtime.bootstrap.ApplicationAssemblyFactoryImpl;
import org.qi4j.runtime.bootstrap.ApplicationModelFactoryImpl;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.runtime.composite.TransientInstance;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.property.PropertyInstance;
import org.qi4j.runtime.service.ImportedServiceReferenceInstance;
import org.qi4j.runtime.service.ServiceInstance;
import org.qi4j.runtime.service.ServiceReferenceInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityState;

import static java.lang.reflect.Proxy.getInvocationHandler;
import static org.qi4j.runtime.composite.TransientInstance.getCompositeInstance;

/**
 * Incarnation of Qi4j.
 */
public final class Qi4jRuntimeImpl
    implements Qi4jSPI, Qi4jRuntime
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

    public Qi4j api()
    {
        return this;
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
            throw new IllegalArgumentException( "ServiceReference type is not known: " + service );
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
            InvocationHandler handler = Proxy.getInvocationHandler( composite );
            if( handler instanceof ServiceInstance )
            {
                return ( (ServiceInstance) handler ).module();
            }
            return ( (ServiceReferenceInstance.ServiceInvocationHandler) handler ).module();
        }
        else
        {
            return null;
        }
    }

    // Descriptors

    public TransientDescriptor getTransientDescriptor( TransientComposite composite )
    {
        TransientInstance transientInstance = getCompositeInstance( composite );
        return (TransientDescriptor) transientInstance.descriptor();
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

    public AssociationStateHolder getState( EntityComposite composite )
    {
        return EntityInstance.getEntityInstance( composite ).state();
    }

    public ValueDescriptor getValueDescriptor( ValueComposite value )
    {
        ValueInstance valueInstance = ValueInstance.getValueInstance( value );
        return valueInstance.descriptor();
    }

    public AssociationStateHolder getState( ValueComposite composite )
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

    public ServiceDescriptor getServiceDescriptor( ServiceComposite service )
    {
        return (ServiceDescriptor) ServiceInstance.getCompositeInstance( service ).descriptor();
    }

    @Override
    public PropertyDescriptor getPropertyDescriptor( Property property )
    {
        while( property instanceof PropertyWrapper )
        {
            property = ( (PropertyWrapper) property ).getNext();
        }

        return (PropertyDescriptor) ( (PropertyInstance) property ).getPropertyInfo();
    }

    public AssociationDescriptor getAssociationDescriptor( AbstractAssociation association )
    {
        while( association instanceof AssociationWrapper )
        {
            association = ( (AssociationWrapper) association ).getNext();
        }

        while( association instanceof ManyAssociationWrapper )
        {
            association = ( (ManyAssociationWrapper) association ).getNext();
        }

        return (AssociationDescriptor) ( (AbstractAssociationInstance) association ).getAssociationInfo();
    }

    // SPI
    public EntityState getEntityState( EntityComposite composite )
    {
        return EntityInstance.getEntityInstance( composite ).entityState();
    }
}
