/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.event.ActivationEventListenerRegistration;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.runtime.activation.ActivationEventListenerSupport;
import org.qi4j.runtime.activation.ActivationHandler;
import org.qi4j.runtime.activation.ActivatorsInstance;

/**
 * JAVADOC
 */
public class ImportedServicesInstance
    implements Activatable, ActivationEventListenerRegistration
{
    private final ImportedServicesModel servicesModel;
    private final List<? extends ServiceReference> serviceReferences;
    private final ActivationHandler activationHandler = new ActivationHandler( this );
    private final ActivationEventListenerSupport eventListenerSupport = new ActivationEventListenerSupport();

    public ImportedServicesInstance( ImportedServicesModel servicesModel,
                                     List<? extends ServiceReference> serviceReferences
    )
    {
        this.servicesModel = servicesModel;
        this.serviceReferences = serviceReferences;
        for( ServiceReference serviceReference : serviceReferences )
        {
            serviceReference.registerActivationEventListener( eventListenerSupport );
        }
    }

    public void activate()
            throws Exception
    {
        List<Activatable> activatableServiceReferences = new LinkedList<Activatable>();
        for( final ServiceReference serviceReference : serviceReferences )
        {
            if( serviceReference instanceof Activatable )
            {
                activatableServiceReferences.add( ( Activatable ) serviceReference );
            }
        }
        activationHandler.activate( ActivatorsInstance.EMPTY, activatableServiceReferences );
    }

    public void passivate()
            throws Exception
    {
        activationHandler.passivate();
    }

    public Iterable<? extends ServiceReference> visibleServices( final Visibility visibility )
    {
        return Iterables.filter( new Specification<ServiceReference>()
        {
            @Override
            public boolean satisfiedBy( ServiceReference item )
            {
                return ( (ImportedServiceReferenceInstance) item ).serviceDescriptor()
                           .visibility()
                           .ordinal() >= visibility.ordinal();
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

    public void registerActivationEventListener( ActivationEventListener listener )
    {
        eventListenerSupport.registerActivationEventListener( listener );
    }

    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        eventListenerSupport.deregisterActivationEventListener( listener );
    }
}
