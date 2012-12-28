package org.qi4j.dci.moneytransfer.context;

/**
 * TODO
 */

/**
 * Base class for methodful roles
 */
public class Role<T>
    implements Comparable<Role<T>>
{
    // Self reference to the bound Data object
    protected T self;

    public Role()
    {
    }

    public Role( T self )
    {
        this.self = self;
    }

    public void bind( T newSelf )
    {
        self = newSelf;
    }

    public T self()
    {
        return self;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( obj == null )
        {
            return false;
        }

        if( obj instanceof Role )
        {
            return self.equals( ( (Role) obj ).self );
        }
        else
        {
            return false;
        }
    }

    public int compareTo( Role<T> role )
    {
        return ( (Comparable<T>) self ).compareTo( role.self );
    }
}
