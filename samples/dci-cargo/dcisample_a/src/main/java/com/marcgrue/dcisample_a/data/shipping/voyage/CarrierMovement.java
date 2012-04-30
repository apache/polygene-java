package com.marcgrue.dcisample_a.data.shipping.voyage;

import com.marcgrue.dcisample_a.data.shipping.location.Location;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.Date;

/**
 * A carrier movement is a vessel voyage from one location to another.
 *
 * All properties are mandatory and immutable.
 */
public interface CarrierMovement
      extends ValueComposite
{
    Property<Location> departureLocation();

    Property<Location> arrivalLocation();

    Property<Date> departureTime();

    Property<Date> arrivalTime();
}
