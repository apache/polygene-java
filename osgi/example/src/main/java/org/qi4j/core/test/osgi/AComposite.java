package org.qi4j.core.test.osgi;

import org.qi4j.property.Property;

/**
 * @author Niclas Hedhman
 */
public interface AComposite
{
    Property<String> property();

    String sayValue();
}
