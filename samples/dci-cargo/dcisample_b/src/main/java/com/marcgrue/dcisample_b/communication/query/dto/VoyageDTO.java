package com.marcgrue.dcisample_b.communication.query.dto;

import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import com.marcgrue.dcisample_b.data.structure.voyage.Voyage;
import com.marcgrue.dcisample_b.infrastructure.conversion.DTO;

/**
 * Voyage DTO
 *
 * Since all properties of Voyage are immutable, we can simply re-use the same interface.
 * We need the Voyage as a DTO when we do entity to value conversions.
 */
public interface VoyageDTO extends Voyage, DTO
{
}
