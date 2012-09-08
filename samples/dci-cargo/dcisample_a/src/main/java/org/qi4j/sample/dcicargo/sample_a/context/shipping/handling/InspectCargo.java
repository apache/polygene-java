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
package org.qi4j.sample.dcicargo.sample_a.context.shipping.handling;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.sample.dcicargo.sample_a.context.shipping.booking.BuildDeliverySnapshot;
import org.qi4j.sample.dcicargo.sample_a.context.support.ApplicationEvents;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.dci.Context;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.dci.RoleMixin;

/**
 * The Inspect Cargo use case updates the delivery history of the cargo and
 * determines if the registered handling event was expected.
 */
public class InspectCargo extends Context
{
    // ROLES ---------------------------------------------------------------------

    private CargoInspectorRole cargoInspector;

    private Cargo cargo;
    private Delivery delivery;

    // CONTEXT CONSTRUCTORS ------------------------------------------------------

    public InspectCargo( HandlingEvent registeredHandlingEvent )
    {
        cargoInspector = rolePlayer( CargoInspectorRole.class, registeredHandlingEvent );
        cargo = loadEntity( Cargo.class, registeredHandlingEvent.trackingId().get().id().get() );
        delivery = cargo.delivery().get();
    }

    // INTERACTIONS --------------------------------------------------------------

    public void inspect()
    {
        cargoInspector.inspect();
    }

    // METHODFUL ROLE IMPLEMENTATIONS --------------------------------------------

    /**
     * Cargo handling role.
     */
    @Mixins( CargoInspectorRole.Mixin.class )
    public interface CargoInspectorRole
    {
        void setContext( InspectCargo context );

        void inspect();

        class Mixin
            extends RoleMixin<InspectCargo>
            implements CargoInspectorRole
        {
            @This
            HandlingEvent registeredHandlingEvent;

            @Service
            ApplicationEvents applicationEvents;

            public void inspect()
            {
                // Step 1: Update delivery history of cargo.
                context.delivery = new BuildDeliverySnapshot( context.cargo, registeredHandlingEvent ).get();

                // Step 2: Replace updated delivery snapshot of cargo
                context.cargo.delivery().set( context.delivery );

                // Deviation 3a: Publish that cargo was misdirected
                if( context.delivery.isMisdirected().get() )
                {
                    applicationEvents.cargoWasMisdirected( context.cargo );
                }

                // Deviation 3b: Publish that cargo has arrived
                if( context.delivery.isUnloadedAtDestination().get() )
                {
                    applicationEvents.cargoHasArrived( context.cargo );
                }

                // Step 4: Updated cargo is saved when UnitOfWork completes.
            }
        }
    }
}