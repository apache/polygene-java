package com.marcgrue.dcisample_b.data.structure.voyage;

import com.marcgrue.dcisample_b.data.structure.location.Location;
import java.util.Date;
import org.qi4j.api.association.Association;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * CarrierMovement
 *
 * A carrier movement is a vessel voyage from one location to another.
 *
 * All properties are mandatory and immutable.
 */
public interface CarrierMovement
    extends ValueComposite
{
    Association<Location> departureLocation();

    Association<Location> arrivalLocation();

    Property<Date> departureTime();

    Property<Date> arrivalTime();
}
