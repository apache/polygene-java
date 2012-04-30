package com.marcgrue.dcisample_b.context.interaction.booking;

import com.marcgrue.dcisample_b.data.aggregateroot.CargoAggregateRoot;
import com.marcgrue.dcisample_b.data.factory.CargoFactory;
import com.marcgrue.dcisample_b.data.factory.RouteSpecificationFactoryService;
import com.marcgrue.dcisample_b.data.structure.cargo.Cargo;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.delivery.Delivery;
import com.marcgrue.dcisample_b.data.structure.delivery.NextHandlingEvent;
import com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus;
import com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.tracking.TrackingId;
import com.marcgrue.dcisample_b.infrastructure.dci.Context;
import com.marcgrue.dcisample_b.infrastructure.dci.RoleMixin;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;

import java.util.Date;

import static com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType.RECEIVE;

/**
 * Book New Cargo (use case)
 *
 * Cargo Owner starts with the 1st step in the overall Ship Cargo use case by booking a cargo shipment
 * in our shipping application.
 *
 * The Cargo Owner provides an origin and destination location and a arrival deadline to the booking
 * application. A route specification is created from the input and attached to a new cargo with
 * a unique tracking id.
 */
public class BookNewCargo extends Context
{
    private BookingSystemRole bookingSystem;

    private Location origin;
    private Location destination;
    private Date arrivalDeadline;


    public BookNewCargo( CargoFactory cargoFactory, Location origin, Location destination, Date arrivalDeadline ) throws Exception
    {
        bookingSystem = rolePlayer( BookingSystemRole.class, cargoFactory );
        this.origin = origin;
        this.destination = destination;
        this.arrivalDeadline = arrivalDeadline;
    }

    public BookNewCargo( String originId, String destinationId, Date deadline ) throws Exception
    {
        this( loadEntity( CargoAggregateRoot.class, CargoAggregateRoot.CARGOS_ID ),
              loadEntity( Location.class, originId ),
              loadEntity( Location.class, destinationId ),
              deadline );
    }


    public TrackingId getTrackingId() throws Exception
    {
        return bookingSystem.createCargo( null );
    }

    public TrackingId withTrackingId( String trackingIdString ) throws Exception
    {
        return bookingSystem.createCargo( trackingIdString );
    }


    @Mixins( BookingSystemRole.Mixin.class )
    public interface BookingSystemRole
    {
        void setContext( BookNewCargo context );

        TrackingId createCargo( @Optional String trackingIdString ) throws Exception;

        class Mixin
              extends RoleMixin<BookNewCargo>
              implements BookingSystemRole
        {
            @This
            CargoFactory cargoFactory;

            @Service
            RouteSpecificationFactoryService routeSpecFactory;

            public TrackingId createCargo( String trackingIdString ) throws Exception
            {
                Date earliestDeparture = new Date( );
                RouteSpecification routeSpec = routeSpecFactory.build( c.origin,
                                                                       c.destination,
                                                                       earliestDeparture,
                                                                       c.arrivalDeadline );

                ValueBuilder<Delivery> delivery = vbf.newValueBuilder( Delivery.class );
                delivery.prototype().timestamp().set( new Date() );
                delivery.prototype().transportStatus().set( TransportStatus.NOT_RECEIVED );
                delivery.prototype().routingStatus().set( RoutingStatus.NOT_ROUTED );

                // Expect receipt in origin (time unknown / no voyage yet)
                ValueBuilder<NextHandlingEvent> nextEvent = vbf.newValueBuilder( NextHandlingEvent.class );
                nextEvent.prototype().handlingEventType().set( RECEIVE );
                nextEvent.prototype().location().set( routeSpec.origin().get() );
                delivery.prototype().nextHandlingEvent().set( nextEvent.newInstance() );

                Cargo cargo = cargoFactory.createCargo( routeSpec, delivery.newInstance(), trackingIdString );

                return cargo.trackingId().get();
            }
        }
    }
}