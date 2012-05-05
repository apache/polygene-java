package com.marcgrue.dcisample_a.data.shipping.voyage;

import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Identifies a {@link Voyage}.
 *
 * Voyage number is mandatory and immutable.
 */
public interface VoyageNumber
      extends ValueComposite
{
    Property<String> number();
}
