package com.marcgrue.dcisample_b.communication.query.dto;

import com.marcgrue.dcisample_b.data.structure.location.Location;
import com.marcgrue.dcisample_b.infrastructure.conversion.DTO;

/**
 * Location DTO
 *
 * Since all properties of Location are immutable, we can simply re-use the same interface.
 * We need the Location as a DTO when we do entity to value conversions.
 */
public interface LocationDTO extends Location, DTO
{
}
