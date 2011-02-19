package org.qi4j.samples.dddsample.domain.model.cargo.assembly;

import java.util.Date;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.CargoRepository;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;
import org.qi4j.samples.dddsample.domain.model.cargo.RouteSpecification;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;
import org.qi4j.samples.dddsample.domain.service.Booking;
import org.qi4j.samples.dddsample.domain.service.Routing;

@Mixins( BookingService.BookingServiceMixin.class )
interface BookingService
    extends Booking, ServiceComposite
{
    public class BookingServiceMixin
        implements Booking
    {
        @Structure
        private UnitOfWorkFactory uowf;
        @Structure
        private TransientBuilderFactory cbf;

        @Service
        private CargoRepository cargoRepository;
        @Service
        private LocationRepository locationRepository;
        @Service
        private Routing routing;

        public TrackingId bookNewCargo( UnLocode originUnLocode, UnLocode destinationUnLocode )
        {
            Location origin = locationRepository.find( originUnLocode );
            Location destination = locationRepository.find( destinationUnLocode );

            UnitOfWork uow = uowf.currentUnitOfWork();

            EntityBuilder<Cargo> builder = uow.newEntityBuilder( Cargo.class );
            CargoState cargoState = builder.instanceFor( CargoState.class );
            cargoState.origin().set( origin );
            cargoState.destination().set( destination );

            Cargo cargo = builder.newInstance();
            return cargo.trackingId();
        }

        public Query<Itinerary> requestPossibleRoutesForCargo( TrackingId trackingId )
        {
            Cargo cargo = cargoRepository.find( trackingId );
            TransientBuilder<RouteSpecification> builder = cbf.newTransientBuilder( RouteSpecification.class );
            builder.use( cargo, new Date() );
            RouteSpecification routeSpecification = builder.newInstance();
            return routing.fetchRoutesForSpecification( routeSpecification );
        }

        public void assignCargoToRoute( TrackingId trackingId, Itinerary newItinerary )
        {
            final Cargo cargo = cargoRepository.find( trackingId );
            if( cargo == null )
            {
                throw new IllegalArgumentException( "Can't assign itinerary to non-existing cargo " + trackingId );
            }

            cargo.attachItinerary( newItinerary );
        }
    }
}