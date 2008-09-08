package org.qi4j.entity.javaspaces;

import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.Queryable;
import org.qi4j.property.Property;

@Queryable( false )
public interface JavaSpacesConfiguration
    extends EntityComposite
{
    Property<String> uri();
}
