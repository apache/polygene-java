package org.qi4j.entity.jdbm;

import org.qi4j.entity.EntityComposite;
import org.qi4j.property.Property;

/**
 * TODO
 */
public interface JdbmConfiguration
    extends EntityComposite
{
    Property<String> file();
}
