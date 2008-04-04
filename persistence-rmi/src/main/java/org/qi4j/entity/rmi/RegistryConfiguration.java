package org.qi4j.entity.rmi;

import org.qi4j.entity.EntityComposite;
import org.qi4j.property.Property;

/**
 * TODO
 */
public interface RegistryConfiguration
    extends EntityComposite
{
    Property<Integer> port();
}
