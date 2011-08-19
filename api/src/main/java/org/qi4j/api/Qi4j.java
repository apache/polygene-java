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

package org.qi4j.api;

import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.composite.*;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.association.AbstractAssociation;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Encapsulation of the Qi4j API.
 */
public interface Qi4j
{
    /**
     * If a Modifier gets a reference to the Composite using @This,
     * then that reference must be dereferenced using this method
     * before handing it out for others to use.
     *
     * @param composite instance reference injected in Modified using @This
     *
     * @return the dereferenced Composite
     */
    <T> T dereference( T composite );

    /**
     * Finds the Configuration instance of a service.
     * <p>This is used by ConfigurationMixin to figure out the configuration instance used by
     * a Service using {@link org.qi4j.api.configuration.Configuration}, and should not be
     * used directly by client code.</p>
     *
     * <p>If the Configuration entity doesn't exist in the visible EntityStore, then a properties
     * file with the name of the service identifier will be located on the classpath, and the
     * values used to create the Configuration instance, which will then be saved to the EntityStore
     * for future use. That means that the properties file is <b>only</b> used
     *
     * @param serviceComposite the service instance
     * @param uow              the UnitOfWork from which the configuration will be loaded
     *
     * @return configuration instance
     *
     * @throws InstantiationException thrown if the configuration cannot be instantiated
     */
    <T> T getConfigurationInstance( ServiceComposite serviceComposite, UnitOfWork uow )
        throws InstantiationException;

    /**
     * Returns the Module where the UnitOfWork belongs.
     *
     * @param uow The UnitOfWork to be checked.
     *
     * @return The Module instance where the UnitOfWork belongs.
     */
    Module getModule( UnitOfWork uow );

    /**
     * Returns the Module where the Composite belongs.
     *
     * @param composite The Composite to be checked.
     *
     * @return The Module instance where the Composite belongs.
     */
    Module getModule( Composite composite );

    /**
     * Returns the Module where the service is located.
     *
     * @param service The service to be checked.
     *
     * @return The Module instance where the Composite belongs.
     */
    Module getModule( ServiceReference service );

    TransientDescriptor getTransientDescriptor( TransientComposite composite );

    EntityDescriptor getEntityDescriptor( EntityComposite composite );

    ValueDescriptor getValueDescriptor( ValueComposite value );

    // Services
    ServiceDescriptor getServiceDescriptor( ServiceReference service );

    ServiceDescriptor getServiceDescriptor( ServiceComposite service );

    // State
    PropertyDescriptor getPropertyDescriptor( Property property );

    AssociationDescriptor getAssociationDescriptor( AbstractAssociation association);

    StateHolder getState( TransientComposite composite );

    AssociationStateHolder getState( EntityComposite composite );

    AssociationStateHolder getState( ValueComposite composite );

    public static Function<Composite, CompositeDescriptor> DESCRIPTOR_FUNCTION = new Function<Composite, CompositeDescriptor>()
    {
        @Override
        public CompositeDescriptor map( Composite composite )
        {
            if (composite instanceof Proxy)
            {
                InvocationHandler invocationHandler = Proxy.getInvocationHandler( composite );
                return ((CompositeInstance) invocationHandler).descriptor();
            } else
            {
                try
                {
                    CompositeInstance instance = (CompositeInstance) composite.getClass().getField( "_instance" ).get( composite );
                    return instance.descriptor();
                } catch( Exception e )
                {
                    throw (InvalidCompositeException) new InvalidCompositeException( "Could not get _instance field" ).initCause( e );
                }
            }
        }
    };

    public static Function<Composite, CompositeInstance> INSTANCE_FUNCTION = new Function<Composite, CompositeInstance>()
    {
        @Override
        public CompositeInstance map( Composite composite )
        {
            if (composite instanceof Proxy)
            {
                return ((CompositeInstance) Proxy.getInvocationHandler( composite ));
            } else
            {
                try
                {
                    CompositeInstance instance = (CompositeInstance) composite.getClass().getField( "_instance" ).get( composite );
                    return instance;
                } catch( Exception e )
                {
                    throw (InvalidCompositeException) new InvalidCompositeException( "Could not get _instance field" ).initCause( e );
                }
            }
        }
    };
}
