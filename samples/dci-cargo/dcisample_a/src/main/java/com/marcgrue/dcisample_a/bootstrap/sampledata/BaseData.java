package com.marcgrue.dcisample_a.bootstrap.sampledata;

import com.marcgrue.dcisample_a.data.entity.CargosEntity;
import com.marcgrue.dcisample_a.data.entity.HandlingEventsEntity;
import com.marcgrue.dcisample_a.data.shipping.*;
import com.marcgrue.dcisample_a.data.shipping.cargo.Cargo;
import com.marcgrue.dcisample_a.data.shipping.cargo.RouteSpecification;
import com.marcgrue.dcisample_a.data.shipping.cargo.TrackingId;
import com.marcgrue.dcisample_a.data.shipping.delivery.Delivery;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEvent;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import com.marcgrue.dcisample_a.data.shipping.itinerary.Leg;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.location.UnLocode;
import com.marcgrue.dcisample_a.data.shipping.voyage.CarrierMovement;
import com.marcgrue.dcisample_a.data.shipping.voyage.Schedule;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Test base class with shared Locations, Voyages etc.
 */
public abstract class BaseData
{
    protected static ValueBuilderFactory vbf;
    protected static UnitOfWork uow;

    protected static final Date TODAY = new Date();
    protected static Date deadline;
    protected static Date arrival;

    protected static RouteSpecification routeSpec;
    protected static List<Itinerary> routeCandidates;
    protected static Delivery delivery;
    protected static Cargo cargo;
    protected static TrackingId trackingId;
    protected static Itinerary itinerary;
    protected static HandlingEvent handlingEvent;

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
    protected static Location DALLAS;
    protected static Location CHICAGO;
    protected static Location NEWYORK;

    protected static Voyage V100S; // By ship
    protected static Voyage V200T; // By train
    protected static Voyage V300A; // By air
    protected static Voyage V400S; // By ship
    protected static Voyage V500S; // By ship

    protected static CargosEntity CARGOS;
    protected static HandlingEventsEntity HANDLING_EVENTS;


    protected static UnLocode unlocode( String unlocodeString )
    {
        ValueBuilder<UnLocode> unlocode = vbf.newValueBuilder( UnLocode.class );
        unlocode.prototype().code().set( unlocodeString );
        return unlocode.newInstance();
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

    protected RouteSpecification routeSpecification( Location origin, Location destination, Date deadline )
    {
        ValueBuilder<RouteSpecification> routeSpec = vbf.newValueBuilder( RouteSpecification.class );
        routeSpec.prototype().origin().set( origin );
        routeSpec.prototype().destination().set( destination );
        routeSpec.prototype().arrivalDeadline().set( deadline );
        return routeSpec.newInstance();
    }

    protected static Date day( int days )
    {
        Date today = new Date();
        long aDay = 24 * 60 * 60 * 1000;
        return new Date( today.getTime() + days * aDay );
    }
}
