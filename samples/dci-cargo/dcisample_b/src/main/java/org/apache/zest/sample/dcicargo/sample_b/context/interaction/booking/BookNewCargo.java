/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.sample_b.context.interaction.booking;

import java.util.Date;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.sample.dcicargo.sample_b.data.aggregateroot.CargoAggregateRoot;
import org.apache.zest.sample.dcicargo.sample_b.data.factory.CargoFactory;
import org.apache.zest.sample.dcicargo.sample_b.data.factory.RouteSpecificationFactoryService;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.cargo.Cargo;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.NextHandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.location.Location;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.tracking.TrackingId;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.dci.Context;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.dci.RoleMixin;

import static org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType.RECEIVE;

/**
 * Book New Cargo (use case)
 *
 * Cargo Owner starts with the 1st step in the overall Ship Cargo use case by booking a cargo shipment
 * in our shipping application.
 *
 * The Cargo Owner provides an origin and destination location and a arrival deadline to the booking
 * application. A route specification is created from the input and attached to a new cargo with
 * a unique tracking id.
 */
public class BookNewCargo extends Context
{
    private BookingSystemRole bookingSystem;

    private Location origin;
    private Location destination;
    private Date arrivalDeadline;

    public BookNewCargo( CargoFactory cargoFactory, Location origin, Location destination, Date arrivalDeadline )
        throws Exception
    {
        bookingSystem = rolePlayer( BookingSystemRole.class, cargoFactory );
        this.origin = origin;
        this.destination = destination;
        this.arrivalDeadline = arrivalDeadline;
    }

    public BookNewCargo( String originId, String destinationId, Date deadline )
        throws Exception
    {
        this( loadEntity( CargoAggregateRoot.class, CargoAggregateRoot.CARGOS_ID ),
              loadEntity( Location.class, originId ),
              loadEntity( Location.class, destinationId ),
              deadline );
    }

    public TrackingId getTrackingId()
        throws Exception
    {
        return bookingSystem.createCargo( null );
    }

    public TrackingId withTrackingId( String trackingIdString )
        throws Exception
    {
        return bookingSystem.createCargo( trackingIdString );
    }

    @Mixins( BookingSystemRole.Mixin.class )
    public interface BookingSystemRole
    {
        void setContext( BookNewCargo context );

        TrackingId createCargo( @Optional String trackingIdString )
            throws Exception;

        class Mixin
            extends RoleMixin<BookNewCargo>
            implements BookingSystemRole
        {
            @This
            CargoFactory cargoFactory;

            @Service
            RouteSpecificationFactoryService routeSpecFactory;

            public TrackingId createCargo( String trackingIdString )
                throws Exception
            {
                Date earliestDeparture = new Date();
                RouteSpecification routeSpec = routeSpecFactory.build( c.origin,
                                                                       c.destination,
                                                                       earliestDeparture,
                                                                       c.arrivalDeadline );

                ValueBuilder<Delivery> delivery = vbf.newValueBuilder( Delivery.class );
                delivery.prototype().timestamp().set( new Date() );
                delivery.prototype().transportStatus().set( TransportStatus.NOT_RECEIVED );
                delivery.prototype().routingStatus().set( RoutingStatus.NOT_ROUTED );

                // Expect receipt in origin (time unknown / no voyage yet)
                ValueBuilder<NextHandlingEvent> nextEvent = vbf.newValueBuilder( NextHandlingEvent.class );
                nextEvent.prototype().handlingEventType().set( RECEIVE );
                nextEvent.prototype().location().set( routeSpec.origin().get() );
                delivery.prototype().nextHandlingEvent().set( nextEvent.newInstance() );

                Cargo cargo = cargoFactory.createCargo( routeSpec, delivery.newInstance(), trackingIdString );

                return cargo.trackingId().get();
            }
        }
    }
}