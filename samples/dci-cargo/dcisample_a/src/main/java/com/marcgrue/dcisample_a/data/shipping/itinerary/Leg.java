package com.marcgrue.dcisample_a.data.shipping.itinerary;

import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.Date;

/**
 * A leg describes an expected segment of a route:
 * - loading onto a voyage at a load location
 * - unloading from the voyage at a unload location
 *
 * All properties are mandatory and immutable.
 */
public interface Leg
      extends ValueComposite
{
    Property<Location> loadLocation();

    Property<Date> loadTime();

    Property<Voyage> voyage();

    Property<Date> unloadTime();

    Property<Location> unloadLocation();
}
