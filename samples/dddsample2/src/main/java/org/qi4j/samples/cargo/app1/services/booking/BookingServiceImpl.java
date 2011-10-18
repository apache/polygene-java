package org.qi4j.samples.cargo.app1.services.booking;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.samples.cargo.app1.model.cargo.Cargo;
import org.qi4j.samples.cargo.app1.model.cargo.Itinerary;
import org.qi4j.samples.cargo.app1.model.cargo.RouteSpecification;
import org.qi4j.samples.cargo.app1.model.cargo.TrackingId;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.services.routing.RoutingService;
import org.qi4j.samples.cargo.app1.system.FutureDate;
import org.qi4j.samples.cargo.app1.system.UnLocode;
import org.qi4j.samples.cargo.app1.system.factories.CargoFactory;
import org.qi4j.samples.cargo.app1.system.factories.RouteSpecificationFactory;
import org.qi4j.samples.cargo.app1.system.repositories.CargoRepository;
import org.qi4j.samples.cargo.app1.system.repositories.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BookingServiceImpl
    implements BookingService
{

    private static final Logger logger = LoggerFactory.getLogger( BookingService.class );

    @Service
    private CargoRepository cargoRepository;

    @Service
    private LocationRepository locationRepository;

    @Service
    private RoutingService routingService;

    @Service
    private CargoFactory cargoFactory;

    @Service
    private RouteSpecificationFactory routeSpecificationFactory;

    @UnitOfWorkPropagation
    public TrackingId bookNewCargo( @UnLocode String originUnLocode,
                                    @UnLocode String destinationUnLocode,
                                    @FutureDate Date arrivalDeadline )
    {
        Cargo cargo = cargoFactory.create( originUnLocode, destinationUnLocode, arrivalDeadline );
        return cargo.trackingId();
    }

    @UnitOfWorkPropagation
    public List<Itinerary> requestPossibleRoutesForCargo( TrackingId trackingId )
    {
        final Cargo cargo = cargoRepository.find( trackingId );
        if( cargo == null )
        {
            return Collections.emptyList();
        }
        return routingService.fetchRoutesForSpecification( cargo.routeSpecification() );
    }

    @UnitOfWorkPropagation
    public void assignCargoToRoute( Itinerary itinerary, TrackingId trackingId )
    {
        final Cargo cargo = cargoRepository.find( trackingId );
        if( cargo == null )
        {
            throw new IllegalArgumentException( "Can't assign itinerary to non-existing cargo " + trackingId );
        }
        cargo.assignToRoute( itinerary );
        logger.info( "Assigned cargo " + trackingId + " to new route" );
    }

    /**
     * Changes the destination of a cargo.
     *
     * @param trackingId cargo tracking id
     * @param unLocode   UN locode of new destination
     */
    @UnitOfWorkPropagation
    public void changeDestination( TrackingId trackingId, @UnLocode String unLocode )
    {
        Cargo cargo = cargoRepository.find( trackingId );
        Location newDestination = locationRepository.findLocationByUnLocode( unLocode );
        RouteSpecification routeSpecification = routeSpecificationFactory.create(
            cargo.origin(), newDestination, cargo.routeSpecification().arrivalDeadline()
        );
        cargo.specifyNewRoute( routeSpecification );
        logger.info( "Changed destination for cargo " + trackingId + " to " + routeSpecification.destination() );
    }

}
