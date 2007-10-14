package org.qi4j.api.model;

import java.lang.reflect.Type;

/**
 * TODO
 */
public class PropertyModel
{
    boolean writeable;
    boolean mutable;
    boolean readable;
    String name;
    Type type;

    public PropertyModel( String name, Type type, boolean writeable, boolean mutable, boolean readable )
    {
        this.mutable = mutable;
        this.readable = readable;
        this.writeable = writeable;
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

    public boolean isWriteable()
    {
        return writeable;
    }

    public boolean isMutable()
    {
        return mutable;
    }

    public boolean isReadable()
    {
        return readable;
    }
}
