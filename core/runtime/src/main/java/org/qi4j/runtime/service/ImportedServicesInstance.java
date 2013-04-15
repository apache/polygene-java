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

import java.util.List;
import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationEventListenerRegistration;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.runtime.activation.ActivationDelegate;
import org.qi4j.runtime.activation.ActivationEventListenerSupport;
import org.qi4j.runtime.activation.ActivatorsInstance;

import static org.qi4j.api.util.Classes.instanceOf;
import static org.qi4j.functional.Iterables.filter;

/**
 * JAVADOC
 */
public class ImportedServicesInstance
    implements Activation, ActivationEventListenerRegistration
{
    private final ImportedServicesModel servicesModel;
    private final List<ServiceReference> serviceReferences;
    private final ActivationDelegate activation = new ActivationDelegate( this );
    private final ActivationEventListenerSupport activationEventSupport = new ActivationEventListenerSupport();

    public ImportedServicesInstance( ImportedServicesModel servicesModel,
                                     List<ServiceReference> serviceReferences
    )
    {
        this.servicesModel = servicesModel;
        this.serviceReferences = serviceReferences;
        for( ServiceReference serviceReference : serviceReferences )
        {
            serviceReference.registerActivationEventListener( activationEventSupport );
        }
    }

    @Override
    public void activate()
        throws ActivationException
    {
        Iterable<Activation> activatees = Iterables.<Activation>cast( filter( instanceOf( Activation.class ), serviceReferences ) );
        activation.activate( ActivatorsInstance.EMPTY, activatees );
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        activation.passivate();
    }

    public Iterable<ServiceReference> visibleServices( final Visibility visibility )
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
        StringBuilder sb = new StringBuilder( "Services{" );
        String sep = " ";
        for( ServiceReference serviceReference : serviceReferences )
        {
            sb.append( sep ).
                append( serviceReference.identity() ).
                append( "(active=" ).append( serviceReference.isActive() ).append( ")" );
            sep = ", ";
        }
        return sb.append( " }" ).toString();
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activationEventSupport.deregisterActivationEventListener( listener );
    }
}
