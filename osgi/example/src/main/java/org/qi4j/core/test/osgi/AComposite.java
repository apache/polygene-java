package org.qi4j.core.test.osgi;

import org.qi4j.property.Property;
import org.qi4j.composite.Optional;

/**
 * @author Niclas Hedhman
 */
public interface AComposite
{
    @Optional Property<String> property();

    String sayValue();
}
