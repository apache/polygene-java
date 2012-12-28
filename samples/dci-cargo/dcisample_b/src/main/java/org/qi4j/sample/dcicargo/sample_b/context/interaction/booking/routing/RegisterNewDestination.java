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
package org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.routing;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.exception.ChangeDestinationException;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.specification.DeriveUpdatedRouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.InspectCargoDeliveryStatus;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.InspectionException;
import org.qi4j.sample.dcicargo.sample_b.data.factory.exception.CannotCreateRouteSpecificationException;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.dci.Context;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.dci.RoleMixin;

import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.CLAIMED;

/**
 * Register New Destination (subfunction use case)
 *
 * Booking system has asked Cargo Inspector to register a new destination for a cargo.
 */
public class RegisterNewDestination extends Context
{
    CargoInspectorRole cargoInspector;

    TransportStatus transportStatus;
//    HandlingEvent lastHandlingEvent;

    public RegisterNewDestination( Cargo cargo )
    {
        this.cargoInspector = rolePlayer( CargoInspectorRole.class, cargo );

        transportStatus = cargo.delivery().get().transportStatus().get();
//        lastHandlingEvent = cargo.delivery().get().lastHandlingEvent().get();
    }

    public RegisterNewDestination( String trackingIdString )
    {
        this( loadEntity( Cargo.class, trackingIdString ) );
    }

    public void to( String destinationUnLocodeString )
        throws Exception
    {
        // Pre-conditions
        if( transportStatus.equals( CLAIMED ) )
        {
            throw new ChangeDestinationException( "Can't change destination of claimed cargo." );
        }

        cargoInspector.registerNewDestination( destinationUnLocodeString );
    }

    @Mixins( CargoInspectorRole.Mixin.class )
    public interface CargoInspectorRole
    {
        void setContext( RegisterNewDestination context );

        void registerNewDestination( String destination )
            throws Exception;

        class Mixin
            extends RoleMixin<RegisterNewDestination>
            implements CargoInspectorRole
        {
            @This
            Cargo cargo;

            Location newDestination;
            RouteSpecification newRouteSpec;

            public void registerNewDestination( String newDestinationString )
                throws ChangeDestinationException, CannotCreateRouteSpecificationException, InspectionException
            {
                // Step 1 - Verify new destination

                try
                {
                    newDestination = uowf.currentUnitOfWork().get( Location.class, newDestinationString );
                }
                catch( NoSuchEntityException e )
                {
                    throw new ChangeDestinationException( "Didn't recognize location '" + newDestinationString + "'" );
                }
                if( newDestination.equals( cargo.routeSpecification().get().destination().get() ) )
                {
                    throw new ChangeDestinationException( "New destination is same as old destination." );
                }

                // Step 2 - Derive new route specification

                newRouteSpec = new DeriveUpdatedRouteSpecification( cargo, newDestination ).getRouteSpec();

                // Step 3 - Assign new route specification to cargo

                cargo.routeSpecification().set( newRouteSpec );

                // Step 4 - Update cargo delivery status

                new InspectCargoDeliveryStatus( cargo ).update();
            }
        }
    }
}