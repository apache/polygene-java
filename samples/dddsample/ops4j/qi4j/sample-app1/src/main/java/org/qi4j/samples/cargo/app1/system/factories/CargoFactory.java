package org.qi4j.samples.cargo.app1.system.factories;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.cargo.app1.model.cargo.Cargo;
import org.qi4j.samples.cargo.app1.model.cargo.RouteSpecification;
import org.qi4j.samples.cargo.app1.model.cargo.TrackingId;
import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.system.repositories.CargoRepository;
import org.qi4j.samples.cargo.app1.system.repositories.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


/**
 *
 */
@Mixins(CargoFactory.CargoFactoryMixin.class )
public interface CargoFactory {
    Cargo create(final String originUnLocode, final String destinationUnLocode, final Date arrivalDeadline);

    public static class CargoFactoryMixin
            implements CargoFactory {
        private static final Logger logger = LoggerFactory.getLogger(CargoFactory.class);

        @Structure
        private UnitOfWorkFactory uowf;

        @Service
        private CargoRepository cargoRepository;

        @Service
        private LocationRepository locationRepository;

        @Service
        private RouteSpecificationFactory routeSpecificationFactory;

        public Cargo create(String originUnLocode, String destinationUnLocode, Date arrivalDeadline) {
            TrackingId trackingId = cargoRepository.nextTrackingId();
            Location origin = locationRepository.findLocationByUnLocode(originUnLocode);
            Location destination = locationRepository.findLocationByUnLocode(destinationUnLocode);
            RouteSpecification routeSpecification = routeSpecificationFactory.create(origin, destination, arrivalDeadline);
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<Cargo> builder = uow.newEntityBuilder(Cargo.class, trackingId.id().get());
            Cargo.State instanceState = builder.instanceFor(Cargo.State.class);
            instanceState.routeSpecification().set(routeSpecification);
            instanceState.origin().set(origin);
            Cargo cargo = builder.newInstance();
            logger.info("Booked new cargo with tracking id " + cargo.trackingId().id().get());
            return cargo;
        }
    }
}