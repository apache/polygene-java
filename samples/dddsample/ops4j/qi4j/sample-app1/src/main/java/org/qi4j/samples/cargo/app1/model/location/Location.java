package org.qi4j.samples.cargo.app1.model.location;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Property;

/**
 * The Identity of Location entities is the UnLocode used in the original dddsample codebase. Have not
 * understood the idea of the intermediary entity types.
 */
public interface Location extends Identity {

    Property<String> commonName();
}