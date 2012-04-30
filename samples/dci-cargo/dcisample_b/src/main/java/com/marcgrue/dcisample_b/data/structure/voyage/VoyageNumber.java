package com.marcgrue.dcisample_b.data.structure.voyage;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * VoyageNumber
 *
 * Identifies a {@link Voyage}.
 *
 * Voyage number is mandatory and immutable.
 */
public interface VoyageNumber
      extends ValueComposite
{
    Property<String> number();
}
