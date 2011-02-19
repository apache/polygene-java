package org.qi4j.samples.cargo.app1.model.voyage;

import java.util.List;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 *
 */
public interface Schedule extends ValueComposite
{

    Property<List<CarrierMovement>> carrierMovements();
}