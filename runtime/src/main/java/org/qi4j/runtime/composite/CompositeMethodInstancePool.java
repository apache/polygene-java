package org.qi4j.runtime.composite;

import java.util.concurrent.atomic.AtomicReference;

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
