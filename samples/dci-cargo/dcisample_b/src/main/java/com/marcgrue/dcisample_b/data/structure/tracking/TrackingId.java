package com.marcgrue.dcisample_b.data.structure.tracking;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.Matches;

/**
 * TrackingId
 *
 * A TrackingId uniquely identifies a particular cargo.
 *
 * Suggested constraints:
 * - starts with a letter [a-zA-Z] or digit
 * - then allows underscore/dash
 * - is minimum 3 characters long
 * - is maximum 30 characters long.
 */
public interface TrackingId
      extends ValueComposite
{
    @Matches( "[a-zA-Z0-9]{1}[a-zA-Z0-9_-]{2,29}" )
    Property<String> id();
}
