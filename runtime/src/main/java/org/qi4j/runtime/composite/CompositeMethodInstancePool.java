package org.qi4j.runtime.composite;

import org.qi4j.runtime.composite.qi.CompositeMethodInstance;

/**
 * TODO
 */
public interface CompositeMethodInstancePool
{
    public CompositeMethodInstance getInstance();

    public void returnInstance( CompositeMethodInstance instance );
}
