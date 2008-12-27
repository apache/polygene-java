package org.qi4j.library.swing.binding.example;

import org.qi4j.api.property.Property;

/**
 * @author Lan Boon Ping
 */
public interface Car
{

    Property<String> model();

    Property<String> capacity();

    Property<String> price();
}
