package org.qi4j.manual.recipes.createEntity;

import java.time.ZonedDateTime;
import org.qi4j.api.property.Property;

// START SNIPPET: entity
public interface Accident
{
    Property<String> description();
    Property<ZonedDateTime> occured();
    Property<ZonedDateTime> repaired();
}

// END SNIPPET: entity