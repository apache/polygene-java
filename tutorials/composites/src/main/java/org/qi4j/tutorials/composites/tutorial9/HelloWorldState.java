package org.qi4j.tutorials.composites.tutorial9;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

/**
 * This interface contains only the state
 * of the HelloWorld object.
 * <p/>
 * The state is declared using Properties. The @NotEmpty annotation is applied to the
 * method to check that the properties are not set to empty strings.
 */
public interface HelloWorldState
{
    @NotEmpty
    Property<String> phrase();

    @NotEmpty
    Property<String> name();
}
