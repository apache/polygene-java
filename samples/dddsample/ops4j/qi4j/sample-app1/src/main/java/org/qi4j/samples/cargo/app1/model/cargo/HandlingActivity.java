package org.qi4j.samples.cargo.app1.model.cargo;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.samples.cargo.app1.model.handling.HandlingEvent;
import org.qi4j.samples.cargo.app1.model.voyage.Voyage;


/**
 *
 */
public interface HandlingActivity extends ValueComposite {
    Property<HandlingEvent.Type> handlingEventType();

    Property<String> locationUnLocodeIdentity();

    Property<Voyage> voyage();
}