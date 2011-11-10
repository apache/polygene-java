/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.service;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.event.ActivationEventListenerRegistration;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.runtime.structure.ActivationEventListenerSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JAVADOC
 */
public class ServicesInstance
        implements Activatable, ActivationEventListenerRegistration
{
    private final ServicesModel servicesModel;
    private final List<ServiceReference> serviceReferences;
    private final Activator activator;
    private final Map<String, ServiceReference> mapIdentityServiceReference = new HashMap<String, ServiceReference>();
    private final ActivationEventListenerSupport eventListenerSupport = new ActivationEventListenerSupport();

    public ServicesInstance( ServicesModel servicesModel, List<ServiceReference> serviceReferences )
    {
        this.servicesModel = servicesModel;
        this.serviceReferences = serviceReferences;

        for( ServiceReference serviceReference : serviceReferences )
        {
            mapIdentityServiceReference.put( serviceReference.identity(), serviceReference );
            serviceReference.registerActivationEventListener( eventListenerSupport );
        }
        activator = new Activator();
    }

    public void activate()
            throws Exception
    {
        for( final ServiceReference serviceReference : serviceReferences )
        {
            if( serviceReference instanceof Activatable )
            {
                Activatable eventActivatable = new Activatable()
                {
                    @Override
                    public void activate() throws Exception
                    {
                        ((Activatable) serviceReference).activate();
                    }

                    @Override
                    public void passivate() throws Exception
                    {
                        ((Activatable) serviceReference).passivate();
                    }
                };

                activator.activate( eventActivatable );
            }
        }
    }

    public void passivate()
            throws Exception
    {
        activator.passivate();
    }

    public <T> ServiceReference<T> getServiceWithIdentity( String serviceIdentity )
    {
        return mapIdentityServiceReference.get( serviceIdentity );
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        eventListenerSupport.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        eventListenerSupport.deregisterActivationEventListener( listener );
    }

    public Iterable<ServiceReference> visibleServices( final Visibility visibility )
    {
        return Iterables.filter( new Specification<ServiceReference>()
                {
                    @Override
                    public boolean satisfiedBy( ServiceReference item )
                    {
                        return ((ServiceReferenceInstance) item).serviceDescriptor().visibility().ordinal() >= visibility.ordinal();
                    }
                }, serviceReferences );
    }

    @Override
    public String toString()
    {
        String str = "{";
        String sep = "";
        for( ServiceReference serviceReference : serviceReferences )
        {
            str += sep + serviceReference.identity() + ",active=" + serviceReference.isActive();
            sep = ", ";
        }
        return str += "}";
    }
}
