package org.qi4j.lib.struts2.example;

import org.qi4j.library.constraints.annotation.NotNull;
import org.qi4j.property.Property;

public interface Nameable {

    @NotNull Property<String> name();
}
