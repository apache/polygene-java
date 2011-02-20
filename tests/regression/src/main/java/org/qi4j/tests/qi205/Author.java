package org.qi4j.tests.qi205;

import org.qi4j.api.property.Property;

public interface Author
{
    Property<String> surname();

    Property<String> forename();
}
