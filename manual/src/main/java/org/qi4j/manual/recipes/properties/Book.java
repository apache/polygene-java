package org.qi4j.manual.recipes.properties;

import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

// START SNIPPET: book
public interface Book
{
    @Immutable
    Property<String> title();

    @Immutable
    Property<String> author();
}
// END SNIPPET: book
