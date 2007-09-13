package org.qi4j.runtime;

import java.util.concurrent.atomic.AtomicReference;

/**
 * TODO
 */
public class InvocationInstancePool
{
    AtomicReference<InvocationInstance> first = new AtomicReference<InvocationInstance>();

    public InvocationInstance getInstance()
    {
        InvocationInstance instance = first.get();
        if (instance != null)
        {
            first.set( instance.getNext());

        }
        return instance;
    }

    public void returnInstance(InvocationInstance instance)
    {
        if (!first.compareAndSet(instance.getNext(), instance ))
        {
            instance.setNext(first.getAndSet( instance));
        }
    }
}
