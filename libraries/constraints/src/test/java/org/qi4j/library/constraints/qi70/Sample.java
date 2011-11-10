package org.qi4j.library.constraints.qi70;

import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

public interface Sample
{
    @NotEmpty
    Property<String> stuff();
}
