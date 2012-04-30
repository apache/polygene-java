package com.marcgrue.dcisample_a.data.shipping.location;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

/**
 * A location is stops on a journey, such as cargo
 * origin or destination, or carrier movement endpoints.
 *
 * It is uniquely identified by a {@link UnLocode}.
 *
 * All properties are mandatory and immutable.
 */
@Immutable
@Mixins( Location.Mixin.class )
public interface Location
{
    Property<UnLocode> unLocode();

    Property<String> name();

    // Side-effects free and UI agnostic convenience methods
    String getCode();
    String getString();

    abstract class Mixin
          implements Location
    {
        public String getCode()
        {
            return unLocode().get().code().get();
        }
        public String getString()
        {
            return name().get() + " (" + getCode() + ")";
        }
    }
}
