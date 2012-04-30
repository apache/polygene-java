package com.marcgrue.dcisample_b.data.structure.delivery;

import com.marcgrue.dcisample_b.data.structure.handling.HandlingEventType;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.Date;

/**
 * NextHandlingEvent
 *
 * (former "HandlingActivity" / "ExpectedHandlingEvent")
 *
 * This represents our assumptions about the next handling event for a cargo.
 * 
 * Since a cargo could have been loaded onto an unexpected carrier it seems better
 * not to call the next unload an _expected_ handling event. It's expected to
 * the carrier voyage schedule, but unexpected to the itinerary.
 *
 * A time for the expected event was added.
 */
public interface NextHandlingEvent
      extends ValueComposite
{
    Property<HandlingEventType> handlingEventType();

    Property<Location> location();

    @Optional
    Property<Date> time();

    @Optional
    Property<Voyage> voyage();
}
