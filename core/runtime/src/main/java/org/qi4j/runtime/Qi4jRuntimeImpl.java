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
import java.util.Arrays;
import org.qi4j.api.Qi4j;
import org.qi4j.api.association.AbstractAssociation;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.AssociationWrapper;
import org.qi4j.api.association.ManyAssociationWrapper;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.composite.ModelDescriptor;
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

    public Module getModule( Object compositeOrServiceReferenceOrUow )
    {
        if( compositeOrServiceReferenceOrUow instanceof TransientComposite )
        {
            TransientComposite composite = (TransientComposite) compositeOrServiceReferenceOrUow;
            return TransientInstance.getCompositeInstance( composite ).module();
        }
        else if( compositeOrServiceReferenceOrUow instanceof EntityComposite )
        {
            EntityComposite composite = (EntityComposite) compositeOrServiceReferenceOrUow;
            return EntityInstance.getEntityInstance( composite ).module();
        }
        else if( compositeOrServiceReferenceOrUow instanceof ValueComposite )
        {
            ValueComposite composite = (ValueComposite) compositeOrServiceReferenceOrUow;
            return ValueInstance.getValueInstance( composite ).module();
        }
        else if( compositeOrServiceReferenceOrUow instanceof ServiceComposite )
        {
            ServiceComposite composite = (ServiceComposite) compositeOrServiceReferenceOrUow;
            InvocationHandler handler = Proxy.getInvocationHandler( composite );
            if( handler instanceof ServiceInstance )
            {
                return ( (ServiceInstance) handler ).module();
            }
            return ( (ServiceReferenceInstance.ServiceInvocationHandler) handler ).module();
        }
        else if( compositeOrServiceReferenceOrUow instanceof UnitOfWork )
        {
            ModuleUnitOfWork unitOfWork = (ModuleUnitOfWork) compositeOrServiceReferenceOrUow;
            return unitOfWork.module();
        }
        else if( compositeOrServiceReferenceOrUow instanceof ServiceReferenceInstance )
        {
            ServiceReferenceInstance reference = (ServiceReferenceInstance) compositeOrServiceReferenceOrUow;
            return reference.module();
        }
        else if( compositeOrServiceReferenceOrUow instanceof ImportedServiceReferenceInstance )
        {
            ImportedServiceReferenceInstance importedServiceReference =
                (ImportedServiceReferenceInstance) compositeOrServiceReferenceOrUow;
            return importedServiceReference.module();
        }
        throw new IllegalArgumentException( "Wrong type. Must be one of " + Arrays.asList(
            ValueComposite.class, ServiceComposite.class, TransientComposite.class,
            ServiceComposite.class, ServiceReference.class, UnitOfWork.class ) );
    }

    @Override
    public ModelDescriptor getModelDescriptor( Object compositeOrServiceReference )
    {
        if( compositeOrServiceReference instanceof TransientComposite )
        {
            TransientComposite composite = (TransientComposite) compositeOrServiceReference;
            return TransientInstance.getCompositeInstance( composite ).descriptor();
        }
        else if( compositeOrServiceReference instanceof EntityComposite )
        {
            EntityComposite composite = (EntityComposite) compositeOrServiceReference;
            return EntityInstance.getEntityInstance( composite ).descriptor();
        }
        else if( compositeOrServiceReference instanceof ValueComposite )
        {
            ValueComposite composite = (ValueComposite) compositeOrServiceReference;
            return ValueInstance.getValueInstance( composite ).descriptor();
        }
        else if( compositeOrServiceReference instanceof ServiceComposite )
        {
            ServiceComposite composite = (ServiceComposite) compositeOrServiceReference;
            InvocationHandler handler = Proxy.getInvocationHandler( composite );
            if( handler instanceof ServiceInstance )
            {
                return ( (ServiceInstance) handler ).descriptor();
            }
            return ( (ServiceReferenceInstance.ServiceInvocationHandler) handler ).descriptor();
        }
        else if( compositeOrServiceReference instanceof ServiceReferenceInstance )
        {
            ServiceReferenceInstance reference = (ServiceReferenceInstance) compositeOrServiceReference;
            return reference.serviceDescriptor();
        }
        else if( compositeOrServiceReference instanceof ImportedServiceReferenceInstance )
        {
            ImportedServiceReferenceInstance importedServiceReference =
                (ImportedServiceReferenceInstance) compositeOrServiceReference;
            return importedServiceReference.serviceDescriptor();
        }
        throw new IllegalArgumentException( "Wrong type. Must be one of " + Arrays.asList(
            ValueComposite.class, ServiceComposite.class, TransientComposite.class,
            ServiceComposite.class, ServiceReference.class ) );
    }

    @Override
    public CompositeDescriptor getCompositeDescriptor( Object compositeOrServiceReference )
    {
        return (CompositeDescriptor) getModelDescriptor( compositeOrServiceReference );
    }

    // Descriptors

    public TransientDescriptor getTransientDescriptor( Object transsient )
    {
        if( transsient instanceof TransientComposite )
        {
            TransientInstance transientInstance = getCompositeInstance( (Composite) transsient );
            return (TransientDescriptor) transientInstance.descriptor();
        }
        throw new IllegalArgumentException( "Wrong type. Must be subtype of " + TransientComposite.class );
    }

    public StateHolder getState( TransientComposite composite )
    {
        return TransientInstance.getCompositeInstance( composite ).state();
    }

    public EntityDescriptor getEntityDescriptor( Object entity )
    {
        if( entity instanceof EntityComposite )
        {
            EntityInstance entityInstance = (EntityInstance) getInvocationHandler( entity );
            return entityInstance.entityModel();
        }
        throw new IllegalArgumentException( "Wrong type. Must be subtype of " + EntityComposite.class );
    }

    public AssociationStateHolder getState( EntityComposite composite )
    {
        return EntityInstance.getEntityInstance( composite ).state();
    }

    public ValueDescriptor getValueDescriptor( Object value )
    {
        if( value instanceof ValueComposite )
        {
            ValueInstance valueInstance = ValueInstance.getValueInstance( (ValueComposite) value );
            return valueInstance.descriptor();
        }
        throw new IllegalArgumentException( "Wrong type. Must be subtype of " + ValueComposite.class );
    }

    public AssociationStateHolder getState( ValueComposite composite )
    {
        return ValueInstance.getValueInstance( composite ).state();
    }

    public ServiceDescriptor getServiceDescriptor( Object service )
    {
        if( service instanceof ServiceReferenceInstance )
        {
            ServiceReferenceInstance ref = (ServiceReferenceInstance) service;
            return ref.serviceDescriptor();
        }
        if( service instanceof ServiceComposite )
        {
            ServiceComposite composite = (ServiceComposite) service;
            return (ServiceDescriptor) ServiceInstance.getCompositeInstance( composite ).descriptor();
        }
        String message = "Wrong type. Must be subtype of " + ServiceComposite.class + " or " + ServiceReference.class;
        throw new IllegalArgumentException( message );
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
