package com.marcgrue.dcisample_a.data.shipping.cargo;

import com.marcgrue.dcisample_a.data.shipping.itinerary.Itinerary;
import com.marcgrue.dcisample_a.data.shipping.location.Location;
import com.marcgrue.dcisample_a.data.shipping.delivery.Delivery;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

/**
 * Cargo data
 *
 * {@link TrackingId}           created automatically
 * {@link Location} origin      Specified upon creation (mandatory)
 * {@link RouteSpecification}   Specified upon creation (mandatory)
 * {@link Delivery}             A calculated snapshot of the current delivery status (created by system)
 * {@link Itinerary}            Description of chosen route (optional)
 */
public interface Cargo
{
    @Immutable
    Property<TrackingId> trackingId();

    @Immutable
    Association<Location> origin();

    Property<RouteSpecification> routeSpecification();

    Property<Delivery> delivery();

    @Optional
    Property<Itinerary> itinerary();
}