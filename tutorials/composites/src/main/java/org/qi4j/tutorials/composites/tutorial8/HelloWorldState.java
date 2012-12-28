package org.qi4j.tutorials.composites.tutorial8;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

// START SNIPPET: solution

/**
 * This interface contains only the state
 * of the HelloWorld object.
 * <p/>
 * The state is now declared using Properties. The @NotEmpty annotation is applied to the
 * method instead, and has the same meaning as before.
 */
public interface HelloWorldState
{
    @NotEmpty
    Property<String> phrase();

    @NotEmpty
    Property<String> name();
}
// END SNIPPET: solution
