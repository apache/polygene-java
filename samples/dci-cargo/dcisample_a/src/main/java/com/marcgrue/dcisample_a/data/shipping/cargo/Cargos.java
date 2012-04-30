package com.marcgrue.dcisample_a.data.shipping.cargo;

import com.marcgrue.dcisample_a.data.shipping.delivery.Delivery;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import java.util.UUID;

/**
 * Cargo "collection" - could have had a many-association to cargos if it was part of the domain model.
 */
@Mixins( Cargos.Mixin.class )
public interface Cargos
{
    Cargo createCargo( RouteSpecification routeSpecification, Delivery delivery, @Optional String id );

    class Mixin
          implements Cargos
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        public Cargo createCargo( RouteSpecification routeSpecification, Delivery delivery, String id )
        {
            TrackingId trackingId = buildTrackingId( id );

            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<Cargo> cargoBuilder = uow.newEntityBuilder( Cargo.class, trackingId.id().get() );
            cargoBuilder.instance().trackingId().set( trackingId );
            cargoBuilder.instance().origin().set( routeSpecification.origin().get() );
            cargoBuilder.instance().routeSpecification().set( routeSpecification );
            cargoBuilder.instance().delivery().set( delivery );

            return cargoBuilder.newInstance();
        }

        private TrackingId buildTrackingId( String id )
        {
            if (id == null || id.trim().equals( "" ))
            {
                // Build random tracking id
                final String uuid = UUID.randomUUID().toString().toUpperCase();
                id = uuid.substring( 0, uuid.indexOf( "-" ) );
            }

            ValueBuilder<TrackingId> trackingIdBuilder = vbf.newValueBuilder( TrackingId.class );
            trackingIdBuilder.prototype().id().set( id );
            return trackingIdBuilder.newInstance();
        }
    }
}
