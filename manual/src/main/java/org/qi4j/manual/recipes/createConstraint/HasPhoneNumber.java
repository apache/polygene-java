package org.qi4j.manual.recipes.createConstraint;

import org.qi4j.api.property.Property;

// START SNIPPET: property
public interface HasPhoneNumber
{
    @PhoneNumber
    Property<String> phoneNumber();
}
// END SNIPPET: property
