package org.qi4j.runtime.composite;

import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;

import java.lang.reflect.AccessibleObject;
import java.util.Map;

/**
 * TODO
 */
public final class TransientStateInstance
        implements StateHolder
{
    protected Map<AccessibleObject, Property<?>> properties;

    public TransientStateInstance( Map<AccessibleObject, Property<?>> properties
    )
    {
        this.properties = properties;
    }

    public <T> Property<T> propertyFor( AccessibleObject accessor )
            throws IllegalArgumentException
    {
        Property<T> property = (Property<T>) properties.get( accessor );

        if( property == null )
            throw new IllegalArgumentException( "No such property:" + accessor );

        return property;
    }

    @Override
    public Iterable<Property<?>> properties()
    {
        return properties.values();
    }
}
