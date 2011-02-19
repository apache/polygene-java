package org.qi4j.samples.cargo.app1.model.cargo;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 *
 */
public interface TrackingId extends ValueComposite
{

    Property<String> id();
}