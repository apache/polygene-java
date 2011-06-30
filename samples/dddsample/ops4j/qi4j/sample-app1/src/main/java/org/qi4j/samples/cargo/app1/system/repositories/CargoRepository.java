package org.qi4j.samples.cargo.app1.system.repositories;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.samples.cargo.app1.model.cargo.Cargo;
import org.qi4j.samples.cargo.app1.model.cargo.TrackingId;

import java.util.UUID;

/**
 *
 */
public interface CargoRepository extends ServiceComposite {

    /**
     * Finds a cargo using given id.
     *
     * @param trackingId Id
     * @return Cargo if found, else {@code null}
     */
    Cargo find(TrackingId trackingId);

    /**
     * Finds all cargo.
     *
     * @return All cargo.
     */
    Query<Cargo> findAll();

    /**
     * @return A unique, generated tracking Id.
     */
    TrackingId nextTrackingId();

    public static abstract class CargoRepositoryMixin
            implements CargoRepository {
        @Structure
        private UnitOfWorkFactory uowf;

        @Structure
        private QueryBuilderFactory qbf;

        @Structure
        private ValueBuilderFactory vbf;

        public TrackingId nextTrackingId() {
            String uuid = UUID.randomUUID().toString();
            ValueBuilder<TrackingId> builder = vbf.newValueBuilder(TrackingId.class);
            builder.prototype().id().set(uuid);
            return builder.newInstance();
        }

        /**
         * Finds a cargo using given id.
         *
         * @param trackingId Id
         * @return Cargo if found, else {@code null}
         */
        public Cargo find(TrackingId trackingId) {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.get(Cargo.class, trackingId.id().get() );
        }

        /**
         * Finds all cargo.
         *
         * @return All cargo.
         */
        public Query<Cargo> findAll() {
            QueryBuilder<Cargo> builder = qbf.newQueryBuilder(Cargo.class);
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.newQuery( builder );
        }
    }
}