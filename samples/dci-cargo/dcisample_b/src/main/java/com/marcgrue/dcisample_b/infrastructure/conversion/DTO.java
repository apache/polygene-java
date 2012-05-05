package com.marcgrue.dcisample_b.infrastructure.conversion;

import org.qi4j.api.property.Immutable;
import org.qi4j.api.value.ValueComposite;

/**
 * DTO
 *
 * Base class for DTOs
 *
 * Qi4j-comment:
 * ValueComposites that extend DTO are candidates to have association types converted and
 * assigned in the EntityToDTOService.
 */
@Immutable
public interface DTO extends ValueComposite
{
}
