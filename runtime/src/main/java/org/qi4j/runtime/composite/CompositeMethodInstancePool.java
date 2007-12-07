package org.qi4j.runtime.composite;

import java.util.concurrent.atomic.AtomicReference;

/**
 * TODO
 */
public final class CompositeMethodInstancePool
{
    AtomicReference<CompositeMethodInstance> first = new AtomicReference<CompositeMethodInstance>();

    public CompositeMethodInstance getInstance()
    {
        CompositeMethodInstance instance = first.get();
        if( instance != null )
        {
            first.set( instance.getNext() );

        }
        return instance;
    }

    public void returnInstance( CompositeMethodInstance instance )
    {
        if( !first.compareAndSet( instance.getNext(), instance ) )
        {
            instance.setNext( first.getAndSet( instance ) );
        }
    }
}
