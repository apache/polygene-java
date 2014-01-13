package org.qi4j.runtime.composite;

import java.lang.reflect.AccessibleObject;
import java.util.Map;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;

/**
 * TODO
 */
public final class TransientStateInstance
    implements StateHolder
{
    private final Map<AccessibleObject, Property<?>> properties;

    public TransientStateInstance( Map<AccessibleObject, Property<?>> properties
    )
    {
        this.properties = properties;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> Property<T> propertyFor( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        Property<T> property = (Property<T>) properties.get( accessor );

        if( property == null )
        {
            throw new IllegalArgumentException( "No such property:" + accessor );
        }

        return property;
    }

    @Override
    public Iterable<Property<?>> properties()
    {
        return properties.values();
    }
}
