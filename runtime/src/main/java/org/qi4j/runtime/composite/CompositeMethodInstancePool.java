package org.qi4j.runtime.composite;

/**
 * TODO
 */
public interface CompositeMethodInstancePool
{
    public CompositeMethodInstance getInstance();

    public void returnInstance( CompositeMethodInstance instance );
}
