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
package org.qi4j.samples.dddsample.domain.model.cargo.assembly;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.samples.dddsample.domain.model.cargo.Cargo;
import org.qi4j.samples.dddsample.domain.model.cargo.TrackingId;
import org.qi4j.samples.dddsample.domain.model.location.Location;
import org.qi4j.samples.dddsample.domain.model.location.LocationRepository;
import org.qi4j.samples.dddsample.domain.model.location.UnLocode;
import org.qi4j.samples.dddsample.domain.model.location.assembly.SampleLocationDataBootstrapService;

import static org.qi4j.samples.dddsample.domain.model.location.assembly.SampleLocationDataBootstrapService.*;

/**
 * @author edward.yakop@gmail.com
 * @see org.qi4j.samples.dddsample.domain.model.location.assembly.SampleLocationDataBootstrapService This needs to be executed before
 */
@Mixins( SampleCargoDataBootstrapService.CargoBootstrapServiceMixin.class )
public interface SampleCargoDataBootstrapService
    extends Activatable, ServiceComposite
{
    public static final TrackingId XYZ = new TrackingId( "XYZ" );
    public static final TrackingId ABC = new TrackingId( "ABC" );
    public static final TrackingId ZYX = new TrackingId( "ZYX" );
    public static final TrackingId CBA = new TrackingId( "CBA" );
    public static final TrackingId FGH = new TrackingId( "FGH" );
    public static final TrackingId JKL = new TrackingId( "JKL" );

    public class CargoBootstrapServiceMixin
        implements Activatable
    {
        static final String[][] CARGOS = new String[][]
            {
                { XYZ.idString(), STOCKHOLM, MELBOURNE },
                { ABC.idString(), STOCKHOLM, HELSINKI },
                { ZYX.idString(), MELBOURNE, STOCKHOLM },
                { CBA.idString(), HELSINKI, STOCKHOLM },
                { FGH.idString(), HONGKONG, HELSINKI },
                { JKL.idString(), HAMBURG, TOKYO }
            };

        // Do not remove. This is to ensure the order of persisted data are correct.
        @Service
        private SampleLocationDataBootstrapService dependency;

        @Service
        private BookingService bookingService;

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
                for( String[] cargo : CARGOS )
                {
                    String trackingId = cargo[ 0 ];
                    Location origin = findLocation( cargo[ 1 ] );
                    Location destination = findLocation( cargo[ 2 ] );

                    createCargo( uow, trackingId, origin, destination );
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

        private void createCargo( UnitOfWork uow, String trackingId, Location origin, Location destination )
        {
            EntityBuilder<Cargo> builder = uow.newEntityBuilder( Cargo.class, trackingId );
            CargoState cargoState = builder.instanceFor( CargoState.class );
            cargoState.origin().set( origin );
            cargoState.destination().set( destination );

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
