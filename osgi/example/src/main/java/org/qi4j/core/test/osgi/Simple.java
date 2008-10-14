package org.qi4j.core.test.osgi;

import org.qi4j.property.Property;

/**
 * @author Niclas Hedhman
 */
public interface Simple
{
    Property<String> someValue();

    String sayValue();
}
