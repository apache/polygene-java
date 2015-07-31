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
package org.qi4j.sample.dcicargo.sample_b.bootstrap.sampledata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.Delivery;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.NextHandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.RoutingStatus;
import org.qi4j.sample.dcicargo.sample_b.data.structure.delivery.TransportStatus;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEventType;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Leg;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.Location;
import org.qi4j.sample.dcicargo.sample_b.data.structure.location.UnLocode;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.CarrierMovement;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Schedule;
import org.qi4j.sample.dcicargo.sample_b.data.structure.voyage.Voyage;

/**
 * Test base class with shared data and factory methods.
 */
public abstract class BaseData
{
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
    protected final Module module;

    protected BaseData( Module module )
    {
        this.module = module;
    }

    protected UnLocode unlocode( String unlocodeString )
    {
        ValueBuilder<UnLocode> unlocode = module.newValueBuilder( UnLocode.class );
        unlocode.prototype().code().set( unlocodeString );
        return unlocode.newInstance();
    }

    protected CarrierMovement carrierMovement( Location depLoc, Location arrLoc, Date depTime, Date arrTime )
    {
        ValueBuilder<CarrierMovement> carrierMovement = module.newValueBuilder( CarrierMovement.class );
        carrierMovement.prototype().departureLocation().set( depLoc );
        carrierMovement.prototype().arrivalLocation().set( arrLoc );
        carrierMovement.prototype().departureTime().set( depTime );
        carrierMovement.prototype().arrivalTime().set( arrTime );
        return carrierMovement.newInstance();
    }

    protected Schedule schedule( CarrierMovement... carrierMovements )
    {
        ValueBuilder<Schedule> schedule = module.newValueBuilder( Schedule.class );
        List<CarrierMovement> cm = new ArrayList<>();
        cm.addAll( Arrays.asList( carrierMovements ) );
        schedule.prototype().carrierMovements().set( cm );
        return schedule.newInstance();
    }

    protected Leg leg( Voyage voyage, Location load, Location unload, Date loadTime, Date unloadTime )
    {
        ValueBuilder<Leg> leg = module.newValueBuilder( Leg.class );
        leg.prototype().voyage().set( voyage );
        leg.prototype().loadLocation().set( load );
        leg.prototype().unloadLocation().set( unload );
        leg.prototype().loadTime().set( loadTime );
        leg.prototype().unloadTime().set( unloadTime );
        return leg.newInstance();
    }

    protected Itinerary itinerary( Leg... legArray )
    {
        ValueBuilder<Itinerary> itinerary = module.newValueBuilder( Itinerary.class );
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
        Date eta,
        Integer itineraryProgressIndex,
        NextHandlingEvent nextHandlingEvent
    )
    {
        ValueBuilder<Delivery> delivery = module.newValueBuilder( Delivery.class );
        delivery.prototype().timestamp().set( new Date() );
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
    protected Delivery delivery( Date date,
                                 TransportStatus transportStatus,
                                 RoutingStatus routingStatus,
                                 Integer itineraryProgressIndex
    )
    {
        ValueBuilder<Delivery> delivery = module.newValueBuilder( Delivery.class );
        delivery.prototype().timestamp().set( date );
        delivery.prototype().transportStatus().set( transportStatus );
        delivery.prototype().routingStatus().set( routingStatus );
        delivery.prototype().itineraryProgressIndex().set( itineraryProgressIndex );
        return delivery.newInstance();
    }

    protected NextHandlingEvent nextHandlingEvent( HandlingEventType handlingEventType,
                                                   Location location,
                                                   Date time,
                                                   Voyage voyage
    )
    {
        ValueBuilder<NextHandlingEvent> nextHandlingEvent = module.newValueBuilder( NextHandlingEvent.class );
        nextHandlingEvent.prototype().handlingEventType().set( handlingEventType );
        nextHandlingEvent.prototype().location().set( location );
        nextHandlingEvent.prototype().time().set( time );
        nextHandlingEvent.prototype().voyage().set( voyage );
        return nextHandlingEvent.newInstance();
    }

    protected ParsedHandlingEventData parsedHandlingEventData( Date registrationTime,
                                                               Date completionTime,
                                                               String trackingIdString,
                                                               HandlingEventType handlingEventType,
                                                               String unLocodeString,
                                                               String voyageNumberString
    )
        throws Exception
    {
        ValueBuilder<ParsedHandlingEventData> attempt = module.newValueBuilder( ParsedHandlingEventData.class );
        attempt.prototype().registrationTime().set( registrationTime );
        attempt.prototype().completionTime().set( completionTime );
        attempt.prototype().trackingIdString().set( trackingIdString );
        attempt.prototype().handlingEventType().set( handlingEventType );
        attempt.prototype().unLocodeString().set( unLocodeString );
        attempt.prototype().voyageNumberString().set( voyageNumberString );

        return attempt.newInstance();
    }

    protected static Date day( int days )
    {
        Date today = new Date();
        long aDay = 24 * 60 * 60 * 1000;
        return new Date( today.getTime() + days * aDay );
    }
}
