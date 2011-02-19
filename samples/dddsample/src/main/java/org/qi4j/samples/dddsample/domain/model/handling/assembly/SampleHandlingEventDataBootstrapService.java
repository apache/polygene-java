/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.samples.dddsample.domain.model.handling.assembly;

import java.util.Date;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.CargoRepository;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.cargo.assembly.SampleCargoDataBootstrapService;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementId;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementRepository;
import org.qi4j.samples.dddsample.domain.model.carrier.assembly.SampleCarierMovementDataBootstrapService;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent;
import org.qi4j.samples.dddsample.domain.model.handling.HandlingEventFactory;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;
import org.qi4j.samples.dddsample.domain.model.location.assembly.SampleLocationDataBootstrapService;

import static org.qi4j.samples.dddsample.domain.model.SampleConstants.*;
import static org.qi4j.samples.dddsample.domain.model.cargo.assembly.SampleCargoDataBootstrapService.*;
import static org.qi4j.samples.dddsample.domain.model.carrier.assembly.SampleCarierMovementDataBootstrapService.*;
import static org.qi4j.samples.dddsample.domain.model.handling.HandlingEvent.Type.*;
import static org.qi4j.samples.dddsample.domain.model.location.assembly.SampleLocationDataBootstrapService.*;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( SampleHandlingEventDataBootstrapService.HandlingEventBootstrapServiceMixin.class )
public interface SampleHandlingEventDataBootstrapService
    extends Activatable, ServiceComposite
{
    public class HandlingEventBootstrapServiceMixin
        implements Activatable
    {
        static final Object[][] HANDLING_EVENTS =
            {
                //XYZ (SESTO-FIHEL-DEHAM-CNHKG-JPTOK-AUMEL)
                { offset( 0 ), offset( 0 ), RECEIVE, STOCKHOLM, null, XYZ },
                { offset( 4 ), offset( 5 ), LOAD, STOCKHOLM, CAR_001, XYZ },
                { offset( 14 ), offset( 14 ), UNLOAD, HELSINKI, CAR_001, XYZ },
                { offset( 15 ), offset( 15 ), LOAD, HELSINKI, CAR_002, XYZ },
                { offset( 30 ), offset( 30 ), UNLOAD, HAMBURG, CAR_002, XYZ },
                { offset( 33 ), offset( 33 ), LOAD, HAMBURG, CAR_003, XYZ },
                { offset( 34 ), offset( 34 ), UNLOAD, HONGKONG, CAR_003, XYZ },
                { offset( 60 ), offset( 60 ), LOAD, HONGKONG, CAR_004, XYZ },
                { offset( 70 ), offset( 71 ), UNLOAD, TOKYO, CAR_004, XYZ },
                { offset( 75 ), offset( 75 ), LOAD, TOKYO, CAR_005, XYZ },
                { offset( 88 ), offset( 88 ), UNLOAD, MELBOURNE, CAR_005, XYZ },
                { offset( 100 ), offset( 102 ), CLAIM, MELBOURNE, null, XYZ },

                //ZYX (AUMEL - USCHI - DEHAM -)
                { offset( 200 ), offset( 201 ), RECEIVE, MELBOURNE, null, ZYX },
                { offset( 202 ), offset( 202 ), LOAD, MELBOURNE, CAR_007, ZYX },
                { offset( 208 ), offset( 208 ), UNLOAD, CHICAGO, CAR_007, ZYX },
                { offset( 212 ), offset( 212 ), LOAD, CHICAGO, CAR_008, ZYX },
                { offset( 230 ), offset( 230 ), UNLOAD, HAMBURG, CAR_008, ZYX },
                { offset( 235 ), offset( 235 ), LOAD, HAMBURG, CAR_009, ZYX },

                //ABC
                { offset( 20 ), offset( 21 ), CLAIM, MELBOURNE, null, ABC },

                //CBA
                { offset( 0 ), offset( 1 ), RECEIVE, MELBOURNE, null, CBA },
                { offset( 10 ), offset( 11 ), LOAD, MELBOURNE, CAR_007, CBA },
                { offset( 20 ), offset( 21 ), UNLOAD, CHICAGO, CAR_007, CBA },

                //FGH
                { offset( 100 ), offset( 160 ), RECEIVE, HONGKONG, null, FGH },
                { offset( 150 ), offset( 110 ), LOAD, HONGKONG, CAR_010, FGH },

                // JKL
                { offset( 200 ), offset( 220 ), RECEIVE, HAMBURG, null, JKL },
                { offset( 300 ), offset( 330 ), LOAD, HAMBURG, CAR_020, JKL },
                { offset( 400 ), offset( 440 ), UNLOAD, HELSINKI, CAR_020, JKL }  // Unexpected event
            };

        // Do not remove. This is to ensure the order of persisted data are correct.
        @Service
        private SampleLocationDataBootstrapService dependency1;
        @Service
        private SampleCargoDataBootstrapService dependency2;
        @Service
        private SampleCarierMovementDataBootstrapService dependency3;

        @Structure
        private UnitOfWorkFactory uowf;
        @Service
        private HandlingEventFactory factory;

        @Service
        private LocationRepository locationRepository;
        @Service
        private CarrierMovementRepository carrierMovementRepository;
        @Service
        private CargoRepository cargoRepository;

        public void activate()
            throws Exception
        {
            // TODO: Remove the 3 lines below when Dependency Ordering is solved.
            dependency1.type();
            dependency2.type();
            dependency3.type();

            UnitOfWork uow = uowf.newUnitOfWork();

            try
            {
                for( Object[] handlingEvent : HANDLING_EVENTS )
                {
                    Date completionTime = (Date) handlingEvent[ 0 ];
                    Date registrationTime = (Date) handlingEvent[ 1 ];
                    HandlingEvent.Type type = (HandlingEvent.Type) handlingEvent[ 2 ];
                    Location location = findLocation( (String) handlingEvent[ 3 ] );
                    CarrierMovement carrierMovement = findCarrierMovement( (CarrierMovementId) handlingEvent[ 4 ] );
                    Cargo cargo = findCargo( (TrackingId) handlingEvent[ 5 ] );

                    createHandlingEvent(
                        uow, completionTime, registrationTime, type, location, carrierMovement, cargo
                    );
                }
            }
            finally
            {
                uow.complete();
            }
        }

        private Cargo findCargo( TrackingId trackingId )
        {
            return cargoRepository.find( trackingId );
        }

        private Location findLocation( String unLocodeString )
        {
            UnLocode unLocode = new UnLocode( unLocodeString );
            return locationRepository.find( unLocode );
        }

        private CarrierMovement findCarrierMovement( CarrierMovementId carrierMovementId )
        {
            if( carrierMovementId == null )
            {
                return null;
            }

            return carrierMovementRepository.find( carrierMovementId );
        }

        private void createHandlingEvent(
            UnitOfWork uow,
            Date completionTime,
            Date registrationTime,
            HandlingEvent.Type type,
            Location location,
            CarrierMovement carrierMovement,
            Cargo cargo
        )
        {
            EntityBuilder<HandlingEvent> builder = uow.newEntityBuilder( HandlingEvent.class );

            HandlingEventState state = builder.instanceFor( HandlingEventState.class );
            state.completionTime().set( completionTime );
            state.registrationTime().set( registrationTime );
            state.cargo().set( cargo );
            state.eventType().set( type );
            state.location().set( location );
            state.carrierMovement().set( carrierMovement );

            // TODO: Remove this when query+association is implemented
            final TrackingId trackingId = cargo.trackingId();
            final String identity = trackingId.idString();
            state.cargoTrackingId().set( identity );

            // create entity
            builder.newInstance();
        }

        public void passivate()
            throws Exception
        {
            // Do nothing
        }
    }
}
