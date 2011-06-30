package org.qi4j.samples.cargo.app1.ui.booking;

import com.msdw.eqrisk.vorticity.EntityToValueConverter;
import com.msdw.eqrisk.vorticity.messaging.VorticityWebService;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.samples.cargo.app1.model.cargo.Cargo;
import org.qi4j.samples.cargo.app1.model.cargo.TrackingId;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.services.booking.BookingService;
import org.qi4j.samples.cargo.app1.system.repositories.CargoRepository;
import org.qi4j.samples.cargo.app1.system.repositories.LocationRepository;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@WebService(name = "bookingService", targetNamespace = "http://cargo.eqrisk.msdw.com")
@Mixins(BookingServiceFacade.BookingServiceFacadeMixin.class)
public interface BookingServiceFacade extends BookingService, VorticityWebService {

    CargoValue loadCargoForRouting(String trackingId);

    List<LocationValue> listShippingLocations();

    List<CargoValue> listAllCargos();

    public static abstract class BookingServiceFacadeMixin
            implements BookingServiceFacade {
        @Service
        private CargoRepository cargoRepository;

        @Structure
        private ValueBuilderFactory vbf;

        @Service
        private LocationRepository locationRepository;

        @Service
        private EntityToValueConverter valueConverter;

        public CargoValue loadCargoForRouting(final String trackingId) {
            final Cargo cargo = cargoRepository.find(createTrackingId(trackingId));
            return valueConverter.convertToValue(cargo, CargoValue.class);
        }

        public List<LocationValue> listShippingLocations() {
            final Query<Location> allLocations = locationRepository.findAll();
            ArrayList<LocationValue> result = new ArrayList<LocationValue>();
            for (Location location : allLocations) {
                result.add(valueConverter.convertToValue((EntityComposite) location, LocationValue.class));
            }
            return result;
        }

        public List<CargoValue> listAllCargos() {
            final Query<Cargo> cargoList = cargoRepository.findAll();
            final List<CargoValue> result = new ArrayList<CargoValue>();
            for (Cargo cargo : cargoList) {
                result.add(valueConverter.convertToValue(cargo, CargoValue.class));
            }
            return result;
        }

        private TrackingId createTrackingId(final String idNumber) {
            ValueBuilder<TrackingId> builder = vbf.newValueBuilder(TrackingId.class);
            builder.prototype().id().set(idNumber);
            return builder.newInstance();
        }

    }
}