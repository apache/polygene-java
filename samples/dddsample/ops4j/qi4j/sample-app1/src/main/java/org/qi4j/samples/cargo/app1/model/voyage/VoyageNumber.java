package org.qi4j.samples.cargo.app1.model.voyage;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;


/**
 *
 */
public interface VoyageNumber extends ValueComposite {

    Property<String> number();
}