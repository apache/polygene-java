package org.qi4j.samples.dddsample.domain.service.assembly;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.CargoRepository;
import org.qi4j.samples.dddsample.domain.model.cargo.DeliveryHistory;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.service.Tracking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Niclas Hedhman
 * @author edward.yakop@gmail.com
 */
@Mixins( TrackingService.TrackingServiceMixin.class )
@Concerns( UnitOfWorkConcern.class )
interface TrackingService
    extends Tracking, ServiceComposite
{
    class TrackingServiceMixin
        implements Tracking
    {
        private static final Logger logger = LoggerFactory.getLogger( TrackingServiceMixin.class );

        @Service
        private CargoRepository cargoRepository;

        public Cargo track( TrackingId trackingId )
        {
            return cargoRepository.find( trackingId );
        }

        public void inspectCargo( TrackingId trackingId )
        {
            final Cargo cargo = cargoRepository.find( trackingId );
            if( cargo == null )
            {
                logger.warn( "Can't inspect non-existing cargo " + trackingId );
                return;
            }

            if( cargo.isMisdirected() )
            {
                handleMisdirectedCargo( cargo );
            }
            if( cargo.isUnloadedAtDestination() )
            {
                notifyCustomerOfAvailability( cargo );
            }
        }

        private void notifyCustomerOfAvailability( Cargo cargo )
        {
            String id = cargo.trackingId().idString();
            String destination = cargo.destination().name();
            logger.info(
                "Cargo " + id + " has been unloaded at its final destination " + destination
            );
        }

        private void handleMisdirectedCargo( Cargo cargo )
        {
            String id = cargo.trackingId().idString();
            HandlingEvent event = ( (DeliveryHistory) cargo ).lastEvent();
            logger.info(
                "Cargo " + id + " has been misdirected. Last event was " + event
            );
        }
    }
}