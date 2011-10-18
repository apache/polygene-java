package org.qi4j.samples.dddsample.application.remoting.assembly;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.samples.dddsample.application.remoting.BookingFacade;
import org.qi4j.samples.dddsample.application.remoting.dto.CargoRoutingDTO;
import org.qi4j.samples.dddsample.application.remoting.dto.LocationDTO;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.CargoRepository;
import org.qi4j.samples.dddsample.domain.model.cargo.Itinerary;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementRepository;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;
import org.qi4j.samples.dddsample.domain.service.Booking;

import java.util.ArrayList;
import java.util.List;

import static org.qi4j.samples.dddsample.application.remoting.assembly.CargoRoutingDTOImpl.toDTO;

/**
 * This implementation has additional support from the infrastructure, for exposing as an RMI
 * service and for keeping the OR-mapper unit-of-work open during DTO assembly,
 * analogous to the view rendering for web interfaces.
 * <p/>
 * See context-remote.xml.
 */
@Concerns( UnitOfWorkConcern.class )
@Mixins( BookingFacadeService.BookingFacadeMixin.class )
interface BookingFacadeService
    extends BookingFacade, ServiceComposite
{
    public class BookingFacadeMixin
        implements BookingFacade
    {
        @Service
        private Booking booking;
        @Service
        private LocationRepository locationRepository;
        @Service
        private CargoRepository cargoRepository;
        @Service
        private CarrierMovementRepository carrierMovementRepository;

        public List<LocationDTO> listShippingLocations()
            throws UnitOfWorkCompletionException
        {
            Query<Location> query = locationRepository.findAll();
            List<LocationDTO> locations = new ArrayList<LocationDTO>();
            for( Location location : query )
            {
                String locationName = location.name();
                String locationUnlocode = location.unLocode().idString();
                LocationDTOImpl locationDTO = new LocationDTOImpl( locationUnlocode, locationName );
                locations.add( locationDTO );
            }

            return locations;
        }

        public String registerNewCargo( String origin, String destination )
        {
            UnLocode originCode = new UnLocode( origin );
            UnLocode destCode = new UnLocode( destination );
            TrackingId trackingId = booking.bookNewCargo( originCode, destCode );

            return trackingId.idString();
        }

        public CargoRoutingDTO loadCargoForRouting( String trackingId )
        {
            TrackingId id = new TrackingId( trackingId );
            Cargo cargo = cargoRepository.find( id );
            return toDTO( cargo );
        }

        public void assignCargoToRoute( String trackingId, Itinerary itinerary )
        {
            TrackingId id = new TrackingId( trackingId );
            booking.assignCargoToRoute( id, itinerary );
        }

        public List<CargoRoutingDTO> listAllCargos()
        {
            List<CargoRoutingDTO> cargos = new ArrayList<CargoRoutingDTO>();
            Query<Cargo> query = cargoRepository.findAll();
            for( Cargo cargo : query )
            {
                CargoRoutingDTO cargoDTO = toDTO( cargo );

                cargos.add( cargoDTO );
            }
            return cargos;
        }

        public Query<Itinerary> requestPossibleRoutesForCargo( String trackingId )
        {
            TrackingId id = new TrackingId( trackingId );
            return booking.requestPossibleRoutesForCargo( id );
        }
    }
}