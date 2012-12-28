package org.qi4j.tutorials.composites.tutorial10;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

// START SNIPPET: solution

/**
 * This interface contains only the state
 * of the HelloWorld object.
 */
public interface HelloWorldState
{
    @NotEmpty
    Property<String> phrase();

    @NotEmpty
    Property<String> name();
}
// END SNIPPET: solution
