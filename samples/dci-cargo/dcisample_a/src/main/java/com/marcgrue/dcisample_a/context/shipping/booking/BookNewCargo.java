package com.marcgrue.dcisample_a.context.shipping.booking;

import com.marcgrue.dcisample_a.context.support.FoundNoRoutesException;
import com.marcgrue.dcisample_a.context.support.RoutingService;
import com.marcgrue.dcisample_a.data.entity.CargoEntity;
import com.marcgrue.dcisample_a.data.entity.CargosEntity;
import com.marcgrue.dcisample_a.data.entity.LocationEntity;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.cargo.Cargo;
import com.marcgrue.dcisample_a.data.shipping.cargo.Cargos;
import com.marcgrue.dcisample_a.data.shipping.cargo.RouteSpecification;
import com.marcgrue.dcisample_a.data.shipping.cargo.TrackingId;
import com.marcgrue.dcisample_a.data.shipping.delivery.Delivery;
import com.marcgrue.dcisample_a.infrastructure.dci.Context;
import com.marcgrue.dcisample_a.infrastructure.dci.RoleMixin;
import org.joda.time.DateMidnight;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import java.util.Date;
import java.util.List;

/**
 * Book New Cargo use case
 */
public class BookNewCargo extends Context
{
    // ROLES ---------------------------------------------------------------------

    // Methodful Roles
    private CargoFactoryRole cargoFactory;
    private RoutingFacadeRole routingFacade;

    // Methodless Roles
    private Location origin;
    private Location destination;
    private Date arrivalDeadline;
    private Itinerary itinerary;


    // CONTEXT CONSTRUCTORS ------------------------------------------------------

    public BookNewCargo( Cargos cargos,
                         Location origin,
                         Location destination,
                         Date arrivalDeadline ) throws Exception
    {
        cargoFactory = rolePlayer( CargoFactoryRole.class, cargos );
        this.origin = origin;
        this.destination = destination;
        this.arrivalDeadline = arrivalDeadline;
    }

    public BookNewCargo( Cargo cargo )
    {
        routingFacade = rolePlayer( RoutingFacadeRole.class, cargo );
    }

    public BookNewCargo( Cargo cargo, Itinerary itinerary )
    {
        routingFacade = rolePlayer( RoutingFacadeRole.class, cargo );
        this.itinerary = itinerary;
    }

    // Constructor proxies for communication layer

    public BookNewCargo( String originId, String destinationId, Date deadline ) throws Exception
    {
        this( loadEntity( CargosEntity.class, CargosEntity.CARGOS_ID ),
              loadEntity( Location.class, originId ),
              loadEntity( Location.class, destinationId ),
              deadline );
    }

    public BookNewCargo( String trackingIdString )
    {
        this( loadEntity( CargoEntity.class, trackingIdString ) );
    }

    public BookNewCargo( String trackingIdString, Itinerary itinerary )
    {
        this( loadEntity( Cargo.class, trackingIdString ), itinerary );
    }


    // INTERACTIONS --------------------------------------------------------------

    public TrackingId book()
    {
        return cargoFactory.createCargo( null );
    }

    public TrackingId createCargo( String trackingIdString )
    {
        return cargoFactory.createCargo( trackingIdString );
    }

    public void changeDestination( String destination )
    {
        routingFacade.changeDestination( loadEntity( LocationEntity.class, destination ) );
    }

    public List<Itinerary> routeCandidates() throws FoundNoRoutesException
    {
        return routingFacade.routeCandidates();
    }

    public void assignCargoToRoute()
    {
        routingFacade.assignCargoToRoute();
    }


    // METHODFUL ROLE IMPLEMENTATIONS --------------------------------------------

    @Mixins( CargoFactoryRole.Mixin.class )
    public interface CargoFactoryRole
    {
        void setContext( BookNewCargo context );

        TrackingId createCargo( @Optional String trackingIdString );

        class Mixin
              extends RoleMixin<BookNewCargo>
              implements CargoFactoryRole
        {
            @This
            Cargos cargos;

            public TrackingId createCargo( String trackingIdString )
            {
                // New route specification
                RouteSpecification routeSpec = context.buildRouteSpecification(
                      vbf, context.origin, context.destination, context.arrivalDeadline );

                // Build delivery snapshot from route specification
                Delivery delivery = new BuildDeliverySnapshot( routeSpec ).get();

                // Create cargo
                Cargo cargo = cargos.createCargo( routeSpec, delivery, trackingIdString );

                return cargo.trackingId().get();
            }
        }
    }

    @Mixins( RoutingFacadeRole.Mixin.class )
    public interface RoutingFacadeRole
    {
        void setContext( BookNewCargo context );

        List<Itinerary> routeCandidates() throws FoundNoRoutesException;

        void assignCargoToRoute();
        void changeDestination( Location destination );

        class Mixin
              extends RoleMixin<BookNewCargo>
              implements RoutingFacadeRole
        {
            @This
            Cargo cargo;

            @Service
            RoutingService routingService;

            // Use case step 3 - system calculates possible routes
            public List<Itinerary> routeCandidates()
                  throws FoundNoRoutesException  // Deviation 3a
            {
                return routingService.fetchRoutesForSpecification( cargo.routeSpecification().get() );
            }

            public void assignCargoToRoute()
            {
                cargo.itinerary().set( context.itinerary );

                if (cargo.delivery().get().lastHandlingEvent().get() != null)
                {
                    // We treat subsequent route assignments as reroutes of misdirected cargo
                    cargo.delivery().get().lastHandlingEvent().get().wasUnexpected().set( true );
                }

                // Build delivery snapshot with updated itinerary
                cargo.delivery().set( new BuildDeliverySnapshot( cargo ).get() );
            }

            public void changeDestination( Location newDestination )
            {
                Location currentOrigin = cargo.routeSpecification().get().origin().get();
                Date currentDeadline = cargo.routeSpecification().get().arrivalDeadline().get();

                RouteSpecification newRouteSpecification =
                      context.buildRouteSpecification( vbf, currentOrigin, newDestination, currentDeadline );

                cargo.routeSpecification().set( newRouteSpecification );

                // Build new delivery snapshot with updated route specification
                cargo.delivery().set( new BuildDeliverySnapshot( cargo ).get() );
            }
        }
    }

    public RouteSpecification buildRouteSpecification(
          ValueBuilderFactory vbf, Location origin, Location destination, Date deadline )
    {
        if (origin == destination)
            throw new RuntimeException( "Origin location can't be same as destination location." );

        if (deadline == null)
            throw new RuntimeException( "Arrival deadline cannot be null." );

        Date endOfToday = new DateMidnight().plusDays( 1 ).toDate();
        if (deadline.before( endOfToday ))
            throw new RuntimeException( "Arrival deadline is in the past or Today." +
                                              "\nDeadline           " + deadline +
                                              "\nToday (midnight)   " + endOfToday );

        ValueBuilder<RouteSpecification> routeSpec = vbf.newValueBuilder( RouteSpecification.class );
        routeSpec.prototype().origin().set( origin );
        routeSpec.prototype().destination().set( destination );
        routeSpec.prototype().arrivalDeadline().set( deadline );
        return routeSpec.newInstance();
    }
}