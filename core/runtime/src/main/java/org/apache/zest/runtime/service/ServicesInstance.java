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
package org.apache.zest.runtime.service;

import java.util.List;
import java.util.stream.Stream;
import org.apache.zest.api.activation.Activation;
import org.apache.zest.api.activation.ActivationEventListener;
import org.apache.zest.api.activation.ActivationEventListenerRegistration;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.activation.PassivationException;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.functional.Iterables;
import org.apache.zest.runtime.activation.ActivationDelegate;
import org.apache.zest.runtime.activation.ActivatorsInstance;

import static org.apache.zest.api.util.Classes.instanceOf;
import static org.apache.zest.functional.Iterables.filter;

/**
 * JAVADOC
 */
public class ServicesInstance
    implements Activation, ActivationEventListenerRegistration
{
    private final ServicesModel servicesModel;
    private final List<ServiceReference<?>> serviceReferences;
    private final ActivationDelegate activation = new ActivationDelegate( this, false );

    public ServicesInstance( ServicesModel servicesModel, List<ServiceReference<?>> serviceReferences )
    {
        this.servicesModel = servicesModel;
        this.serviceReferences = serviceReferences;
        for( ServiceReference serviceReference : serviceReferences )
        {
            serviceReference.registerActivationEventListener( activation );
        }
    }

    public Stream<? extends ServiceDescriptor> models()
    {
        return servicesModel.models();
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

    public Stream<ServiceReference<?>> visibleServices( final Visibility visibility )
    {
        return serviceReferences.stream()
            .filter( item -> ( (ServiceReferenceInstance) item ).serviceDescriptor()
                                 .visibility().ordinal() >= visibility.ordinal() );
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
        activation.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activation.deregisterActivationEventListener( listener );
    }

    public Stream<? extends ServiceDescriptor> descriptors()
    {
        return servicesModel.models();
    }

    public Stream<ServiceReference<?>> references()
    {
        return serviceReferences.stream();
    }
}
