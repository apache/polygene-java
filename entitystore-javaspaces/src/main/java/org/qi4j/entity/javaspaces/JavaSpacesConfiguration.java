package org.qi4j.entity.javaspaces;

import org.qi4j.entity.EntityComposite;
import org.qi4j.property.Property;

public interface JavaSpacesConfiguration extends EntityComposite
{
    Property<String> uri();
}
