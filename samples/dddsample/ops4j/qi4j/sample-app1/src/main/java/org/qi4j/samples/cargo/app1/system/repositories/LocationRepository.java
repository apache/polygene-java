package org.qi4j.samples.cargo.app1.system.repositories;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.samples.cargo.app1.model.location.Location;


/**
 *
 */
@Mixins(LocationRepository.LocationRepositoryMixin.class)
public interface LocationRepository extends ServiceComposite {

    Location findLocationByUnLocode(String unLoCode);

    Query<Location> findAll();

    public static abstract class LocationRepositoryMixin
            implements LocationRepository, Activatable {
        
        @Structure
        private QueryBuilderFactory qbf;

        @Structure
        private UnitOfWorkFactory uowf;

        public Location findLocationByUnLocode(final String unLoCode) {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.get(Location.class, createIdentity(unLoCode));
        }

        public Query<Location> findAll() {
            QueryBuilder<Location> builder = qbf.newQueryBuilder(Location.class);
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.newQuery( builder );
        }

        public void activate() throws Exception {
            final Usecase usecase = UsecaseBuilder.newUsecase("Populate Sample Locations");
            UnitOfWork uow = uowf.newUnitOfWork(usecase);
            try {
                createLocation(uow, "CNHKG", "Hongkong");
                createLocation(uow, "AUMEL", "Melbourne");
                createLocation(uow, "SESTO", "Stockholm");
                createLocation(uow, "FIHEL", "Helsinki");
                createLocation(uow, "USCHI", "Chicago");
                createLocation(uow, "JNTKO", "Tokyo");
                createLocation(uow, "DEHAM", "Hamburg");
                createLocation(uow, "CNSHA", "Shanghai");
                createLocation(uow, "NLRTM", "Rotterdam");
                createLocation(uow, "SEGOT", "Göteborg");
                createLocation(uow, "CNHGH", "Hangzhou");
                createLocation(uow, "USNYC", "New York");
                createLocation(uow, "USDAL", "Dallas");
                uow.complete();
            } catch (RuntimeException e) {
                uow.discard();
            }
        }

        public void passivate() throws Exception {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        private Location createLocation(UnitOfWork uow, final String identifier, String commonName) {
            try {
                Location location = uow.newEntity(Location.class, createIdentity(identifier));
                location.commonName().set(commonName);
                return location;
            } catch (RuntimeException e) {
                // already exists? ignore
                return null;
            }
        }

        private String createIdentity(final String unLoCode) {
            return "Location:" + unLoCode;
        }
    }
}