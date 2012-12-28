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
package org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.specification;

import java.util.Date;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception.UnexpectedCarrierException;
import org.qi4j.sample.dcicargo.sample_b.data.factory.RouteSpecificationFactoryService;
import org.qi4j.sample.dcicargo.sample_b.data.factory.exception.CannotCreateRouteSpecificationException;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.CarrierMovement;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Voyage;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.dci.Context;
import org.qi4j.sample.dcicargo.sample_b.infrastructure.dci.RoleMixin;

import static org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus.*;

/**
 * Derive Updated Route Specification (subfunction use case)
 *
 * A Cargo Owner requests to change the route for a Cargo.
 *
 * If the request happens during transport we have to consider the current location of the cargo.
 * If currently on board a carrier we use the arrival port as the new origin for a new route specification
 * otherwise simply the lastKnownLocation. Original origin location of Cargo remains the same.
 */
public class DeriveUpdatedRouteSpecification extends Context
{
    CargoInspectorRole cargoInspector;

    RouteSpecification routeSpecification;
    TransportStatus transportStatus;
    HandlingEvent lastHandlingEvent;
    Location newDestination;

    public DeriveUpdatedRouteSpecification( Cargo cargo )
    {
        this.cargoInspector = rolePlayer( CargoInspectorRole.class, cargo );

        routeSpecification = cargo.routeSpecification().get();
        transportStatus = cargo.delivery().get().transportStatus().get();
        lastHandlingEvent = cargo.delivery().get().lastHandlingEvent().get();
    }

    public DeriveUpdatedRouteSpecification( String trackingIdString )
    {
        this( loadEntity( Cargo.class, trackingIdString ) );
    }

    public DeriveUpdatedRouteSpecification( Cargo cargo, Location newDestination )
    {
        this( cargo );
        this.newDestination = newDestination;
    }

    public RouteSpecification getRouteSpec()
        throws CannotCreateRouteSpecificationException, UnexpectedCarrierException
    {
        // Pre-conditions
        if( transportStatus.equals( CLAIMED ) )
        {
            throw new CannotCreateRouteSpecificationException( "Can't derive new route specification for a claimed cargo." );
        }

        return cargoInspector.getUpdatedRouteSpecification();
    }

    @Mixins( CargoInspectorRole.Mixin.class )
    public interface CargoInspectorRole
    {
        void setContext( DeriveUpdatedRouteSpecification context );

        RouteSpecification getUpdatedRouteSpecification()
            throws CannotCreateRouteSpecificationException, UnexpectedCarrierException;

        class Mixin
            extends RoleMixin<DeriveUpdatedRouteSpecification>
            implements CargoInspectorRole
        {
            @This
            Cargo cargo;

            @Service
            RouteSpecificationFactoryService routeSpecFactory;

            Location newOrigin;
            Location newDestination;
            Date newEarliestDeparture;
            Date newArrivalDeadline;

            public RouteSpecification getUpdatedRouteSpecification()
                throws CannotCreateRouteSpecificationException, UnexpectedCarrierException
            {
                // Step 1 - Collect destination and deadline

                newDestination = c.newDestination == null ? c.routeSpecification.destination().get() : c.newDestination;
                newArrivalDeadline = c.routeSpecification.arrivalDeadline().get();

                // Step 2 - Derive origin and earliest departure date

                if( c.transportStatus.equals( NOT_RECEIVED ) )
                {
                    newOrigin = cargo.origin().get();
                    newEarliestDeparture = c.routeSpecification.earliestDeparture().get();
                }
                else if( c.transportStatus.equals( ONBOARD_CARRIER ) )
                {
                    Voyage voyage = c.lastHandlingEvent.voyage().get();
                    Location departureLocation = c.lastHandlingEvent.location().get();
                    CarrierMovement carrierMovement = voyage.carrierMovementDepartingFrom( departureLocation );
                    if( carrierMovement == null )
                    {
                        throw new UnexpectedCarrierException( c.lastHandlingEvent );
                    }

                    newOrigin = carrierMovement.arrivalLocation().get();
                    newEarliestDeparture = carrierMovement.arrivalTime().get();
                }
                else
                {
                    newOrigin = c.lastHandlingEvent.location().get();
                    newEarliestDeparture = c.lastHandlingEvent.completionTime().get();
                }

                // Step 3 - Build and return new route specification

                return routeSpecFactory.build( newOrigin, newDestination, newEarliestDeparture, newArrivalDeadline );
            }
        }
    }
}