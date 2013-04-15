package org.qi4j.api.association;

/**
 * If you want to catch getting and setting association, then create a GenericConcern
 * that wraps the Qi4j-supplied Association instance with AssociationWrappers. Override
 * get() and/or set() to perform your custom code.
 */
public class AssociationWrapper
    implements Association<Object>
{
    protected Association<Object> next;

    public AssociationWrapper( Association<Object> next )
    {
        this.next = next;
    }

    public Association<Object> next()
    {
        return next;
    }

    @Override
    public Object get()
    {
        return next.get();
    }

    @Override
    public void set( Object associated )
        throws IllegalArgumentException
    {
        next.set( associated );
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
