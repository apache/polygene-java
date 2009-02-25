package org.qi4j.entitystore.rmi;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;
import org.qi4j.api.configuration.ConfigurationComposite;

/**
 * JAVADOC
 */
public interface RegistryConfiguration
    extends ConfigurationComposite
{
    Property<Integer> port();
}
