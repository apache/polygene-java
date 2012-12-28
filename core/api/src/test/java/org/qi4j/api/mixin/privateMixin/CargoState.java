package org.qi4j.api.mixin.privateMixin;

import org.qi4j.api.property.Property;

// START SNIPPET: private
public interface CargoState
{
    Property<String> origin();
    Property<String> destination();
}

// END SNIPPET: private