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
package org.qi4j.sample.dcicargo.sample_a.bootstrap.sampledata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.itinerary.Itinerary;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.itinerary.Leg;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.location.Location;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.location.UnLocode;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.CarrierMovement;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.Schedule;
import org.qi4j.sample.dcicargo.sample_a.data.shipping.voyage.Voyage;

/**
 * Test base class with shared Locations, Voyages etc.
 */
public abstract class BaseData
{
    protected Module module;

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

    public BaseData( Module module )
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
        List<CarrierMovement> cm = new ArrayList<CarrierMovement>();
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
        List<Leg> legs = new ArrayList<Leg>();
        legs.addAll( Arrays.asList( legArray ) );
        itinerary.prototype().legs().set( legs );
        return itinerary.newInstance();
    }

    protected RouteSpecification routeSpecification( Location origin, Location destination, Date deadline )
    {
        ValueBuilder<RouteSpecification> routeSpec = module.newValueBuilder( RouteSpecification.class );
        routeSpec.prototype().origin().set( origin );
        routeSpec.prototype().destination().set( destination );
        routeSpec.prototype().arrivalDeadline().set( deadline );
        return routeSpec.newInstance();
    }

    protected static Date day( int days )
    {
        return LocalDate.now().plusDays( days ).toDate();
    }
}
