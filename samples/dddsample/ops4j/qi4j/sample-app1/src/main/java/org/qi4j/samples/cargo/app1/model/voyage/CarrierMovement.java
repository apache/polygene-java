package org.qi4j.samples.cargo.app1.model.voyage;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import java.util.Date;


/**
 *
 */
public interface CarrierMovement extends ValueComposite {
    
    Property<String> departureLocationUnLocode();
    Property<String> arrivalLocationUnLocode();
    Property<Date> departureTime();
    Property<Date> arrivalTime();
}