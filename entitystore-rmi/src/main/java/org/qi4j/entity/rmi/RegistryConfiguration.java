package org.qi4j.entity.rmi;

import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.Queryable;
import org.qi4j.property.Property;

/**
 * TODO
 */
@Queryable( false )
public interface RegistryConfiguration
    extends EntityComposite
{
    Property<Integer> port();
}
