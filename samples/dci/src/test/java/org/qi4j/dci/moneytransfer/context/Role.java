package org.qi4j.dci.moneytransfer.context;

/**
 * TODO
 */
public class Role<T>
{
    protected T self;

    public void bind( T newSelf )
    {
        this.self = newSelf;
    }
}
