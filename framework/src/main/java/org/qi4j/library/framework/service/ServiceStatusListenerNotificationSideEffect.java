/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.library.framework.service;

import org.qi4j.composite.SideEffectFor;
import org.qi4j.composite.ThisCompositeAs;
import org.qi4j.service.ActivationStatus;
import org.qi4j.service.MutableServiceStatus;
import org.qi4j.service.ServiceActivationEvent;
import org.qi4j.service.ServiceAvailabilityEvent;
import org.qi4j.service.ServiceComposite;
import org.qi4j.service.ServiceStatusNotification;

/**
 * Notify listeners when the service status changes.
 */
public abstract class ServiceStatusListenerNotificationSideEffect
    implements MutableServiceStatus
{
    @SideEffectFor MutableServiceStatus next;

    @ThisCompositeAs ServiceComposite composite;
    @ThisCompositeAs ServiceStatusNotification notification;

    public void setActivationStatus( ActivationStatus newStatus )
    {
        ServiceActivationEvent event = new ServiceActivationEvent( (ServiceComposite) composite.dereference(), newStatus );
        notification.notifyServiceStatusListeners( event );
    }

    public void setAvailable( boolean isAvailable )
    {
        ServiceAvailabilityEvent event = new ServiceAvailabilityEvent( (ServiceComposite) composite.dereference(), isAvailable );
        notification.notifyServiceStatusListeners( event );
    }
}
