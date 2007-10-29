package org.qi4j.model;

/**
 * TODO
 */
public class Binding
{
    public static Binding bind( InjectionKey key, Object value )
    {
        return new Binding( key, value );
    }

    InjectionKey key;
    Object value;

    public Binding( InjectionKey key, Object value )
    {
        this.key = key;
        this.value = value;
    }

    public InjectionKey getKey()
    {
        return key;
    }

    public Object getValue()
    {
        return value;
    }
}
