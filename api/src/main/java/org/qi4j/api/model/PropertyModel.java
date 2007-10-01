package org.qi4j.api.model;

import java.lang.reflect.Type;

/**
 * TODO
 */
public class PropertyModel
{
    String name;
    Type type;

    public PropertyModel( String name, Type type )
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public Type getType()
    {
        return type;
    }
}
