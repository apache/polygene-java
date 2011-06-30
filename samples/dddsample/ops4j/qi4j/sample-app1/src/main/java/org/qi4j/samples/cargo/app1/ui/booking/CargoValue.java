package org.qi4j.samples.cargo.app1.ui.booking;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.samples.cargo.app1.model.cargo.Delivery;
import org.qi4j.samples.cargo.app1.model.cargo.Itinerary;
import org.qi4j.samples.cargo.app1.model.cargo.RouteSpecification;

/**
 *
 */
public interface CargoValue extends ValueComposite {
    Property<String> origin();

    Property<RouteSpecification> routeSpecification();

    @Optional
    Property<Itinerary> itinerary();

    @Optional
    Property<Delivery> delivery();
}
