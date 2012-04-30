package com.marcgrue.dcisample_b.bootstrap.sampledata;

import com.marcgrue.dcisample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import com.marcgrue.dcisample_b.data.aggregateroot.CargoAggregateRoot;
import com.marcgrue.dcisample_b.data.aggregateroot.HandlingEventAggregateRoot;
import com.marcgrue.dcisample_b.data.structure.delivery.Delivery;
import com.marcgrue.dcisample_b.data.structure.delivery.NextHandlingEvent;
import com.marcgrue.dcisample_b.data.structure.delivery.RoutingStatus;
import com.marcgrue.dcisample_b.data.structure.delivery.TransportStatus;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;
import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import com.marcgrue.dcisample_b.data.structure.itinerary.Leg;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.location.UnLocode;
import com.marcgrue.dcisample_b.data.structure.voyage.CarrierMovement;
import com.marcgrue.dcisample_b.data.structure.voyage.Schedule;
import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import com.marcgrue.dcisample_b.data.structure.voyage.VoyageNumber;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Test base class with shared data and factory methods.
 */
public abstract class BaseData
{
    protected static ValueBuilderFactory vbf;
    protected static UnitOfWork uow;

    protected static UnLocode AUMEL;
    protected static UnLocode CNHGH;
    protected static UnLocode CNHKG;
    protected static UnLocode CNSHA;
    protected static UnLocode DEHAM;
    protected static UnLocode FIHEL;
    protected static UnLocode JNTKO;
    protected static UnLocode NLRTM;
    protected static UnLocode SEGOT;
    protected static UnLocode SESTO;
    protected static UnLocode SOMGQ;
    protected static UnLocode USCHI;
    protected static UnLocode USDAL;
    protected static UnLocode USNYC;

    protected static Location MELBOURNE;
    protected static Location HANGZHOU;
    protected static Location HONGKONG;
    protected static Location SHANGHAI;
    protected static Location HAMBURG;
    protected static Location HELSINKI;
    protected static Location TOKYO;
    protected static Location ROTTERDAM;
    protected static Location GOTHENBURG;
    protected static Location STOCKHOLM;
    protected static Location MOGADISHU;
    protected static Location DALLAS;
    protected static Location CHICAGO;
    protected static Location NEWYORK;

    protected static CargoAggregateRoot CARGOS;
    protected static HandlingEventAggregateRoot HANDLING_EVENTS;


    protected static UnLocode unlocode( String unlocodeString )
    {
        ValueBuilder<UnLocode> unlocode = vbf.newValueBuilder( UnLocode.class );
        unlocode.prototype().code().set( unlocodeString );
        return unlocode.newInstance();
    }

    protected static Location location( UnLocode unlocode, String locationStr )
    {
        EntityBuilder<Location> location = uow.newEntityBuilder( Location.class, unlocode.code().get() );
        location.instance().unLocode().set( unlocode );
        location.instance().name().set( locationStr );
        return location.newInstance();
    }


    protected static Voyage voyage( String voyageNumberStr, Schedule schedule )
    {
        EntityBuilder<Voyage> voyage = uow.newEntityBuilder( Voyage.class, voyageNumberStr );

        // VoyageNumber
        ValueBuilder<VoyageNumber> voyageNumber = vbf.newValueBuilder( VoyageNumber.class );
        voyageNumber.prototype().number().set( voyageNumberStr );
        voyage.instance().voyageNumber().set( voyageNumber.newInstance() );

        // Schedule
        voyage.instance().schedule().set( schedule );
        return voyage.newInstance();
    }

    protected static CarrierMovement carrierMovement( Location depLoc, Location arrLoc, Date depTime, Date arrTime )
    {
        ValueBuilder<CarrierMovement> carrierMovement = vbf.newValueBuilder( CarrierMovement.class );
        carrierMovement.prototype().departureLocation().set( depLoc );
        carrierMovement.prototype().arrivalLocation().set( arrLoc );
        carrierMovement.prototype().departureTime().set( depTime );
        carrierMovement.prototype().arrivalTime().set( arrTime );
        return carrierMovement.newInstance();
    }

    protected static Schedule schedule( CarrierMovement... carrierMovements )
    {
        ValueBuilder<Schedule> schedule = vbf.newValueBuilder( Schedule.class );
        List<CarrierMovement> cm = new ArrayList<CarrierMovement>();
        cm.addAll( Arrays.asList( carrierMovements ) );
        schedule.prototype().carrierMovements().set( cm );
        return schedule.newInstance();
    }

    protected static Leg leg( Voyage voyage, Location load, Location unload, Date loadTime, Date unloadTime )
    {
        ValueBuilder<Leg> leg = vbf.newValueBuilder( Leg.class );
        leg.prototype().voyage().set( voyage );
        leg.prototype().loadLocation().set( load );
        leg.prototype().unloadLocation().set( unload );
        leg.prototype().loadTime().set( loadTime );
        leg.prototype().unloadTime().set( unloadTime );
        return leg.newInstance();
    }

    protected static Itinerary itinerary( Leg... legArray )
    {
        ValueBuilder<Itinerary> itinerary = vbf.newValueBuilder( Itinerary.class );
        List<Leg> legs = new ArrayList<Leg>();
        legs.addAll( Arrays.asList( legArray ) );
        itinerary.prototype().legs().set( legs );
        return itinerary.newInstance();
    }


    protected static Delivery delivery(
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
        ValueBuilder<Delivery> delivery = vbf.newValueBuilder( Delivery.class );
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
    protected static Delivery delivery( Date date,
                                        TransportStatus transportStatus,
                                        RoutingStatus routingStatus,
                                        Integer itineraryProgressIndex )
    {
        ValueBuilder<Delivery> delivery = vbf.newValueBuilder( Delivery.class );
        delivery.prototype().timestamp().set( date );
        delivery.prototype().transportStatus().set( transportStatus );
        delivery.prototype().routingStatus().set( routingStatus );
        delivery.prototype().itineraryProgressIndex().set( itineraryProgressIndex );
        return delivery.newInstance();
    }

    protected static NextHandlingEvent nextHandlingEvent( HandlingEventType handlingEventType,
                                                          Location location,
                                                          Date time,
                                                          Voyage voyage )
    {
        ValueBuilder<NextHandlingEvent> nextHandlingEvent = vbf.newValueBuilder( NextHandlingEvent.class );
        nextHandlingEvent.prototype().handlingEventType().set( handlingEventType );
        nextHandlingEvent.prototype().location().set( location );
        nextHandlingEvent.prototype().time().set( time );
        nextHandlingEvent.prototype().voyage().set( voyage );
        return nextHandlingEvent.newInstance();
    }

    protected static ParsedHandlingEventData parsedHandlingEventData( Date registrationTime,
                                                                      Date completionTime,
                                                                      String trackingIdString,
                                                                      HandlingEventType handlingEventType,
                                                                      String unLocodeString,
                                                                      String voyageNumberString ) throws Exception
    {
        ValueBuilder<ParsedHandlingEventData> attempt = vbf.newValueBuilder( ParsedHandlingEventData.class );
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
