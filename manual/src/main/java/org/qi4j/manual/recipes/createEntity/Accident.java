package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.property.Property;
import java.util.Date;

// START SNIPPET: entity
public interface Accident
{
    Property<String> description();
    Property<Date> occured();
    Property<Date> repaired();
}

// END SNIPPET: entity