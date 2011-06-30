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
package org.qi4j.samples.dddsample.domain.model.carrier.assembly;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovement;
import org.qi4j.samples.dddsample.domain.model.carrier.CarrierMovementId;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;
import org.qi4j.samples.dddsample.domain.model.location.assembly.SampleLocationDataBootstrapService;

import java.util.Date;

import static org.qi4j.samples.dddsample.domain.model.SampleConstants.offset;
import static org.qi4j.samples.dddsample.domain.model.location.assembly.SampleLocationDataBootstrapService.*;

/**
 * @author edward.yakop@gmail.com
 * @see org.qi4j.samples.dddsample.domain.model.location.assembly.SampleLocationDataBootstrapService This needs to be included to module.
 * @since 0.5
 */
@Mixins( SampleCarierMovementDataBootstrapService.CarrierMovementBootstrapServiceMixin.class )
public interface SampleCarierMovementDataBootstrapService
    extends Activatable, ServiceComposite
{
    // SESTO-FIHEL-DEHAM-CNHKG-JPTOK-AUMEL
    public static final CarrierMovementId CAR_001 = new CarrierMovementId( "CAR_001" ); // SESTO - FIHEL
    public static final CarrierMovementId CAR_002 = new CarrierMovementId( "CAR_002" ); // FIHEL - DEHAM
    public static final CarrierMovementId CAR_003 = new CarrierMovementId( "CAR_003" ); // DEHAM - CNHKG
    public static final CarrierMovementId CAR_004 = new CarrierMovementId( "CAR_004" ); // CNHKG - JPTOK
    public static final CarrierMovementId CAR_005 = new CarrierMovementId( "CAR_005" ); // JPTOK - AUMEL

    public static final CarrierMovementId CAR_006 = new CarrierMovementId( "CAR_006" ); // FIHEL - SESTO

    // AUMEL - USCHI - DEHAM - SESTO
    public static final CarrierMovementId CAR_007 = new CarrierMovementId( "CAR_007" ); // AUMEL - USCHI
    public static final CarrierMovementId CAR_008 = new CarrierMovementId( "CAR_008" ); // USCHI - DEHAM
    public static final CarrierMovementId CAR_009 = new CarrierMovementId( "CAR_009" ); // DEHAM - SESTO

    public static final CarrierMovementId CAR_010 = new CarrierMovementId( "CAR_010" ); // CNHKG - AUMEL
    public static final CarrierMovementId CAR_011 = new CarrierMovementId( "CAR_011" ); // AUMEL - FIHEL
    public static final CarrierMovementId CAR_020 = new CarrierMovementId( "CAR_020" ); // DEHAM - SESTO
    public static final CarrierMovementId CAR_021 = new CarrierMovementId( "CAR_021" ); // SESTO - USCHI
    public static final CarrierMovementId CAR_022 = new CarrierMovementId( "CAR_022" ); // USCHI - JPTKO

    public class CarrierMovementBootstrapServiceMixin
        implements Activatable
    {
        static final Object[][] CARRIER_MOVEMENTS;

        static
        {
            Date departureTime = offset( 1 );
            Date arrivalTime = offset( 2 );

            CARRIER_MOVEMENTS = new Object[][]
                {
                    // SESTO-FIHEL-DEHAM-CNHKG-JPTOK-AUMEL
                    { CAR_001, STOCKHOLM, HELSINKI, departureTime, arrivalTime },
                    { CAR_002, HELSINKI, HAMBURG, departureTime, arrivalTime },
                    { CAR_003, HAMBURG, HONGKONG, departureTime, arrivalTime },
                    { CAR_004, HONGKONG, TOKYO, departureTime, arrivalTime },
                    { CAR_005, TOKYO, MELBOURNE, departureTime, arrivalTime },

                    // FIHEL - SESTO
                    { CAR_006, HELSINKI, STOCKHOLM, departureTime, arrivalTime },

                    // AUMEL - USCHI - DEHAM - SESTO
                    { CAR_007, MELBOURNE, CHICAGO, departureTime, arrivalTime },
                    { CAR_008, CHICAGO, HAMBURG, departureTime, arrivalTime },
                    { CAR_009, HAMBURG, STOCKHOLM, departureTime, arrivalTime },

                    // CNHKG - AUMEL
                    { CAR_010, HONGKONG, MELBOURNE, departureTime, arrivalTime },
                    // AUMEL - FIHEL
                    { CAR_011, MELBOURNE, HELSINKI, departureTime, arrivalTime },
                    // DEHAM - SESTO
                    { CAR_020, HAMBURG, STOCKHOLM, departureTime, arrivalTime },
                    // SESTO - USCHI
                    { CAR_021, STOCKHOLM, CHICAGO, departureTime, arrivalTime },
                    // USCHI - JPTKO
                    { CAR_022, CHICAGO, TOKYO, departureTime, arrivalTime }
                };
        }

        // Do not remove. This is to ensure the order of persisted data are correct.
        @Service
        private SampleLocationDataBootstrapService dependency;

        @Structure
        private UnitOfWorkFactory uowf;
        @Service
        private LocationRepository locationRepository;

        public void activate()
            throws Exception
        {
            UnitOfWork uow = uowf.newUnitOfWork();

            try
            {
                for( Object[] carrierMovement : CARRIER_MOVEMENTS )
                {
                    CarrierMovementId id = (CarrierMovementId) carrierMovement[ 0 ];
                    Location origin = findLocation( (String) carrierMovement[ 1 ] );
                    Location destination = findLocation( (String) carrierMovement[ 2 ] );
                    Date departureTime = (Date) carrierMovement[ 3 ];
                    Date arrivalTime = (Date) carrierMovement[ 4 ];
                    createCarierMovement( uow, id, origin, destination, departureTime, arrivalTime );
                }
            }
            finally
            {
                uow.complete();
            }
        }

        private Location findLocation( String unlocode )
        {
            return locationRepository.find( new UnLocode( unlocode ) );
        }

        private void createCarierMovement(
            UnitOfWork uow,
            CarrierMovementId id,
            Location from, Location to,
            Date departureTime, Date arrivalTime
        )
        {
            String idString = id.idString();
            EntityBuilder<CarrierMovement> builder = uow.newEntityBuilder( CarrierMovement.class, idString );
            CarrierMovementState state = builder.instanceFor( CarrierMovementState.class );
            state.from().set( from );
            state.to().set( to );

            state.departureTime().set( departureTime );
            state.arrivalTime().set( arrivalTime );

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