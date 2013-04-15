package org.qi4j.api.property;

/**
 * If you want to catch getting and setting properties, then create a GenericConcern
 * that wraps the Qi4j-supplied Property instance with PropertyWrappers. Override
 * get() and/or set() to perform your custom code.
 */
public class PropertyWrapper
    implements Property<Object>
{
    protected Property<Object> next;

    public PropertyWrapper( Property<Object> next )
    {
        this.next = next;
    }

    public Property<Object> next()
    {
        return next;
    }

    @Override
    public Object get()
    {
        return next.get();
    }

    @Override
    public void set( Object newValue )
        throws IllegalArgumentException, IllegalStateException
    {
        next.set( newValue );
    }

    @Override
    public int hashCode()
    {
        return next.hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        return next.equals( obj );
    }

    @Override
    public String toString()
    {
        return next.toString();
    }
}
