/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectArrivedCargo;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectCargoInCustoms;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectClaimedCargo;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectLoadedCargo;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectReceivedCargo;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectUnhandledCargo;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.event.InspectUnloadedCargo;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionException;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.dci.Context;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.dci.RoleMixin;

/**
 * Inspect Cargo Delivery Status (subfunction use case)
 *
 * Third and last step in the ProcessHandlingEvent use case.
 *
 * Updates our knowledge about a cargo delivery. All we know about the delivery is saved in
 * a Delivery value object that is replaced each time it's updated.
 *
 * Cargo is playing the Role of a Delivery Inspector that inspects a specific handling event
 * in order to update the Delivery status for the cargo.
 *
 * If the Delivery Inspector realizes that the cargo is not on track, proper notifications are
 * sent out so that the cargo owner can re-route the cargo or the system take some action.
 *
 * Handling cargo in customs is a completely different process and context than loading it onto
 * a ship, so each handling event has its own use case and thus its own Context. Each of those
 * (sub-)subfunction use cases/Contexts reflects our understanding of a real world process and
 * can be easily reviewed, maintained, specialized, tested etc. It's now easy to understand
 * what our program does and what we can expect from it - basically in one place in the code.
 */
public class InspectCargoDeliveryStatus extends Context
{
    DeliveryInspectorRole deliveryInspector;

    Location destination;
    HandlingEvent handlingEvent;
    Location handlingLocation;

    public InspectCargoDeliveryStatus( HandlingEvent handlingEvent )
    {
        this.handlingEvent = handlingEvent;

        Cargo cargo = loadEntity( Cargo.class, handlingEvent.trackingId().get().id().get() );
        deliveryInspector = rolePlayer( DeliveryInspectorRole.class, cargo );

        handlingLocation = handlingEvent.location().get();
        destination = cargo.routeSpecification().get().destination().get();
    }

    public InspectCargoDeliveryStatus( Cargo cargo )
    {
        deliveryInspector = rolePlayer( DeliveryInspectorRole.class, cargo );
        destination = cargo.routeSpecification().get().destination().get();

        handlingEvent = cargo.delivery().get().lastHandlingEvent().get();
        if( handlingEvent != null )
        {
            handlingLocation = handlingEvent.location().get();
        }
    }

    public void update()
        throws InspectionException
    {
        deliveryInspector.delegateInspection();
    }

    @Mixins( DeliveryInspectorRole.Mixin.class )
    public interface DeliveryInspectorRole
    {
        void setContext( InspectCargoDeliveryStatus context );

        void delegateInspection()
            throws InspectionException;

        class Mixin
            extends RoleMixin<InspectCargoDeliveryStatus>
            implements DeliveryInspectorRole
        {
            @This
            Cargo cargo;

            public void delegateInspection()
                throws InspectionException
            {

                // Step 1 - Determine handling event type

                if( c.handlingEvent == null )
                {
                    new InspectUnhandledCargo( cargo ).inspect();
                    return;
                }
                HandlingEventType handlingEventType = c.handlingEvent.handlingEventType().get();

                // Step 2 - Delegate inspection

                switch( handlingEventType )
                {
                case RECEIVE:
                    new InspectReceivedCargo( cargo, c.handlingEvent ).inspect();
                    break;

                case LOAD:
                    new InspectLoadedCargo( cargo, c.handlingEvent ).inspect();
                    break;

                case UNLOAD:
                    if( c.handlingLocation.equals( c.destination ) )
                    {
                        new InspectArrivedCargo( cargo, c.handlingEvent ).inspect();
                    }
                    else
                    {
                        new InspectUnloadedCargo( cargo, c.handlingEvent ).inspect();
                    }
                    break;

                case CUSTOMS:
                    new InspectCargoInCustoms( cargo, c.handlingEvent ).inspect();
                    break;

                case CLAIM:
                    new InspectClaimedCargo( cargo, c.handlingEvent ).inspect();
                    break;

                default:
                    // No other handling event types
                }
            }
        }
    }
}