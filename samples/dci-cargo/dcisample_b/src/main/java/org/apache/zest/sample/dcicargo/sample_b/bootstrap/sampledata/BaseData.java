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
package org.apache.zest.sample.dcicargo.sample_b.bootstrap.sampledata;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.sample.dcicargo.sample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.NextHandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.itinerary.Leg;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.location.Location;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.location.UnLocode;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.CarrierMovement;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.Schedule;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.Voyage;

/**
 * Test base class with shared data and factory methods.
 */
public abstract class BaseData
{
    private static Random random = new Random();
    protected UnLocode AUMEL;
    protected UnLocode CNHGH;
    protected UnLocode CNHKG;
    protected UnLocode CNSHA;
    protected UnLocode DEHAM;
    protected UnLocode FIHEL;
    protected UnLocode JNTKO;
    protected UnLocode NLRTM;
    protected UnLocode SEGOT;
    protected UnLocode SESTO;
    protected UnLocode SOMGQ;
    protected UnLocode USCHI;
    protected UnLocode USDAL;
    protected UnLocode USNYC;
    protected ValueBuilderFactory vbf;

    protected BaseData( ValueBuilderFactory vbf )
    {
        this.vbf = vbf;
    }

    protected UnLocode unlocode( String unlocodeString )
    {
        ValueBuilder<UnLocode> unlocode = vbf.newValueBuilder( UnLocode.class );
        unlocode.prototype().code().set( unlocodeString );
        return unlocode.newInstance();
    }

    protected CarrierMovement carrierMovement( Location depLoc, Location arrLoc, LocalDate depDate, LocalDate arrDate )
    {
        ValueBuilder<CarrierMovement> carrierMovement = vbf.newValueBuilder( CarrierMovement.class );
        carrierMovement.prototype().departureLocation().set( depLoc );
        carrierMovement.prototype().arrivalLocation().set( arrLoc );
        carrierMovement.prototype().departureDate().set( depDate );
        carrierMovement.prototype().arrivalDate().set( arrDate );
        return carrierMovement.newInstance();
    }

    protected Schedule schedule( CarrierMovement... carrierMovements )
    {
        ValueBuilder<Schedule> schedule = vbf.newValueBuilder( Schedule.class );
        List<CarrierMovement> cm = new ArrayList<>();
        cm.addAll( Arrays.asList( carrierMovements ) );
        schedule.prototype().carrierMovements().set( cm );
        return schedule.newInstance();
    }

    protected Leg leg( Voyage voyage, Location load, Location unload, LocalDate loadDate, LocalDate unloadDate )
    {
        ValueBuilder<Leg> leg = vbf.newValueBuilder( Leg.class );
        leg.prototype().voyage().set( voyage );
        leg.prototype().loadLocation().set( load );
        leg.prototype().unloadLocation().set( unload );
        leg.prototype().loadDate().set( loadDate );
        leg.prototype().unloadDate().set( unloadDate );
        return leg.newInstance();
    }

    protected Itinerary itinerary( Leg... legArray )
    {
        ValueBuilder<Itinerary> itinerary = vbf.newValueBuilder( Itinerary.class );
        List<Leg> legs = new ArrayList<>();
        legs.addAll( Arrays.asList( legArray ) );
        itinerary.prototype().legs().set( legs );
        return itinerary.newInstance();
    }

    protected Delivery delivery(
        HandlingEvent lastHandlingEvent,
        TransportStatus transportStatus,
        Boolean isUnloadedAtDestination,
        RoutingStatus routingStatus,
        Boolean isMisdirected,
        LocalDate eta,
        Integer itineraryProgressIndex,
        NextHandlingEvent nextHandlingEvent
    )
    {
        ValueBuilder<Delivery> delivery = vbf.newValueBuilder( Delivery.class );
        delivery.prototype().timestamp().set( Instant.now() );
        delivery.prototype().lastHandlingEvent().set( lastHandlingEvent );
        delivery.prototype().transportStatus().set( transportStatus );
        delivery.prototype().isUnloadedAtDestination().set( isUnloadedAtDestination );
        delivery.prototype().routingStatus().set( routingStatus );
        delivery.prototype().isMisdirected().set( isMisdirected );
        delivery.prototype().eta().set( eta );
        delivery.prototype().itineraryProgressIndex().set( itineraryProgressIndex );
        delivery.prototype().nextHandlingEvent().set( nextHandlingEvent );
        return delivery.newInstance();
    }

    // Delivery with only mandatory values
    protected Delivery delivery( LocalDate date,
                                 TransportStatus transportStatus,
                                 RoutingStatus routingStatus,
                                 Integer itineraryProgressIndex
    )
    {
        ValueBuilder<Delivery> delivery = vbf.newValueBuilder( Delivery.class );
        delivery.prototype().timestamp().set( date.atStartOfDay().toInstant( ZoneOffset.UTC ) );
        delivery.prototype().transportStatus().set( transportStatus );
        delivery.prototype().routingStatus().set( routingStatus );
        delivery.prototype().itineraryProgressIndex().set( itineraryProgressIndex );
        return delivery.newInstance();
    }

    protected NextHandlingEvent nextHandlingEvent( HandlingEventType handlingEventType,
                                                   Location location,
                                                   LocalDate time,
                                                   Voyage voyage
    )
    {
        ValueBuilder<NextHandlingEvent> nextHandlingEvent = vbf.newValueBuilder( NextHandlingEvent.class );
        nextHandlingEvent.prototype().handlingEventType().set( handlingEventType );
        nextHandlingEvent.prototype().location().set( location );
        nextHandlingEvent.prototype().date().set( time );
        nextHandlingEvent.prototype().voyage().set( voyage );
        return nextHandlingEvent.newInstance();
    }

    protected ParsedHandlingEventData parsedHandlingEventData( LocalDate registrationDate,
                                                               LocalDate completionDate,
                                                               String trackingIdString,
                                                               HandlingEventType handlingEventType,
                                                               String unLocodeString,
                                                               String voyageNumberString
    )
        throws Exception
    {
        ValueBuilder<ParsedHandlingEventData> attempt = vbf.newValueBuilder( ParsedHandlingEventData.class );
        attempt.prototype().registrationDate().set( registrationDate );
        attempt.prototype().completionDate().set( completionDate );
        attempt.prototype().trackingIdString().set( trackingIdString );
        attempt.prototype().handlingEventType().set( handlingEventType );
        attempt.prototype().unLocodeString().set( unLocodeString );
        attempt.prototype().voyageNumberString().set( voyageNumberString );

        return attempt.newInstance();
    }

    protected static LocalDate day( int days )
    {
        return LocalDate.now().plusDays( days );
    }
}
