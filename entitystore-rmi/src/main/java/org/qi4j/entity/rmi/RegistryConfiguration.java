package org.qi4j.entity.rmi;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
@Queryable( false )
public interface RegistryConfiguration
    extends EntityComposite
{
    Property<Integer> port();
}
