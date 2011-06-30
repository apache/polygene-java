package org.qi4j.samples.cargo.app1.model.voyage;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.List;

/**
 *
 */
public interface Schedule extends ValueComposite
{

    Property<List<CarrierMovement>> carrierMovements();
}