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
package org.apache.zest.runtime;

import java.lang.reflect.InvocationHandler;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.association.AbstractAssociation;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.association.AssociationWrapper;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.ManyAssociationWrapper;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.association.NamedAssociationWrapper;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.composite.CompositeDescriptor;
import org.apache.zest.api.composite.CompositeInstance;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.property.PropertyWrapper;
import org.apache.zest.api.property.StateHolder;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.bootstrap.ApplicationAssemblyFactory;
import org.apache.zest.bootstrap.ApplicationModelFactory;
import org.apache.zest.bootstrap.ZestRuntime;
import org.apache.zest.runtime.association.AbstractAssociationInstance;
import org.apache.zest.runtime.bootstrap.ApplicationAssemblyFactoryImpl;
import org.apache.zest.runtime.bootstrap.ApplicationModelFactoryImpl;
import org.apache.zest.runtime.composite.ProxyReferenceInvocationHandler;
import org.apache.zest.runtime.composite.TransientInstance;
import org.apache.zest.runtime.entity.EntityInstance;
import org.apache.zest.runtime.property.PropertyInstance;
import org.apache.zest.runtime.service.ImportedServiceReferenceInstance;
import org.apache.zest.runtime.service.ServiceInstance;
import org.apache.zest.runtime.service.ServiceReferenceInstance;
import org.apache.zest.runtime.unitofwork.ModuleUnitOfWork;
import org.apache.zest.runtime.value.ValueInstance;
import org.apache.zest.spi.ZestSPI;
import org.apache.zest.spi.entity.EntityState;

import static java.lang.reflect.Proxy.getInvocationHandler;
import static org.apache.zest.runtime.composite.TransientInstance.compositeInstanceOf;

/**
 * Incarnation of Zest.
 */
public final class ZestRuntimeImpl
    implements ZestSPI, ZestRuntime
{
    private final ApplicationAssemblyFactory applicationAssemblyFactory;
    private final ApplicationModelFactory applicationModelFactory;

    public ZestRuntimeImpl()
    {
        applicationAssemblyFactory = new ApplicationAssemblyFactoryImpl();
        applicationModelFactory = new ApplicationModelFactoryImpl();
    }

    @Override
    public ApplicationAssemblyFactory applicationAssemblyFactory()
    {
        return applicationAssemblyFactory;
    }

    @Override
    public ApplicationModelFactory applicationModelFactory()
    {
        return applicationModelFactory;
    }

    @Override
    public ZestAPI api()
    {
        return this;
    }

    @Override
    public ZestSPI spi()
    {
        return this;
    }

    // API

    @Override
    @SuppressWarnings( "unchecked" )
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

    @Override
    public ModuleDescriptor moduleOf( Object compositeOrServiceReferenceOrUow )
    {
        if( compositeOrServiceReferenceOrUow instanceof TransientComposite )
        {
            TransientComposite composite = (TransientComposite) compositeOrServiceReferenceOrUow;
            return TransientInstance.compositeInstanceOf( composite ).module();
        }
        else if( compositeOrServiceReferenceOrUow instanceof EntityComposite )
        {
            EntityComposite composite = (EntityComposite) compositeOrServiceReferenceOrUow;
            return EntityInstance.entityInstanceOf( composite ).module();
        }
        else if( compositeOrServiceReferenceOrUow instanceof ValueComposite )
        {
            ValueComposite composite = (ValueComposite) compositeOrServiceReferenceOrUow;
            return ValueInstance.valueInstanceOf( composite ).module();
        }
        else if( compositeOrServiceReferenceOrUow instanceof ServiceComposite )
        {
            ServiceComposite composite = (ServiceComposite) compositeOrServiceReferenceOrUow;
            InvocationHandler handler = getInvocationHandler( composite );
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
            ServiceReferenceInstance<?> reference = (ServiceReferenceInstance<?>) compositeOrServiceReferenceOrUow;
            return reference.module();
        }
        else if( compositeOrServiceReferenceOrUow instanceof ImportedServiceReferenceInstance )
        {
            ImportedServiceReferenceInstance<?> importedServiceReference
                = (ImportedServiceReferenceInstance<?>) compositeOrServiceReferenceOrUow;
            return importedServiceReference.module();
        }
        throw new IllegalArgumentException( "Wrong type. Must be one of "
                                            + Arrays.asList( TransientComposite.class, ValueComposite.class,
                                                             ServiceComposite.class, ServiceReference.class,
                                                             UnitOfWork.class ) );
    }

    @Override
    public ModelDescriptor modelDescriptorFor( Object compositeOrServiceReference )
    {
        if( compositeOrServiceReference instanceof TransientComposite )
        {
            TransientComposite composite = (TransientComposite) compositeOrServiceReference;
            return TransientInstance.compositeInstanceOf( composite ).descriptor();
        }
        else if( compositeOrServiceReference instanceof EntityComposite )
        {
            EntityComposite composite = (EntityComposite) compositeOrServiceReference;
            return EntityInstance.entityInstanceOf( composite ).descriptor();
        }
        else if( compositeOrServiceReference instanceof ValueComposite )
        {
            ValueComposite composite = (ValueComposite) compositeOrServiceReference;
            return ValueInstance.valueInstanceOf( composite ).descriptor();
        }
        else if( compositeOrServiceReference instanceof ServiceComposite )
        {
            ServiceComposite composite = (ServiceComposite) compositeOrServiceReference;
            InvocationHandler handler = getInvocationHandler( composite );
            if( handler instanceof ServiceInstance )
            {
                return ( (ServiceInstance) handler ).descriptor();
            }
            return ( (ServiceReferenceInstance.ServiceInvocationHandler) handler ).descriptor();
        }
        else if( compositeOrServiceReference instanceof ServiceReferenceInstance )
        {
            ServiceReferenceInstance<?> reference = (ServiceReferenceInstance<?>) compositeOrServiceReference;
            return reference.serviceDescriptor();
        }
        else if( compositeOrServiceReference instanceof ImportedServiceReferenceInstance )
        {
            ImportedServiceReferenceInstance<?> importedServiceReference
                = (ImportedServiceReferenceInstance<?>) compositeOrServiceReference;
            return importedServiceReference.serviceDescriptor();
        }
        throw new IllegalArgumentException( "Wrong type. Must be one of "
                                            + Arrays.asList( TransientComposite.class, ValueComposite.class,
                                                             ServiceComposite.class, ServiceReference.class ) );
    }

    @Override
    public CompositeDescriptor compositeDescriptorFor( Object compositeOrServiceReference )
    {
        return (CompositeDescriptor) modelDescriptorFor( compositeOrServiceReference );
    }

    // Descriptors

    @Override
    public TransientDescriptor transientDescriptorFor( Object transsient )
    {
        if( transsient instanceof TransientComposite )
        {
            TransientInstance transientInstance = compositeInstanceOf( (Composite) transsient );
            return (TransientDescriptor) transientInstance.descriptor();
        }
        throw new IllegalArgumentException( "Wrong type. Must be subtype of " + TransientComposite.class );
    }

    @Override
    public StateHolder stateOf( TransientComposite composite )
    {
        return TransientInstance.compositeInstanceOf( composite ).state();
    }

    @Override
    public EntityDescriptor entityDescriptorFor( Object entity )
    {
        if( entity instanceof EntityComposite )
        {
            EntityInstance entityInstance = (EntityInstance) getInvocationHandler( entity );
            return entityInstance.entityModel();
        }
        throw new IllegalArgumentException( "Wrong type. Must be subtype of " + EntityComposite.class );
    }

    @Override
    public AssociationStateHolder stateOf( EntityComposite composite )
    {
        return EntityInstance.entityInstanceOf( composite ).state();
    }

    @Override
    public ValueDescriptor valueDescriptorFor( Object value )
    {
        if( value instanceof ValueComposite )
        {
            ValueInstance valueInstance = ValueInstance.valueInstanceOf( (ValueComposite) value );
            return valueInstance.descriptor();
        }
        throw new IllegalArgumentException( "Wrong type. Must be subtype of " + ValueComposite.class );
    }

    @Override
    public AssociationStateHolder stateOf( ValueComposite composite )
    {
        return ValueInstance.valueInstanceOf( composite ).state();
    }

    @Override
    public ServiceDescriptor serviceDescriptorFor( Object service )
    {
        if( service instanceof ServiceReferenceInstance )
        {
            ServiceReferenceInstance<?> ref = (ServiceReferenceInstance<?>) service;
            return ref.serviceDescriptor();
        }
        if( service instanceof ServiceComposite )
        {
            ServiceComposite composite = (ServiceComposite) service;
            return (ServiceDescriptor) ServiceInstance.serviceInstanceOf( composite ).descriptor();
        }
        throw new IllegalArgumentException( "Wrong type. Must be subtype of "
                                            + ServiceComposite.class + " or " + ServiceReference.class );
    }

    @Override
    public PropertyDescriptor propertyDescriptorFor( Property<?> property )
    {
        while( property instanceof PropertyWrapper )
        {
            property = ( (PropertyWrapper) property ).next();
        }

        return (PropertyDescriptor) ( (PropertyInstance<?>) property ).propertyInfo();
    }

    @Override
    public AssociationDescriptor associationDescriptorFor( AbstractAssociation association )
    {
        while( association instanceof AssociationWrapper )
        {
            association = ( (AssociationWrapper) association ).next();
        }

        while( association instanceof ManyAssociationWrapper )
        {
            association = ( (ManyAssociationWrapper) association ).next();
        }

        while( association instanceof NamedAssociationWrapper )
        {
            association = ( (NamedAssociationWrapper) association ).next();
        }

        return (AssociationDescriptor) ( (AbstractAssociationInstance) association ).associationInfo();
    }

    // SPI
    @Override
    public EntityState entityStateOf( EntityComposite composite )
    {
        return EntityInstance.entityInstanceOf( composite ).entityState();
    }

    @Override
    public EntityReference entityReferenceOf( Association<?> assoc )
    {
        return assoc.reference();
    }

    @Override
    public Stream<EntityReference> entityReferencesOf( ManyAssociation<?> assoc )
    {
        return assoc.references();
    }

    @Override
    public Stream<Map.Entry<String, EntityReference>> entityReferencesOf( NamedAssociation<?> assoc )
    {
        return assoc.references();
    }
}
