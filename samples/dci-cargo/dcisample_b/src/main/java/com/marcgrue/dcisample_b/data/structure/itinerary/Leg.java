package com.marcgrue.dcisample_b.data.structure.itinerary;

import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import java.util.Date;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Leg
 *
 * A leg describes an expected segment of a route:
 * - loading onto a voyage at a load location
 * - unloading from the voyage at an unload location
 */
public interface Leg
    extends ValueComposite
{
    Property<Location> loadLocation();

    Property<Date> loadTime();

    Property<Voyage> voyage();

    Property<Location> unloadLocation();

    Property<Date> unloadTime();
}
