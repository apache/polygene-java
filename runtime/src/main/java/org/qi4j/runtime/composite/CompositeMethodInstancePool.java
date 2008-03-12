package org.qi4j.runtime.composite;

/**
 * TODO
 */
public final class CompositeMethodInstancePool
{
    private CompositeMethodInstance first = null;

    public synchronized CompositeMethodInstance getInstance()
    {
        CompositeMethodInstance instance = first;
        if( instance != null )
        {
            first = instance.getNext();

        }
        return instance;
    }

    public synchronized void returnInstance( CompositeMethodInstance instance )
    {
        instance.setNext( first );
        first = instance;
    }
}

/* TODO Test this code
private AtomicReference<CompositeMethodInstance> first = new AtomicReference<CompositeMethodInstance>();

public CompositeMethodInstance getInstance()
{
//        synchronized(this)
    {
        CompositeMethodInstance instance = first.getAndSet( null );
        return instance;
    }
}

public void returnInstance( CompositeMethodInstance instance )
{
//        synchronized (this)
    {
       //instance.setNext( first );
        first.set( instance );
    }
}
*/