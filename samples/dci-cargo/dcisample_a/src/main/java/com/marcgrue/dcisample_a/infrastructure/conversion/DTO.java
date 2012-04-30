package com.marcgrue.dcisample_a.infrastructure.conversion;

import org.qi4j.api.property.Immutable;
import org.qi4j.api.value.ValueComposite;

/**
 * ValueComposites that extend DTO are candidates to have association types converted and assigned
 */
@Immutable
public interface DTO extends ValueComposite
{
}
