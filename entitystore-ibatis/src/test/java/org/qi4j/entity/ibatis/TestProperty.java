package org.qi4j.entity.ibatis;

import org.qi4j.property.Property;
import java.lang.reflect.Type;

/**
 * @autor Michael Hunger
* @since 19.05.2008
*/
class TestProperty<T> implements Property<T>
{
    private final T value;
    private final String name;

    public TestProperty( T value, String name )
    {
        this.value = value;
        this.name = name;
    }

    public T get()
    {
        return value;
    }

    public void set( T newValue ) throws IllegalArgumentException
    {
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return null;
    }

    public String name()
    {
        return name;
    }

    public String qualifiedName()
    {
        return name;
    }

    public Type type()
    {
        return value.getClass();
    }
}
