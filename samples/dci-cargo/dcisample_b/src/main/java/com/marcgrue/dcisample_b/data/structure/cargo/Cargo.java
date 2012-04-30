package com.marcgrue.dcisample_b.data.structure.cargo;

import com.marcgrue.dcisample_b.data.structure.delivery.Delivery;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.data.structure.tracking.TrackingId;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

/**
 * Cargo
 *
 * Data describing a cargo, it's planned route and current delivery status.
 *
 * {@link TrackingId}           created automatically
 * {@link Location} origin      Specified upon creation (mandatory)
 * {@link RouteSpecification}   Specified upon creation (mandatory)
 * {@link Itinerary}            Description of chosen route (optional)
 * {@link Delivery}             Snapshot of the current delivery status (automatically created by the system)
 */
public interface Cargo
{
    @Immutable
    Property<TrackingId> trackingId();

    @Immutable
    Association<Location> origin();

    Property<RouteSpecification> routeSpecification();

    @Optional
    Property<Itinerary> itinerary();

    Property<Delivery> delivery();
}