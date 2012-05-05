package com.marcgrue.dcisample_b.communication.query.dto;

import com.marcgrue.dcisample_b.data.structure.cargo.Cargo;
import com.marcgrue.dcisample_b.data.structure.cargo.RouteSpecification;
import com.marcgrue.dcisample_b.data.structure.delivery.Delivery;
import com.marcgrue.dcisample_b.data.structure.itinerary.Itinerary;
import com.marcgrue.dcisample_b.data.structure.tracking.TrackingId;
import com.marcgrue.dcisample_b.infrastructure.conversion.DTO;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.library.values.Unqualified;

/**
 * Cargo DTO
 *
 * Qi4j-comment:
 * We need the @Unqualified annotation since the CargoDTO interface has other properties than {@link Cargo}
 * so that properties can not be directly mapped when we convert from entity to immutable value DTO.
 * With the annotation, property access methods are compared by name instead.
 *
 * @see Cargo
 */
@Unqualified
public interface CargoDTO extends DTO
{
    Property<TrackingId> trackingId();

    // Associated Location entity in Cargo is converted to an immutable LocationDTO value object
    Property<LocationDTO> origin();

    Property<RouteSpecification> routeSpecification();

    Property<Delivery> delivery();

    @Optional
    Property<Itinerary> itinerary();
}