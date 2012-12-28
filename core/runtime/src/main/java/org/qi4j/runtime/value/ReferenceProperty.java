package org.qi4j.runtime.value;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;

/**
 * The reference for an Association
 */
public class ReferenceProperty
    implements Property<EntityReference>
{
    EntityReference reference;

    public ReferenceProperty()
    {
    }

    public ReferenceProperty( EntityReference reference )
    {
        this.reference = reference;
    }

    @Override
    public EntityReference get()
    {
        return reference;
    }

    @Override
    public void set( EntityReference newValue )
        throws IllegalArgumentException, IllegalStateException
    {
        reference = newValue;
    }
}
