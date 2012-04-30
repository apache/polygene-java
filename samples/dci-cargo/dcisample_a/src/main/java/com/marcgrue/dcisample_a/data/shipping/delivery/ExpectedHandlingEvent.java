package com.marcgrue.dcisample_a.data.shipping.delivery;

import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.Date;

/**
 * An expected handling event (former "HandlingActivity") represents how and where a cargo
 * is expected to be handled next.
 */
public interface ExpectedHandlingEvent
      extends ValueComposite
{
    Property<HandlingEventType> handlingEventType();

    Property<Location> location();

    // Added expected time for the event to happen (compared to the original DDD sample)
    @Optional
    Property<Date> time();

    @Optional
    Property<Voyage> voyage();
}
