package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;

// START SNIPPET: entity
public interface Manufacturer
{
    Property<String> name();
    Property<String> country();

    @UseDefaults
    Property<Long> carsProduced();
}

// END SNIPPET: entity
