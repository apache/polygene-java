package com.marcgrue.dcisample_a.data.shipping.handling;

import com.marcgrue.dcisample_a.data.shipping.cargo.TrackingId;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.voyage.Voyage;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

import java.util.Date;

/**
 * A HandlingEvent is used to register the event when, for instance,
 * a cargo is unloaded from a carrier at some location at a given time.
 *
 * The HandlingEvent's are sent from different Incident Logging Applications
 * some time after the event occurred and contain information about the
 * {@link TrackingId}, {@link Location}, timestamp of the completion of the event,
 * and possibly, if applicable a {@link Voyage}.
 *
 * HandlingEvent's could contain information about a {@link Voyage} and if so,
 * the event type must be either {@link HandlingEventType#LOAD} or
 * {@link HandlingEventType#UNLOAD}.
 *
 * All other events must be of {@link HandlingEventType#RECEIVE},
 * {@link HandlingEventType#CLAIM} or {@link HandlingEventType#CUSTOMS}.
 * (Handling event type is mandatory).
 */
public interface HandlingEvent
{
    @Immutable
    Property<Date> registrationTime();

    @Immutable
    Property<Date> completionTime();

    @Immutable
    Property<TrackingId> trackingId();

    @Immutable
    Property<HandlingEventType> handlingEventType();

    @Immutable
    Association<Location> location();

    @Optional
    @Immutable
    Association<Voyage> voyage();

    @Optional
    @UseDefaults
    Property<Boolean> wasUnexpected();
}