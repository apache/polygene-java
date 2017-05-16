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
package org.apache.polygene.runtime;

import java.lang.reflect.InvocationHandler;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.association.AbstractAssociation;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.association.AssociationStateHolder;
import org.apache.polygene.api.association.AssociationWrapper;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.ManyAssociationWrapper;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.association.NamedAssociationWrapper;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.composite.CompositeDescriptor;
import org.apache.polygene.api.composite.CompositeInstance;
import org.apache.polygene.api.composite.ModelDescriptor;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.composite.TransientDescriptor;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.property.PropertyWrapper;
import org.apache.polygene.api.property.StateHolder;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.bootstrap.ApplicationAssemblyFactory;
import org.apache.polygene.bootstrap.ApplicationModelFactory;
import org.apache.polygene.bootstrap.PolygeneRuntime;
import org.apache.polygene.runtime.association.AbstractAssociationInstance;
import org.apache.polygene.runtime.bootstrap.ApplicationAssemblyFactoryImpl;
import org.apache.polygene.runtime.bootstrap.ApplicationModelFactoryImpl;
import org.apache.polygene.runtime.composite.ProxyReferenceInvocationHandler;
import org.apache.polygene.runtime.composite.TransientInstance;
import org.apache.polygene.runtime.entity.EntityInstance;
import org.apache.polygene.runtime.property.PropertyInstance;
import org.apache.polygene.runtime.service.ImportedServiceReferenceInstance;
import org.apache.polygene.runtime.service.ServiceReferenceInstance;
import org.apache.polygene.runtime.unitofwork.ModuleUnitOfWork;
import org.apache.polygene.runtime.value.ValueInstance;
import org.apache.polygene.spi.PolygeneSPI;
import org.apache.polygene.spi.entity.EntityState;

import static java.lang.reflect.Proxy.getInvocationHandler;
import static org.apache.polygene.api.composite.CompositeInstance.compositeInstanceOf;

/**
 * Incarnation of Polygene.
 */
public final class PolygeneRuntimeImpl
    implements PolygeneSPI, PolygeneRuntime
{
    private final ApplicationAssemblyFactory applicationAssemblyFactory;
    private final ApplicationModelFactory applicationModelFactory;

    public PolygeneRuntimeImpl()
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
    public PolygeneAPI api()
    {
        return this;
    }

    @Override
    public PolygeneSPI spi()
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
        if( compositeOrServiceReferenceOrUow instanceof Composite )
        {
            return compositeInstanceOf( (Composite) compositeOrServiceReferenceOrUow ).module();
        }
//        else if( compositeOrServiceReferenceOrUow instanceof ServiceComposite )
//        {
//            ServiceComposite composite = (ServiceComposite) compositeOrServiceReferenceOrUow;
//            InvocationHandler handler = getInvocationHandler( composite );
//            if( handler instanceof ServiceInstance )
//            {
//                return ( (ServiceInstance) handler ).module();
//            }
//            return ( (ServiceReferenceInstance.ServiceInvocationHandler) handler ).module();
//        }
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
                                                             EntityComposite.class,
                                                             ServiceComposite.class, ServiceReference.class,
                                                             UnitOfWork.class ) );
    }

    @Override
    public ModelDescriptor modelDescriptorFor( Object compositeOrServiceReference )
    {
        if( compositeOrServiceReference instanceof Composite )
        {
            return compositeInstanceOf( (Composite) compositeOrServiceReference ).descriptor();
        }
//        else if( compositeOrServiceReference instanceof ServiceComposite )
//        {
//            ServiceComposite composite = (ServiceComposite) compositeOrServiceReference;
//            InvocationHandler handler = getInvocationHandler( composite );
//            if( handler instanceof ServiceInstance )
//            {
//                return ( (ServiceInstance) handler ).descriptor();
//            }
//            return ( (ServiceReferenceInstance.ServiceInvocationHandler) handler ).descriptor();
//        }
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
                                            + Arrays.asList( TransientComposite.class, ValueComposite.class, EntityComposite.class,
                                                             ServiceComposite.class, ServiceReference.class,
                                                             ImportedServiceReferenceInstance.class ) );
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
            TransientInstance transientInstance = (TransientInstance) compositeInstanceOf( (Composite) transsient );
            return (TransientDescriptor) transientInstance.descriptor();
        }
        throw new IllegalArgumentException( "Wrong type. Must be subtype of " + TransientComposite.class );
    }

    @Override
    public StateHolder stateOf( TransientComposite composite )
    {
        return compositeInstanceOf( composite ).state();
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
        return ( (EntityInstance) compositeInstanceOf( composite ) ).state();
    }

    @Override
    public ValueDescriptor valueDescriptorFor( Object value )
    {
        if( value instanceof ValueComposite )
        {
            ValueInstance valueInstance = (ValueInstance) compositeInstanceOf( (Composite) value );
            return valueInstance.descriptor();
        }
        throw new IllegalArgumentException( "Wrong type. {" + value + "} must be subtype of " + ValueComposite.class );
    }

    @Override
    public AssociationStateHolder stateOf( ValueComposite composite )
    {
        return ( (ValueInstance) compositeInstanceOf( composite ) ).state();
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
            return (ServiceDescriptor) compositeInstanceOf( composite ).descriptor();
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
        return ( (EntityInstance) compositeInstanceOf( composite ) ).entityState();
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
