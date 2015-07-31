/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_b.data.factory;

import java.util.UUID;
import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.sample.dcicargo.sample_b.data.factory.exception.CannotCreateCargoException;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_b.data.structure.tracking.TrackingId;

/**
 * Cargo factory
 *
 * If an empty tracking id string is received the factory will create an automated tracking id.
 *
 * Validations of RouteSpecification and Delivery are considered out of this scope.
 */
@Mixins( CargoFactory.Mixin.class )
public interface CargoFactory
{
    Cargo createCargo( RouteSpecification routeSpecification, Delivery delivery, @Optional String id )
        throws CannotCreateCargoException;

    class Mixin
        implements CargoFactory
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        public Cargo createCargo( RouteSpecification routeSpecification, Delivery delivery, String id )
            throws ConstraintViolationException, CannotCreateCargoException
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
            throws CannotCreateCargoException
        {
            if( id == null || id.trim().equals( "" ) )
            {
                // Build random tracking id
                final String uuid = UUID.randomUUID().toString().toUpperCase();
                id = uuid.substring( 0, uuid.indexOf( "-" ) );
            }
            else
            {
                try
                {
                    // Verify that tracking id doesn't exist in store
                    uowf.currentUnitOfWork().get( Cargo.class, id );
                    throw new CannotCreateCargoException( "Tracking id '" + id + "' is not unique." );
                }
                catch( NoSuchEntityException e )
                {
                    // Ok: tracking id is unique
                }
            }

            ValueBuilder<TrackingId> trackingIdBuilder = vbf.newValueBuilder( TrackingId.class );
            trackingIdBuilder.prototype().id().set( id );
            return trackingIdBuilder.newInstance();
        }
    }
}
