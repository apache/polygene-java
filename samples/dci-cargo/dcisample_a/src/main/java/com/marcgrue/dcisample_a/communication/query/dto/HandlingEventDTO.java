package com.marcgrue.dcisample_a.communication.query.dto;

import com.marcgrue.dcisample_a.data.shipping.cargo.TrackingId;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEvent;
import com.marcgrue.dcisample_a.data.shipping.handling.HandlingEventType;
import com.marcgrue.dcisample_a.infrastructure.conversion.DTO;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.library.values.Unqualified;

import java.util.Date;

/**
 * HandlingEvent DTO
 *
 * We need the @Unqualified annotation since the HandlingEventDTO interface has other properties than
 * {@link HandlingEvent} so that properties can not be directly mapped when we convert from entity to
 * immutable value DTO. With the annotation, property access methods are compared by name instead.
 */
@Unqualified
public interface HandlingEventDTO extends DTO
{
    Property<Date> completionTime();

    Property<TrackingId> trackingId();

    Property<HandlingEventType> handlingEventType();

    Property<LocationDTO> location();

    @Optional
    Property<VoyageDTO> voyage();
}
