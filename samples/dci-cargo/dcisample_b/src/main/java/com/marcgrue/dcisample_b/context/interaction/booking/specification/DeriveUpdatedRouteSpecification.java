package com.marcgrue.dcisample_b.context.interaction.booking.specification;

import com.marcgrue.dcisample_b.context.interaction.handling.inspection.exception.UnexpectedCarrierException;
import com.marcgrue.dcisample_b.data.factory.RouteSpecificationFactoryService;
import com.marcgrue.dcisample_b.data.factory.exception.CannotCreateRouteSpecificationException;
import com.marcgrue.dcisample_b.data.structure.cargo.Cargo;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.voyage.CarrierMovement;
import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import com.marcgrue.dcisample_b.infrastructure.dci.Context;
import com.marcgrue.dcisample_b.infrastructure.dci.RoleMixin;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus.*;

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

    public RouteSpecification getRouteSpec() throws CannotCreateRouteSpecificationException, UnexpectedCarrierException
    {
        // Pre-conditions
        if (transportStatus.equals( CLAIMED ))
            throw new CannotCreateRouteSpecificationException( "Can't derive new route specification for a claimed cargo." );

        return cargoInspector.getUpdatedRouteSpecification();
    }


    @Mixins( CargoInspectorRole.Mixin.class )
    public interface CargoInspectorRole
    {
        void setContext( DeriveUpdatedRouteSpecification context );

        RouteSpecification getUpdatedRouteSpecification() throws CannotCreateRouteSpecificationException, UnexpectedCarrierException;

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

                if (c.transportStatus.equals( NOT_RECEIVED ))
                {
                    newOrigin = cargo.origin().get();
                    newEarliestDeparture = c.routeSpecification.earliestDeparture().get();
                }
                else if (c.transportStatus.equals( ONBOARD_CARRIER ))
                {
                    Voyage voyage = c.lastHandlingEvent.voyage().get();
                    Location departureLocation = c.lastHandlingEvent.location().get();
                    CarrierMovement carrierMovement = voyage.carrierMovementDepartingFrom( departureLocation );
                    if (carrierMovement == null)
                        throw new UnexpectedCarrierException( c.lastHandlingEvent);

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